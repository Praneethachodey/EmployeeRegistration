package com.example.employee.repository;

import com.example.employee.entity.SecurityContext;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@ApplicationScoped
public class SecurityContextRepository {
    
    @PersistenceContext
    private EntityManager entityManager;
    
    private final ConcurrentHashMap<String, SecurityContext> sessionCache = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, AtomicInteger> failedAttemptsCache = new ConcurrentHashMap<>();
    
    public void saveSecurityContext(SecurityContext securityContext) {
        entityManager.persist(securityContext);
        sessionCache.put(securityContext.getSessionId(), securityContext);
    }
    
    public SecurityContext findBySessionId(String sessionId) {
        // Check cache first
        SecurityContext cached = sessionCache.get(sessionId);
        if (cached != null) {
            return cached;
        }
        
        // Query database
        TypedQuery<SecurityContext> query = entityManager.createNamedQuery("SecurityContext.findBySessionId", SecurityContext.class);
        query.setParameter("sessionId", sessionId);
        List<SecurityContext> results = query.getResultList();
        
        if (!results.isEmpty()) {
            SecurityContext context = results.get(0);
            sessionCache.put(sessionId, context);
            return context;
        }
        
        return null;
    }
    
    public List<SecurityContext> findByUserId(String userId) {
        TypedQuery<SecurityContext> query = entityManager.createNamedQuery("SecurityContext.findByUserId", SecurityContext.class);
        query.setParameter("userId", userId);
        return query.getResultList();
    }
    
    public List<SecurityContext> findActiveSessions() {
        TypedQuery<SecurityContext> query = entityManager.createNamedQuery("SecurityContext.findActiveSessions", SecurityContext.class);
        return query.getResultList();
    }
    
    public List<SecurityContext> findExpiredSessions() {
        TypedQuery<SecurityContext> query = entityManager.createQuery(
            "SELECT s FROM SecurityContext s WHERE s.expiryDate < :now", 
            SecurityContext.class);
        query.setParameter("now", LocalDateTime.now());
        return query.getResultList();
    }
    
    public List<SecurityContext> findLockedSessions() {
        TypedQuery<SecurityContext> query = entityManager.createQuery(
            "SELECT s FROM SecurityContext s WHERE s.lockedUntil > :now", 
            SecurityContext.class);
        query.setParameter("now", LocalDateTime.now());
        return query.getResultList();
    }
    
    public List<SecurityContext> findBySecurityLevel(String securityLevel) {
        TypedQuery<SecurityContext> query = entityManager.createQuery(
            "SELECT s FROM SecurityContext s WHERE s.securityLevel = :securityLevel", 
            SecurityContext.class);
        query.setParameter("securityLevel", securityLevel);
        return query.getResultList();
    }
    
    public List<SecurityContext> findSessionsRequiringAudit() {
        TypedQuery<SecurityContext> query = entityManager.createQuery(
            "SELECT s FROM SecurityContext s WHERE s.auditRequired = true", 
            SecurityContext.class);
        return query.getResultList();
    }
    
    public List<SecurityContext> findSessionsBySourceEJB(String sourceEJB) {
        TypedQuery<SecurityContext> query = entityManager.createQuery(
            "SELECT s FROM SecurityContext s WHERE s.sourceEJB = :sourceEJB", 
            SecurityContext.class);
        query.setParameter("sourceEJB", sourceEJB);
        return query.getResultList();
    }
    
    public void updateSecurityContext(SecurityContext securityContext) {
        entityManager.merge(securityContext);
        sessionCache.put(securityContext.getSessionId(), securityContext);
    }
    
    public void deleteSecurityContext(String sessionId) {
        SecurityContext context = findBySessionId(sessionId);
        if (context != null) {
            entityManager.remove(context);
            sessionCache.remove(sessionId);
        }
    }
    
    public void invalidateExpiredSessions() {
        List<SecurityContext> expiredSessions = findExpiredSessions();
        for (SecurityContext session : expiredSessions) {
            session.setActive(false);
            entityManager.merge(session);
            sessionCache.remove(session.getSessionId());
        }
    }
    
    public void clearFailedAttempts(String userId) {
        failedAttemptsCache.remove(userId);
    }
    
    public int getFailedAttempts(String userId) {
        AtomicInteger attempts = failedAttemptsCache.get(userId);
        return attempts != null ? attempts.get() : 0;
    }
    
    public void incrementFailedAttempts(String userId) {
        failedAttemptsCache.computeIfAbsent(userId, k -> new AtomicInteger(0)).incrementAndGet();
    }
    
    public void clearSessionCache() {
        sessionCache.clear();
    }
    
    public List<SecurityContext> findSessionsByComplianceLevel(String complianceLevel) {
        TypedQuery<SecurityContext> query = entityManager.createQuery(
            "SELECT s FROM SecurityContext s WHERE s.complianceLevel = :complianceLevel", 
            SecurityContext.class);
        query.setParameter("complianceLevel", complianceLevel);
        return query.getResultList();
    }
    
    public List<SecurityContext> findSessionsByTransactionId(String transactionId) {
        TypedQuery<SecurityContext> query = entityManager.createQuery(
            "SELECT s FROM SecurityContext s WHERE s.transactionId = :transactionId", 
            SecurityContext.class);
        query.setParameter("transactionId", transactionId);
        return query.getResultList();
    }
    
    public void updateLastAccessed(String sessionId) {
        SecurityContext context = findBySessionId(sessionId);
        if (context != null) {
            context.updateLastAccessed();
            entityManager.merge(context);
            sessionCache.put(sessionId, context);
        }
    }
    
    public List<SecurityContext> findSessionsByIpAddress(String ipAddress) {
        TypedQuery<SecurityContext> query = entityManager.createQuery(
            "SELECT s FROM SecurityContext s WHERE s.ipAddress = :ipAddress", 
            SecurityContext.class);
        query.setParameter("ipAddress", ipAddress);
        return query.getResultList();
    }
    
    public void lockSession(String sessionId, LocalDateTime lockUntil) {
        SecurityContext context = findBySessionId(sessionId);
        if (context != null) {
            context.setLockedUntil(lockUntil);
            entityManager.merge(context);
            sessionCache.put(sessionId, context);
        }
    }
    
    public void unlockSession(String sessionId) {
        SecurityContext context = findBySessionId(sessionId);
        if (context != null) {
            context.setLockedUntil(null);
            context.resetFailedAttempts();
            entityManager.merge(context);
            sessionCache.put(sessionId, context);
        }
    }
} 