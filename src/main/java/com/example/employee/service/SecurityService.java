package com.example.employee.service;

import com.example.employee.entity.SecurityContext;
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
public class SecurityService {
    
    @Inject
    private SecurityContextRepository securityContextRepository;
    
    // JMS resources - required for application functionality
    @Resource(lookup = "java:/jms/queue/securityQueue")
    private Queue securityQueue;
    
    @Resource(lookup = "java:/JmsXA")
    private ConnectionFactory connectionFactory;
    
    @Resource
    private TimerService timerService;
    
    private final ConcurrentHashMap<String, SecurityContext> activeSessions = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);
    private final ConcurrentHashMap<String, Integer> failedAttempts = new ConcurrentHashMap<>();
    private boolean jmsAvailable = false;
    
    @PostConstruct
    public void init() {
        // Schedule security audit
        scheduler.scheduleAtFixedRate(this::cleanupExpiredSessions, 1800, 1800, TimeUnit.SECONDS);
        
        // Schedule suspicious activity check
        scheduler.scheduleAtFixedRate(this::checkSuspiciousActivity, 300, 300, TimeUnit.SECONDS);
        
        // Create timer for security audits
        timerService.createTimer(600000, 600000, "Security Audit Timer");
        
        // JMS availability check
        jmsAvailable = (securityQueue != null && connectionFactory != null);
    }
    
    @Lock(LockType.WRITE)
    public SecurityContext createSecurityContext(String userId, String securityLevel, String sessionId) {
        try {
            SecurityContext context = new SecurityContext();
            context.setSessionId(sessionId);
            context.setUserId(userId);
            context.setSecurityLevel(securityLevel);
            context.setCreatedDate(LocalDateTime.now());
            context.setLastAccessed(LocalDateTime.now());
            context.setActive(true);
            context.setExpiryDate(LocalDateTime.now().plusHours(8));
            // Grant all permissions for demo
            context.setPermissions(java.util.Arrays.asList("WRITE", "READ"));
            
            // Save to database
            securityContextRepository.saveSecurityContext(context);
            
            // Add to active sessions
            activeSessions.put(sessionId, context);
            
            // Send security event if JMS is available
            if (jmsAvailable) {
                sendToSecurityQueue("SESSION_CREATED", userId, sessionId);
            }
            
            return context;
            
        } catch (Exception e) {
            throw new EJBException("Security context creation failed", e);
        }
    }
    
    @Lock(LockType.READ)
    public boolean validateSession(String sessionId, String requiredPermission) {
        try {
            SecurityContext context = activeSessions.get(sessionId);
            if (context == null) {
                context = securityContextRepository.findBySessionId(sessionId);
                if (context != null && context.isActive()) {
                    activeSessions.put(sessionId, context);
                }
            }
            
            if (context == null || !context.isActive()) {
                return false;
            }
            
            // Check if session is expired
            if (context.getExpiryDate() != null && context.getExpiryDate().isBefore(LocalDateTime.now())) {
                context.setActive(false);
                activeSessions.remove(sessionId);
                return false;
            }
            
            // Update last accessed
            context.setLastAccessed(LocalDateTime.now());
            
            // Check permissions if required
            if (requiredPermission != null) {
                return context.hasPermission(requiredPermission);
            }
            
            return true;
            
        } catch (Exception e) {
            return false;
        }
    }
    
    @Lock(LockType.READ)
    public SecurityContext getSecurityContext(String sessionId) {
        try {
            SecurityContext context = activeSessions.get(sessionId);
            if (context == null) {
                context = securityContextRepository.findBySessionId(sessionId);
                if (context != null && context.isActive()) {
                    activeSessions.put(sessionId, context);
                }
            }
            
            if (context != null && context.isActive()) {
                context.setLastAccessed(LocalDateTime.now());
            }
            
            return context;
            
        } catch (Exception e) {
            return null;
        }
    }
    
    @Lock(LockType.WRITE)
    public void invalidateSession(String sessionId) {
        try {
            SecurityContext context = activeSessions.remove(sessionId);
            if (context != null) {
                context.setActive(false);
                securityContextRepository.updateSecurityContext(context);
                
                // Send security event if JMS is available
                if (jmsAvailable) {
                    sendToSecurityQueue("SESSION_INVALIDATED", context.getUserId(), sessionId);
                }
            }
            
        } catch (Exception e) {
            // Log error silently for session operations
        }
    }
    
    @Lock(LockType.WRITE)
    public void cleanupExpiredSessions() {
        try {
            LocalDateTime cutoff = LocalDateTime.now().minusHours(8);
            
            activeSessions.entrySet().removeIf(entry -> {
                SecurityContext context = entry.getValue();
                if (context.getLastAccessed().isBefore(cutoff)) {
                    context.setActive(false);
                    securityContextRepository.updateSecurityContext(context);
                    return true;
                }
                return false;
            });
            
        } catch (Exception e) {
            // Log error silently for cleanup operations
        }
    }
    
    @Lock(LockType.WRITE)
    public void checkSuspiciousActivity() {
        try {
            // Check for failed attempts
            for (String userId : failedAttempts.keySet()) {
                int attempts = failedAttempts.get(userId);
                if (attempts > 5) {
                    // Lock user account
                    List<SecurityContext> userSessions = securityContextRepository.findByUserId(userId);
                    for (SecurityContext session : userSessions) {
                        session.setActive(false);
                        securityContextRepository.updateSecurityContext(session);
                    }
                    
                    // Send security alert if JMS is available
                    if (jmsAvailable) {
                        sendToSecurityQueue("SUSPICIOUS_ACTIVITY", userId, "Account locked due to multiple failed attempts");
                    }
                }
            }
            
        } catch (Exception e) {
            // Log error silently for security operations
        }
    }
    
    @Timeout
    public void handleSecurityTimer(Timer timer) {
        try {
            // Perform security audits - simplified for now
            // Timer functionality can be extended as needed
        } catch (Exception e) {
            // Log error silently for timer operations
        }
    }
    
    private void sendToSecurityQueue(String eventType, String userId, String data) {
        if (!jmsAvailable) {
            return;
        }
        
        try (JMSContext context = connectionFactory.createContext(JMSContext.SESSION_TRANSACTED)) {
            String message = String.format("SECURITY:%s:%s:%s:%s", 
                eventType, userId, data, LocalDateTime.now());
            context.createProducer().send(securityQueue, message);
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
            System.out.println("SecurityService cleanup completed");
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
} 