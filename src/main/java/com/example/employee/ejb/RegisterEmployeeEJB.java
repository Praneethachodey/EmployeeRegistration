package com.example.employee.ejb;

import com.example.employee.dto.EmployeeWithPoliciesDTO;
import com.example.employee.entity.Employee;
import com.example.employee.entity.SecurityContext;
import com.example.employee.service.EmployeeService;
import com.example.employee.service.AuditService;
import com.example.employee.service.SecurityService;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.annotation.Resource;
import jakarta.ejb.*;
import jakarta.inject.Inject;
import jakarta.jms.ConnectionFactory;
import jakarta.jms.JMSContext;
import jakarta.jms.Queue;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicInteger;

@Stateless
@TransactionManagement(TransactionManagementType.CONTAINER)
public class RegisterEmployeeEJB {

    @EJB
    private EmployeeService employeeService;

    @EJB
    private AuditService auditService;

    @EJB
    private SecurityService securityService;

    @EJB
    private EmployeeDetailsEJB detailsEJB;

    // JMS resources - optional for application functionality
    @Resource(lookup = "java:/jms/queue/employeeQueue")
    private Queue employeeQueue;

    @Resource(lookup = "java:/jms/queue/registrationQueue")
    private Queue registrationQueue;

    @Resource(lookup = "java:/JmsXA")
    private ConnectionFactory connectionFactory;

    @Resource
    private TimerService timerService;

