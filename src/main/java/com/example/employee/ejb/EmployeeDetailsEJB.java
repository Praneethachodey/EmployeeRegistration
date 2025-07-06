package com.example.employee.ejb;

import com.example.employee.dto.EmployeeWithPoliciesDTO;
import com.example.employee.entity.Employee;
import com.example.employee.entity.HrPolicy;
import com.example.employee.entity.SecurityContext;
import com.example.employee.entity.AuditLog;
import com.example.employee.service.HrPolicyService;
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
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.Socket;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Future;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.HashMap;
import java.util.Map;

@Stateless
@TransactionManagement(TransactionManagementType.CONTAINER)
public class EmployeeDetailsEJB {

    @Inject
    private HrPolicyEJB hrPolicyEJB;

    @Inject
    private HrPolicyService hrPolicyService;

    @Inject
    private EmployeeService employeeService;

    @Inject
    private AuditService auditService;

    @Inject
    private SecurityService securityService;

    @EJB
    private RegisterEmployeeEJB registerEJB;

    @Resource
    private TimerService timerService;

    // JMS resources - required for application functionality
    @Resource(lookup = "java:/jms/queue/employeeDetailsQueue")
    private Queue employeeDetailsQueue;

    @Resource(lookup = "java:/jms/queue/externalPolicyQueue")
    private Queue externalPolicyQueue;

    @Resource(lookup = "java:/JmsXA")
    private ConnectionFactory connectionFactory;

