package com.example.employee.ejb;

import com.example.employee.entity.HrPolicy;
import com.example.employee.entity.SecurityContext;
import com.example.employee.entity.AuditLog;
import com.example.employee.repository.HrPolicyRepository;
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
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Singleton
@ConcurrencyManagement(ConcurrencyManagementType.CONTAINER)
public class HrPolicyEJB {

    @Inject
    private HrPolicyRepository hrPolicyRepository;

    @Inject
    private AuditService auditService;

    @Inject
    private SecurityService securityService;

    // JMS resources - required for application functionality
    @Resource(lookup = "java:/jms/queue/hrPolicyAuditQueue")
    private Queue auditQueue;

    @Resource(lookup = "java:/jms/queue/hrPolicyNotificationQueue")
    private Queue notificationQueue;

    @Resource(lookup = "java:/JmsXA")
    private ConnectionFactory connectionFactory;

    @Resource
    private TimerService timerService;

    private List<HrPolicy> cachedPolicies;
    private final ConcurrentHashMap<String, HrPolicy> policyCache = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(3);
    private final ConcurrentHashMap<String, Integer> policyAccessCount = new ConcurrentHashMap<>();
    private String currentTransactionId;
    private boolean jmsAvailable = false;

    @Lock(LockType.WRITE)
    @PostConstruct
    public void init() {
        try {
            // Check if JMS is available
            jmsAvailable = (auditQueue != null && notificationQueue != null && connectionFactory != null);
            
            this.cachedPolicies = hrPolicyRepository.findAllPolicies();
            
            // Initialize cache
            for (HrPolicy policy : cachedPolicies) {
                policyCache.put(policy.getPolicyId(), policy);
                policy.setCached(true);
            }
            
            // Schedule cache refresh
            scheduler.scheduleAtFixedRate(this::refreshPolicyCache, 1800, 1800, TimeUnit.SECONDS);
            
            // Schedule compliance check
            scheduler.scheduleAtFixedRate(this::checkCompliancePolicies, 3600, 3600, TimeUnit.SECONDS);
            
            // Create timer for policy events
            timerService.createTimer(3600000, 3600000, "Policy Event Timer");
            
            // Send initialization event if JMS is available
            if (jmsAvailable) {
                sendPolicyEvent("POLICY_CACHE_INITIALIZED", "SYSTEM");
            }
            
        } catch (Exception e) {
            // Log error silently for initialization
            jmsAvailable = false;
        }
    }

    @Lock(LockType.READ)
    public List<HrPolicy> getPolicies() {
        return cachedPolicies;
    }

    @Lock(LockType.READ)
    public List<HrPolicy> getPoliciesByDepartment(String department, String sessionId) {
        try {
            // Validate security context
            if (!securityService.validateSession(sessionId, "READ")) {
                auditService.logAuditEvent("SYSTEM", "UNAUTHORIZED_ACCESS", 
                    "Attempted to access policies for department: " + department, 
                    "HrPolicyEJB", currentTransactionId);
                throw new SecurityException("Unauthorized access to policies");
            }

            SecurityContext context = securityService.getSecurityContext(sessionId);
            if (context == null) {
                throw new SecurityException("Invalid security context");
            }

            // Filter policies based on department and security level
            List<HrPolicy> filteredPolicies = cachedPolicies.stream()
                .filter(policy -> policy.isApplicableForDepartment(department))
                .filter(policy -> !policy.requiresSecurityLevel(context.getSecurityLevel()) || 
                                 context.canAccessSensitiveData())
                .collect(Collectors.toList());

            // Increment access count
            for (HrPolicy policy : filteredPolicies) {
                policy.incrementAccessCount();
                policyAccessCount.merge(policy.getPolicyId(), 1, Integer::sum);
            }

            // Log audit event
            auditService.logAuditEvent(context.getUserId(), "POLICY_ACCESS", 
                "Accessed policies for department: " + department + ", count: " + filteredPolicies.size(), 
                "HrPolicyEJB", currentTransactionId);

            return filteredPolicies;

        } catch (Exception e) {
            // Log error silently for policy operations
            throw new EJBException("Failed to get policies by department", e);
        }
    }