    private CountDownLatch latch = new CountDownLatch(1);
    private Employee cachedEmployee;
    private final ConcurrentHashMap<String, Employee> employeeCache = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);
    private final AtomicInteger registrationCounter = new AtomicInteger(0);
    private String currentTransactionId;
    private SecurityContext currentSecurityContext;
    private boolean jmsAvailable = false;

    @PostConstruct
    public void init() {
        try {
            // Check if JMS is available
            jmsAvailable = (employeeQueue != null && registrationQueue != null && connectionFactory != null);
            
            // Schedule periodic cleanup
            scheduler.scheduleAtFixedRate(this::cleanupExpiredCache, 600, 600, TimeUnit.SECONDS);
            
            // Schedule registration counter reset
            scheduler.scheduleAtFixedRate(this::resetRegistrationCounter, 86400, 86400, TimeUnit.SECONDS);
            
            // Create timer for registration audits
            timerService.createTimer(7200000, 7200000, "Registration Audit Timer");
            
            // JMS availability check
            jmsAvailable = (employeeQueue != null && registrationQueue != null && connectionFactory != null);
        } catch (Exception e) {
            // Log error silently for initialization
            jmsAvailable = false;
        }
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public EmployeeWithPoliciesDTO registerEmployee(Employee employee, String sessionId) {
        try {
            // Validate security context
            if (!securityService.validateSession(sessionId, "WRITE")) {
                auditService.logAuditEvent("SYSTEM", "UNAUTHORIZED_ACCESS", 
                    "Attempted to register employee: " + employee.getEmployeeID(), 
                    "RegisterEmployeeEJB", currentTransactionId);
                throw new SecurityException("Unauthorized employee registration");
            }

            SecurityContext context = securityService.getSecurityContext(sessionId);
            if (context == null) {
                throw new SecurityException("Invalid security context");
            }

            // Check registration limit
            if (registrationCounter.get() >= 100) {
                auditService.logAuditEvent(context.getUserId(), "REGISTRATION_LIMIT_EXCEEDED", 
                    "Registration limit exceeded", 
                    "RegisterEmployeeEJB", currentTransactionId);
                throw new EJBException("Daily registration limit exceeded");
            }

            // Validate employee data
            if (employee.getEmployeeID() == null || employee.getName() == null || employee.getDepartment() == null) {
                throw new IllegalArgumentException("Required employee fields missing");
            }

            // Check if employee already exists
            Employee existingEmployee = employeeService.find(employee.getEmployeeID());
            if (existingEmployee != null) {
                auditService.logAuditEvent(context.getUserId(), "DUPLICATE_REGISTRATION", 
                    "Attempted to register duplicate employee: " + employee.getEmployeeID(), 
                    "RegisterEmployeeEJB", currentTransactionId);
                throw new EJBException("Employee already exists: " + employee.getEmployeeID());
            }

            // Set employee metadata
            employee.setCreatedDate(LocalDateTime.now());
            employee.setLastModified(LocalDateTime.now());
            employee.setStatus("ACTIVE");
            employee.setSecurityLevel("BASIC");

            // Register employee
            employeeService.register(employee);
            registrationCounter.incrementAndGet();

            // Cache employee
            employeeCache.put(employee.getEmployeeID(), employee);
            this.cachedEmployee = employee;

            // Log audit event
            auditService.logAuditEvent(context.getUserId(), "EMPLOYEE_REGISTERED", 
                "Employee registered: " + employee.getEmployeeID() + " in department: " + employee.getDepartment(), 
                "RegisterEmployeeEJB", currentTransactionId);

            // Send to queue if JMS is available
            if (jmsAvailable) {
                sendToEmployeeQueue(employee);
                sendToRegistrationQueue(employee);
            }

            // Get employee details with policies
            EmployeeWithPoliciesDTO result = detailsEJB.getEmployeeWithPolicies(employee.getEmployeeID(), sessionId);

            return result;

        } catch (Exception e) {
            throw new EJBException("Employee registration failed", e);
        }
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public boolean validateEmployeeFromDetailsEJB(String employeeId) {
        try {
            Employee employee = employeeService.find(employeeId);
            return employee != null && "ACTIVE".equals(employee.getStatus());
        } catch (Exception e) {
            return false;
        }
    }

    @Lock(LockType.WRITE)
    public void cleanupExpiredCache() {
        try {
            LocalDateTime cutoff = LocalDateTime.now().minusHours(24);
            
            employeeCache.entrySet().removeIf(entry -> 
                entry.getValue().getLastModified().isBefore(cutoff));
            
        } catch (Exception e) {
            // Log error silently for cache operations
        }
    }

    @Lock(LockType.WRITE)
    public void resetRegistrationCounter() {
        try {
            registrationCounter.set(0);
        } catch (Exception e) {
            // Log error silently for counter operations
        }
    }

    @Timeout
    public void handleRegistrationTimer(Timer timer) {
        try {
            // Perform registration audits
            auditService.logAuditEvent("SYSTEM", "REGISTRATION_AUDIT", 
                "Registration audit completed, count: " + registrationCounter.get(), 
                "RegisterEmployeeEJB", currentTransactionId);
            
        } catch (Exception e) {
            // Log error silently for timer operations
        }
    }

    private void sendToEmployeeQueue(Employee employee) {
        if (!jmsAvailable) {
            return;
        }
        
        try (JMSContext context = connectionFactory.createContext(JMSContext.SESSION_TRANSACTED)) {
            String message = String.format("EMPLOYEE_REGISTERED:%s:%s:%s", 
                employee.getEmployeeID(), employee.getDepartment(), LocalDateTime.now());
            context.createProducer().send(employeeQueue, message);
        } catch (Exception e) {
            // Log error silently for JMS operations
        }
    }

    private void sendToRegistrationQueue(Employee employee) {
        if (!jmsAvailable) {
            return;
        }
        
        try (JMSContext context = connectionFactory.createContext(JMSContext.SESSION_TRANSACTED)) {
            String message = String.format("REGISTRATION:%s:%s:%s", 
                employee.getEmployeeID(), employee.getName(), LocalDateTime.now());
            context.createProducer().send(registrationQueue, message);
        } catch (Exception e) {
            // Log error silently for JMS operations
        }
    }

    @PreDestroy
    public void cleanup() {
        try {
            scheduler.shutdown();
            if (!scheduler.awaitTermination(60, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