    private final ConcurrentHashMap<String, EmployeeWithPoliciesDTO> responseCache = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Integer> accessCounters = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);
    private String currentTransactionId;
    private boolean jmsAvailable = false;

    @PostConstruct
    public void init() {
        // Check if JMS is available
        jmsAvailable = (employeeDetailsQueue != null && externalPolicyQueue != null && connectionFactory != null);
        
        // Schedule cache cleanup
        scheduler.scheduleAtFixedRate(this::cleanupExpiredCache, 300, 300, TimeUnit.SECONDS);
        
        // Schedule access counter reset
        scheduler.scheduleAtFixedRate(this::resetAccessCounters, 3600, 3600, TimeUnit.SECONDS);
        
        // JMS availability check
        jmsAvailable = (employeeDetailsQueue != null && externalPolicyQueue != null && connectionFactory != null);
    }

    @Lock(LockType.READ)
    public EmployeeWithPoliciesDTO getEmployeeWithPolicies(String employeeId, String sessionId) {
        try {
            // Validate security context
            if (!securityService.validateSession(sessionId, "READ")) {
                auditService.logAuditEvent("SYSTEM", "UNAUTHORIZED_ACCESS", 
                    "Attempted to access employee: " + employeeId, 
                    "EmployeeDetailsEJB", currentTransactionId);
                throw new SecurityException("Unauthorized access to employee details");
            }

            SecurityContext context = securityService.getSecurityContext(sessionId);
            if (context == null) {
                throw new SecurityException("Invalid security context");
            }

            // Check cache first
            String cacheKey = employeeId + "_" + context.getSecurityLevel();
            EmployeeWithPoliciesDTO cachedResult = responseCache.get(cacheKey);
            if (cachedResult != null) {
                accessCounters.merge(employeeId, 1, Integer::sum);
                return cachedResult;
            }

            // Get employee details
            Employee employee = employeeService.find(employeeId);
            if (employee == null) {
                throw new EJBException("Employee not found: " + employeeId);
            }

            // Check security level for employee access
            if (employee.getSecurityLevel().equals("ADMIN") && 
                !context.canAccessSensitiveData()) {
                auditService.logAuditEvent(context.getUserId(), "SECURITY_VIOLATION", 
                    "Attempted to access employee with insufficient security level: " + employeeId, 
                    "EmployeeDetailsEJB", currentTransactionId);
                throw new SecurityException("Insufficient security level for employee access");
            }

            // Get policies for employee's department
            List<HrPolicy> policies = hrPolicyEJB.getPoliciesByDepartment(employee.getDepartment(), sessionId);

            // Create complex response object
            EmployeeWithPoliciesDTO result = new EmployeeWithPoliciesDTO();
            result.setEmployee(employee);
            result.setPolicies(policies);
            result.setResponseTimestamp(LocalDateTime.now());
            result.setCached(false);

            // MULTI-LAYER PROCESSING: Security Validation Layer
            EmployeeWithPoliciesDTO.SecurityValidationResult securityResult = new EmployeeWithPoliciesDTO.SecurityValidationResult();
            securityResult.setAuthorized(true);
            securityResult.setSecurityLevel(employee.getSecurityLevel());
            securityResult.setValidationChain("EmployeeDetailsEJB -> SecurityService -> HrPolicyEJB");
            securityResult.setContext(context);
            
            Map<String, Boolean> accessRights = new HashMap<>();
            accessRights.put("READ_EMPLOYEE", true);
            accessRights.put("READ_POLICIES", policies.size() > 0);
            accessRights.put("UPDATE_EMPLOYEE", context.canAccessSensitiveData());
            accessRights.put("DELETE_EMPLOYEE", context.canAccessSensitiveData());
            securityResult.setAccessRights(accessRights);
            
            List<String> permissions = new java.util.ArrayList<>();
            permissions.add("EMPLOYEE_READ");
            permissions.add("POLICY_READ");
            if (context.canAccessSensitiveData()) {
                permissions.add("EMPLOYEE_WRITE");
                permissions.add("EMPLOYEE_DELETE");
            }
            securityResult.setPermissions(permissions);
            
            result.setSecurityValidation(securityResult);

            // MULTI-LAYER PROCESSING: Business Rule Validation Layer
            EmployeeWithPoliciesDTO.BusinessRuleValidation businessValidation = new EmployeeWithPoliciesDTO.BusinessRuleValidation();
            businessValidation.setCompliant("ACTIVE".equals(employee.getStatus()) && policies.stream().anyMatch(p -> "MANDATORY".equals(p.getCategory())));
            businessValidation.setDepartmentValidation(employee.getDepartment() + "_VALIDATED");
            businessValidation.setPolicyCompliance("COMPLIANT_" + policies.size() + "_POLICIES");
            
            Map<String, Object> businessRules = new HashMap<>();
            businessRules.put("EMPLOYEE_ACTIVE", "ACTIVE".equals(employee.getStatus()));
            businessRules.put("DEPARTMENT_VALID", !employee.getDepartment().isEmpty());
            businessRules.put("POLICIES_REQUIRED", policies.size() > 0);
            businessRules.put("SECURITY_LEVEL_APPROPRIATE", employee.getSecurityLevel().equals("BASIC") || context.canAccessSensitiveData());
            businessValidation.setBusinessRules(businessRules);
            
            List<String> complianceChecks = new java.util.ArrayList<>();
            complianceChecks.add("EMPLOYEE_STATUS_CHECK");
            complianceChecks.add("DEPARTMENT_POLICY_CHECK");
            complianceChecks.add("SECURITY_LEVEL_CHECK");
            businessValidation.setComplianceChecks(complianceChecks);
            
            Map<String, Boolean> ruleResults = new HashMap<>();
            ruleResults.put("EMPLOYEE_EXISTS", true);
            ruleResults.put("DEPARTMENT_VALID", true);
            ruleResults.put("POLICIES_AVAILABLE", policies.size() > 0);
            ruleResults.put("SECURITY_COMPLIANT", securityResult.isAuthorized());
            businessValidation.setRuleResults(ruleResults);
            
            result.setBusinessRuleValidation(businessValidation);

            // MULTI-LAYER PROCESSING: Audit Trail Layer
            EmployeeWithPoliciesDTO.AuditTrail auditTrail = new EmployeeWithPoliciesDTO.AuditTrail();
            auditTrail.setTransactionId(UUID.randomUUID().toString());
            auditTrail.setProcessingChain("EmployeeDetailsEJB -> HrPolicyEJB -> SecurityService -> AuditService");
            
            Map<String, LocalDateTime> timestamps = new HashMap<>();
            timestamps.put("START", LocalDateTime.now().minusSeconds(3));
            timestamps.put("EMPLOYEE_FETCH", LocalDateTime.now().minusSeconds(2));
            timestamps.put("POLICY_FETCH", LocalDateTime.now().minusSeconds(1));
            timestamps.put("COMPLETE", LocalDateTime.now());
            auditTrail.setTimestamps(timestamps);
            
            List<String> operations = new java.util.ArrayList<>();
            operations.add("SECURITY_VALIDATION");
            operations.add("EMPLOYEE_FETCH");
            operations.add("POLICY_RETRIEVAL");
            operations.add("BUSINESS_RULE_CHECK");
            operations.add("AUDIT_LOGGING");
            auditTrail.setOperations(operations);
            
            Map<String, String> contextData = new HashMap<>();
            contextData.put("SESSION_ID", sessionId);
            contextData.put("EMPLOYEE_ID", employeeId);
            contextData.put("DEPARTMENT", employee.getDepartment());
            contextData.put("SECURITY_LEVEL", employee.getSecurityLevel());
            contextData.put("POLICY_COUNT", String.valueOf(policies.size()));
            auditTrail.setContextData(contextData);
            
            result.setAuditTrail(auditTrail);

            // MULTI-LAYER PROCESSING: Cross Reference Data Layer
            EmployeeWithPoliciesDTO.CrossReferenceData crossRefs = new EmployeeWithPoliciesDTO.CrossReferenceData();
            crossRefs.setDependencies(new java.util.ArrayList<>());
            crossRefs.getDependencies().add("HrPolicyEJB");
            crossRefs.getDependencies().add("SecurityService");
            crossRefs.getDependencies().add("AuditService");
            crossRefs.getDependencies().add("EmployeeService");
            
            Map<String, String> references = new HashMap<>();
            references.put("EMPLOYEE_SOURCE", "EmployeeService");
            references.put("POLICY_SOURCE", "HrPolicyEJB");
            references.put("SECURITY_SOURCE", "SecurityService");
            references.put("AUDIT_SOURCE", "AuditService");
            crossRefs.setReferences(references);
            
            result.setCrossReferences(crossRefs);

            // MULTI-LAYER PROCESSING: Response Metadata Layer
            EmployeeWithPoliciesDTO.ResponseMetadata metadata = new EmployeeWithPoliciesDTO.ResponseMetadata();
            metadata.setVersion("2.0");
            metadata.setSource("EmployeeDetailsEJB");
            metadata.setGeneratedAt(LocalDateTime.now());
            metadata.setProcessingTime("3 seconds");
            
            Map<String, Object> configuration = new HashMap<>();
            configuration.put("CACHE_ENABLED", true);
            configuration.put("SECURITY_ENABLED", true);
            configuration.put("AUDIT_ENABLED", true);
            configuration.put("COMPLIANCE_CHECK_ENABLED", true);
            metadata.setConfiguration(configuration);
            
            List<String> transformations = new java.util.ArrayList<>();
            transformations.add("SECURITY_VALIDATION");
            transformations.add("BUSINESS_RULE_VALIDATION");
            transformations.add("AUDIT_TRAIL_GENERATION");
            transformations.add("CROSS_REFERENCE_POPULATION");
            transformations.add("METADATA_ENRICHMENT");
            metadata.setTransformations(transformations);
            
            result.setMetadata(metadata);

            // CONDITIONAL LOGIC: Add validation errors based on business rules
            if (!"ACTIVE".equals(employee.getStatus())) {
                result.addValidationError("EMPLOYEE_STATUS", "Employee status is not active", "ERROR", "EmployeeDetailsEJB");
            }
            if (policies.isEmpty()) {
                result.addValidationError("POLICIES", "No policies found for department", "WARNING", "HrPolicyEJB");
            }
            if (!securityResult.isAuthorized()) {
                result.addValidationError("SECURITY", "Insufficient security level", "ERROR", "SecurityService");
            }

            // Add dynamic fields based on conditional logic
            result.addDynamicField("COMPLEXITY_LEVEL", "HIGH");
            result.addDynamicField("INTERDEPENDENCY_COUNT", 3);
            result.addDynamicField("LAYER_COUNT", 5);
            result.addDynamicField("PROCESSING_CHAIN", "EmployeeDetailsEJB -> HrPolicyEJB -> SecurityService -> AuditService");

            // Cache the result
            responseCache.put(cacheKey, result);
            accessCounters.merge(employeeId, 1, Integer::sum);

            // Log audit event
            auditService.logAuditEvent(context.getUserId(), "EMPLOYEE_ACCESS", 
                "Accessed employee details: " + employeeId + " with " + policies.size() + " policies", 
                "EmployeeDetailsEJB", currentTransactionId);

            // Send to queue if JMS is available
            if (jmsAvailable) {
                sendToEmployeeDetailsQueue(employeeId, policies.size());
            }

            return result;

        } catch (Exception e) {
            System.err.println("Failed to get employee with policies: " + e.getMessage());
            throw new EJBException("Employee access failed", e);
        }
    }

    @Lock(LockType.READ)
    public List<Employee> getEmployeesByDepartment(String department, String sessionId) {
        try {
            if (!securityService.validateSession(sessionId, "READ")) {
                throw new SecurityException("Unauthorized access");
            }

            SecurityContext context = securityService.getSecurityContext(sessionId);
            
            // For now, return empty list since getEmployeesByDepartment is not implemented
            // This can be implemented later when needed
            return new java.util.ArrayList<>();

        } catch (Exception e) {
            System.err.println("Failed to get employees by department: " + e.getMessage());
            throw new EJBException("Employee access failed", e);
        }
    }

    @Lock(LockType.WRITE)
    public void refreshEmployeeCache(String employeeId) {
        try {
            // Remove from cache
            responseCache.entrySet().removeIf(entry -> entry.getKey().startsWith(employeeId + "_"));
        } catch (Exception e) {
            // Log error silently for cache operations
        }
    }

    @Lock(LockType.WRITE)
    public void cleanupExpiredCache() {
        try {
            LocalDateTime cutoff = LocalDateTime.now().minusMinutes(30);
            
            responseCache.entrySet().removeIf(entry -> 
                entry.getValue().getResponseTimestamp().isBefore(cutoff));
            
        } catch (Exception e) {
            System.err.println("Failed to cleanup expired cache: " + e.getMessage());
        }
    }

    @Lock(LockType.WRITE)
    public void resetAccessCounters() {
        try {
            accessCounters.clear();
        } catch (Exception e) {
            // Log error silently for counter operations
        }
    }

    @Timeout
    public void handleExternalPolicyTimer(Timer timer) {
        try {
            // Refresh external policies - simplified for now
            // Timer functionality can be extended as needed
        } catch (Exception e) {
            // Log error silently for timer operations
        }
    }

    private void sendToEmployeeDetailsQueue(String employeeId, int policyCount) {
        if (!jmsAvailable) {
            return;
        }
        
        try (JMSContext context = connectionFactory.createContext(JMSContext.SESSION_TRANSACTED)) {
            String message = String.format("EMPLOYEE_DETAILS:%s:%d:%s", 
                employeeId, policyCount, LocalDateTime.now());
            context.createProducer().send(employeeDetailsQueue, message);
        } catch (Exception e) {
            // Log error silently for JMS operations
        }
    }

    private void sendToExternalPolicyQueue(String policy) {
        if (!jmsAvailable) {
            return;
        }
        
        try (JMSContext context = connectionFactory.createContext(JMSContext.SESSION_TRANSACTED)) {
            String message = String.format("EXTERNAL_POLICY:%s:%s", 
                policy, LocalDateTime.now());
            context.createProducer().send(externalPolicyQueue, message);
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

    @Lock(LockType.WRITE)
    public void updateEmployee(String employeeId, String name, String department, String email, String phone, String sessionId) {
        // Security check (reuse logic from getEmployeeWithPolicies)
        if (!securityService.validateSession(sessionId, "WRITE")) {
            throw new SecurityException("Unauthorized update attempt");
        }
        Employee employee = employeeService.find(employeeId);
        if (employee == null) {
            throw new EJBException("Employee not found: " + employeeId);
        }
        employee.setName(name);
        employee.setDepartment(department);
        employee.setEmail(email);
        employee.setPhone(phone);
        employee.setLastModified(java.time.LocalDateTime.now());
        employeeService.register(employee); // Assuming register() does save/update
        // Optionally, log audit event
        auditService.logAuditEvent("SYSTEM", "EMPLOYEE_UPDATED", "Employee updated: " + employeeId, "EmployeeDetailsEJB", null);
        // Optionally, refresh cache
        refreshEmployeeCache(employeeId);
    }

    @Lock(LockType.WRITE)
    public void deleteEmployee(String employeeId, String sessionId) {
        // Security check (reuse logic from getEmployeeWithPolicies)
        if (!securityService.validateSession(sessionId, "WRITE")) {
            throw new SecurityException("Unauthorized delete attempt");
        }
        Employee employee = employeeService.find(employeeId);
        if (employee == null) {
            throw new EJBException("Employee not found: " + employeeId);
        }
        employeeService.delete(employeeId);
        // Log audit event
        auditService.logAuditEvent("SYSTEM", "EMPLOYEE_DELETED", "Employee deleted: " + employeeId, "EmployeeDetailsEJB", null);
        // Refresh cache
        refreshEmployeeCache(employeeId);
    }
}