    @Lock(LockType.WRITE)
    public HrPolicy getPolicyById(String policyId, String sessionId) {
        try {
            // Validate security context
            if (!securityService.validateSession(sessionId, "READ")) {
                auditService.logAuditEvent("SYSTEM", "UNAUTHORIZED_ACCESS", 
                    "Attempted to access policy: " + policyId, 
                    "HrPolicyEJB", currentTransactionId);
                throw new SecurityException("Unauthorized access to policy");
            }

            SecurityContext context = securityService.getSecurityContext(sessionId);
            if (context == null) {
                throw new SecurityException("Invalid security context");
            }

            // Check cache first
            HrPolicy policy = policyCache.get(policyId);
            if (policy == null) {
                // Query database
                policy = hrPolicyRepository.findPolicyById(policyId);
                if (policy != null) {
                    policyCache.put(policyId, policy);
                    policy.setCached(true);
                }
            }

            if (policy != null) {
                // Check security requirements
                if (policy.requiresSecurityLevel(context.getSecurityLevel()) && 
                    !context.canAccessSensitiveData()) {
                    auditService.logAuditEvent(context.getUserId(), "SECURITY_VIOLATION", 
                        "Attempted to access restricted policy: " + policyId, 
                        "HrPolicyEJB", currentTransactionId);
                    throw new SecurityException("Insufficient security level for policy access");
                }

                // Increment access count
                policy.incrementAccessCount();
                policyAccessCount.merge(policyId, 1, Integer::sum);

                // Log audit event
                auditService.logAuditEvent(context.getUserId(), "POLICY_ACCESS", 
                    "Accessed policy: " + policyId, 
                    "HrPolicyEJB", currentTransactionId);

                // Check if policy needs audit
                if (policy.needsAudit()) {
                    auditService.logAuditEvent(context.getUserId(), "POLICY_AUDIT_REQUIRED", 
                        "Policy requires audit: " + policyId, 
                        "HrPolicyEJB", currentTransactionId);
                }
            }

            return policy;

        } catch (Exception e) {
            // Log error silently for policy operations
            throw new EJBException("Failed to get policy by ID", e);
        }
    }

    @Lock(LockType.WRITE)
    public void updatePolicy(HrPolicy policy, String sessionId) {
        try {
            // Validate security context
            if (!securityService.validateSession(sessionId, "WRITE")) {
                auditService.logAuditEvent("SYSTEM", "UNAUTHORIZED_ACCESS", 
                    "Attempted to update policy: " + policy.getPolicyId(), 
                    "HrPolicyEJB", currentTransactionId);
                throw new SecurityException("Unauthorized policy update");
            }

            SecurityContext context = securityService.getSecurityContext(sessionId);
            if (context == null) {
                throw new SecurityException("Invalid security context");
            }

            // Check if policy requires admin access
            if (policy.isComplianceRequired() && !context.hasRole("ADMIN")) {
                auditService.logAuditEvent(context.getUserId(), "COMPLIANCE_VIOLATION", 
                    "Attempted to update compliance policy without admin role: " + policy.getPolicyId(), 
                    "HrPolicyEJB", currentTransactionId);
                throw new SecurityException("Admin role required for compliance policy updates");
            }

            // Update policy
            policy.setLastModified(LocalDateTime.now());
            policy.setLastAccessed(LocalDateTime.now());
            hrPolicyRepository.updatePolicy(policy);

            // Update cache
            policyCache.put(policy.getPolicyId(), policy);

            // Log audit event
            auditService.logAuditEvent(context.getUserId(), "POLICY_UPDATE", 
                "Updated policy: " + policy.getPolicyId(), 
                "HrPolicyEJB", currentTransactionId);

            // Send notification if JMS is available
            if (jmsAvailable) {
                sendPolicyNotification("POLICY_UPDATED", policy.getPolicyId(), context.getUserId());
            }

        } catch (Exception e) {
            // Log error silently for policy operations
            throw new EJBException("Failed to update policy", e);
        }
    }

    @Lock(LockType.READ)
    public List<HrPolicy> getActivePolicies() {
        return cachedPolicies.stream()
            .filter(HrPolicy::isActive)
            .collect(Collectors.toList());
    }

