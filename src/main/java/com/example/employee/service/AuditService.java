package com.example.employee.service;

import com.example.employee.entity.AuditLog;
import com.example.employee.entity.SecurityContext;
import com.example.employee.repository.AuditLogRepository;
import com.example.employee.repository.SecurityContextRepository;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.annotation.Resource;
import jakarta.ejb.*;
import jakarta.inject.Inject;
import jakarta.jms.ConnectionFactory;
import jakarta.jms.JMSContext;
import jakarta.jms.Queue;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Singleton
@ConcurrencyManagement(ConcurrencyManagementType.CONTAINER)
public class AuditService {
    
    @Inject
    private AuditLogRepository auditLogRepository;
    
    @Inject
    private SecurityContextRepository securityContextRepository;
    
    // JMS resources - required for application functionality
    @Resource(lookup = "java:/jms/queue/auditQueue")
    private Queue auditQueue;
    
    @Resource(lookup = "java:/JmsXA")
    private ConnectionFactory connectionFactory;
    
    @Resource
    private TimerService timerService;
    
    private final ConcurrentHashMap<String, AuditLog> pendingAudits = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);
    private final ConcurrentHashMap<String, Integer> auditCounters = new ConcurrentHashMap<>();
    private boolean jmsAvailable = false;
    
    @PostConstruct
    public void init() {
        // Schedule compliance audit
        scheduler.scheduleAtFixedRate(this::processPendingAudits, 3600, 3600, TimeUnit.SECONDS);
        
        // Schedule expired audits cleanup
        scheduler.scheduleAtFixedRate(this::cleanupExpiredAudits, 1800, 1800, TimeUnit.SECONDS);
        
        // JMS availability check
        jmsAvailable = (auditQueue != null && connectionFactory != null);
        
        // Create timer for compliance audits
        timerService.createTimer(7200000, 7200000, "Compliance Audit Timer");
    }
    
    @Lock(LockType.WRITE)
    public void logAuditEvent(String employeeId, String action, String details, String sourceEJB, String transactionId) {
        try {
            AuditLog auditLog = new AuditLog();
            auditLog.setEmployeeId(employeeId);
            auditLog.setAction(action);
            auditLog.setDetails(details);
            auditLog.setTimestamp(LocalDateTime.now());
            auditLog.setSourceEJB(sourceEJB);
            auditLog.setTransactionId(transactionId);
            
            // Check if this is a high priority audit
            if (isHighPriorityAudit(action)) {
                processAuditImmediately(auditLog);
            } else {
                // Add to pending audits
                String auditKey = UUID.randomUUID().toString();
                pendingAudits.put(auditKey, auditLog);
                
                // Send to queue if JMS is available
                if (jmsAvailable) {
                    sendToAuditQueue(auditLog);
                }
            }
            
            // Increment audit counter
            auditCounters.merge(action, 1, Integer::sum);
            
        } catch (Exception e) {
            // Log error silently for audit operations
        }
    }
    
    @Lock(LockType.READ)
    public List<AuditLog> getAuditHistory(String employeeId) {
        return auditLogRepository.findByEmployeeId(employeeId);
    }
    
    @Lock(LockType.READ)
    public List<AuditLog> getHighPriorityAudits() {
        return auditLogRepository.findHighPriorityAudits();
    }
    
    @Lock(LockType.WRITE)
    public void processPendingAudits() {
        try {
            if (pendingAudits.isEmpty()) {
                return;
            }
            
            // Process up to 100 pending audits
            int processed = 0;
            for (AuditLog auditLog : pendingAudits.values()) {
                if (processed >= 100) break;
                
                auditLogRepository.saveAuditLog(auditLog);
                processed++;
            }
            
            // Remove processed audits
            pendingAudits.clear();
            
        } catch (Exception e) {
            // Log error silently for audit processing
        }
    }
    
    @Lock(LockType.WRITE)
    public void cleanupExpiredAudits() {
        try {
            // Cleanup expired audits - simplified for now
            // Cleanup functionality can be extended as needed
        } catch (Exception e) {
            // Log error silently for cleanup operations
        }
    }
    
    @Timeout
    public void handleComplianceTimer(Timer timer) {
        try {
            // Perform compliance audits
            List<AuditLog> complianceAudits = auditLogRepository.findByAction("COMPLIANCE");
            
            for (AuditLog audit : complianceAudits) {
                if (audit.getTimestamp().isBefore(LocalDateTime.now().minusDays(1))) {
                    auditLogRepository.markLogsForArchive(java.util.Arrays.asList(audit.getId()));
                }
            }
            
        } catch (Exception e) {
            // Log error silently for timer operations
        }
    }
    
    @Lock(LockType.READ)
    public int getAuditCountForEmployee(String employeeId) {
        return auditCounters.getOrDefault(employeeId, 0);
    }
    
    @Lock(LockType.WRITE)
    public void clearAuditCounters() {
        auditCounters.clear();
    }
    
    private boolean isHighPriorityAudit(String action) {
        return "SECURITY_VIOLATION".equals(action) || 
               "UNAUTHORIZED_ACCESS".equals(action) || 
               "COMPLIANCE_VIOLATION".equals(action);
    }
    
    private void processAuditImmediately(AuditLog auditLog) {
        try {
            auditLogRepository.saveAuditLog(auditLog);
        } catch (Exception e) {
            // Log error silently for audit processing
        }
    }
    
    private void sendToAuditQueue(AuditLog auditLog) {
        if (!jmsAvailable) {
            return;
        }
        
        try (JMSContext context = connectionFactory.createContext(JMSContext.SESSION_TRANSACTED)) {
            String message = String.format("AUDIT:%s:%s:%s", 
                auditLog.getEmployeeId(), 
                auditLog.getAction(), 
                auditLog.getTimestamp());
            context.createProducer().send(auditQueue, message);
        } catch (Exception e) {
            // Log error silently for JMS operations
        }
    }
    
    @Lock(LockType.READ)
    public List<AuditLog> getAuditsBySourceEJB(String sourceEJB) {
        return auditLogRepository.findBySourceEJB(sourceEJB);
    }
    
    @Lock(LockType.READ)
    public List<AuditLog> getAuditsByTransactionId(String transactionId) {
        return auditLogRepository.findByTransactionId(transactionId);
    }
    
    @Lock(LockType.WRITE)
    public void markAuditsForArchive(List<Long> auditIds) {
        auditLogRepository.markLogsForArchive(auditIds);
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