    @Lock(LockType.READ)
    public List<HrPolicy> getPoliciesByCategory(String category, String sessionId) {
        try {
            // Validate security context
            if (!securityService.validateSession(sessionId, "READ")) {
                auditService.logAuditEvent("SYSTEM", "UNAUTHORIZED_ACCESS", 
                    "Attempted to access policies by category: " + category, 
                    "HrPolicyEJB", currentTransactionId);
                throw new SecurityException("Unauthorized access to policies");
            }

            SecurityContext context = securityService.getSecurityContext(sessionId);
            if (context == null) {
                throw new SecurityException("Invalid security context");
            }

            // Filter policies by category
            List<HrPolicy> filteredPolicies = cachedPolicies.stream()
                .filter(policy -> category.equals(policy.getCategory()))
                .filter(policy -> !policy.requiresSecurityLevel(context.getSecurityLevel()) || 
                                 context.canAccessSensitiveData())
                .collect(Collectors.toList());

            // Log audit event
            auditService.logAuditEvent(context.getUserId(), "POLICY_CATEGORY_ACCESS", 
                "Accessed policies by category: " + category + ", count: " + filteredPolicies.size(), 
                "HrPolicyEJB", currentTransactionId);

            return filteredPolicies;

        } catch (Exception e) {
            // Log error silently for policy operations
            throw new EJBException("Failed to get policies by category", e);
        }
    }



    @Lock(LockType.WRITE)
    public void refreshPolicyCache() {
        try {
            // Update cache
            cachedPolicies = hrPolicyRepository.findAllPolicies();
            policyCache.clear();
            for (HrPolicy policy : cachedPolicies) {
                policyCache.put(policy.getPolicyId(), policy);
            }
            
        } catch (Exception e) {
            // Log error silently for cache operations
        }
    }

    @Lock(LockType.WRITE)
    public void checkCompliancePolicies() {
        try {
            List<HrPolicy> compliancePolicies = cachedPolicies.stream()
                .filter(HrPolicy::isComplianceRequired)
                .collect(Collectors.toList());

            for (HrPolicy policy : compliancePolicies) {
                if (policy.needsAudit()) {
                    auditService.logAuditEvent("SYSTEM", "COMPLIANCE_AUDIT", 
                        "Compliance policy requires audit: " + policy.getPolicyId(), 
                        "HrPolicyEJB", currentTransactionId);
                }
            }
            
        } catch (Exception e) {
            // Log error silently for compliance operations
        }
    }

    @Timeout
    public void handlePolicyTimer(Timer timer) {
        try {
            // Perform policy audits
            List<HrPolicy> policiesNeedingAudit = cachedPolicies.stream()
                .filter(HrPolicy::needsAudit)
                .collect(Collectors.toList());

            for (HrPolicy policy : policiesNeedingAudit) {
                auditService.logAuditEvent("SYSTEM", "POLICY_AUDIT", 
                    "Policy audit performed: " + policy.getPolicyId(), 
                    "HrPolicyEJB", currentTransactionId);
            }
            
        } catch (Exception e) {
            // Log error silently for timer operations
        }
    }

    @Lock(LockType.READ)
    public int getPolicyAccessCount(String policyId) {
        return policyAccessCount.getOrDefault(policyId, 0);
    }

    @Lock(LockType.WRITE)
    public void clearPolicyAccessCounts() {
        policyAccessCount.clear();
    }

    private void sendPolicyEvent(String eventType, String policyId) {
        if (!jmsAvailable) {
            return;
        }
        
        try (JMSContext context = connectionFactory.createContext(JMSContext.SESSION_TRANSACTED)) {
            String message = String.format("POLICY_EVENT:%s:%s:%s", 
                eventType, policyId, LocalDateTime.now());
            context.createProducer().send(auditQueue, message);
        } catch (Exception e) {
            // Log error silently for JMS operations
        }
    }

    private void sendPolicyNotification(String eventType, String policyId, String userId) {
        if (!jmsAvailable) {
            return;
        }
        
        try (JMSContext context = connectionFactory.createContext(JMSContext.SESSION_TRANSACTED)) {
            String message = String.format("POLICY_NOTIFICATION:%s:%s:%s:%s", 
                eventType, policyId, userId, LocalDateTime.now());
            context.createProducer().send(notificationQueue, message);
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
