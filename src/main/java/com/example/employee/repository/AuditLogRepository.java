package com.example.employee.repository;

import com.example.employee.entity.AuditLog;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@ApplicationScoped
public class AuditLogRepository {
    
    @PersistenceContext
    private EntityManager entityManager;
    
    private final ConcurrentHashMap<String, AtomicInteger> auditCounters = new ConcurrentHashMap<>();
    
    public void saveAuditLog(AuditLog auditLog) {
        entityManager.persist(auditLog);
        incrementAuditCounter(auditLog.getEmployeeId());
    }
    
    public List<AuditLog> findByEmployeeId(String employeeId) {
        TypedQuery<AuditLog> query = entityManager.createNamedQuery("AuditLog.findByEmployeeId", AuditLog.class);
        query.setParameter("employeeId", employeeId);
        return query.getResultList();
    }
    
    public List<AuditLog> findByAction(String action) {
        TypedQuery<AuditLog> query = entityManager.createNamedQuery("AuditLog.findByAction", AuditLog.class);
        query.setParameter("action", action);
        return query.getResultList();
    }
    
    public List<AuditLog> findByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        TypedQuery<AuditLog> query = entityManager.createNamedQuery("AuditLog.findByDateRange", AuditLog.class);
        query.setParameter("startDate", startDate);
        query.setParameter("endDate", endDate);
        return query.getResultList();
    }
    
    public List<AuditLog> findHighPriorityAudits() {
        TypedQuery<AuditLog> query = entityManager.createQuery(
            "SELECT a FROM AuditLog a WHERE a.auditLevel = 'CRITICAL' OR a.action = 'SECURITY_BREACH'", 
            AuditLog.class);
        return query.getResultList();
    }
    
    public List<AuditLog> findExpiredRetentionLogs() {
        TypedQuery<AuditLog> query = entityManager.createQuery(
            "SELECT a FROM AuditLog a WHERE a.timestamp < :expiryDate", 
            AuditLog.class);
        query.setParameter("expiryDate", LocalDateTime.now().minusDays(365));
        return query.getResultList();
    }
    
    public List<AuditLog> findBySourceEJB(String sourceEJB) {
        TypedQuery<AuditLog> query = entityManager.createQuery(
            "SELECT a FROM AuditLog a WHERE a.sourceEJB = :sourceEJB", 
            AuditLog.class);
        query.setParameter("sourceEJB", sourceEJB);
        return query.getResultList();
    }
    
    public List<AuditLog> findByTransactionId(String transactionId) {
        TypedQuery<AuditLog> query = entityManager.createQuery(
            "SELECT a FROM AuditLog a WHERE a.transactionId = :transactionId", 
            AuditLog.class);
        query.setParameter("transactionId", transactionId);
        return query.getResultList();
    }
    
    public List<AuditLog> findComplianceRequiredLogs() {
        TypedQuery<AuditLog> query = entityManager.createQuery(
            "SELECT a FROM AuditLog a WHERE a.complianceRequired = true", 
            AuditLog.class);
        return query.getResultList();
    }
    
    public void archiveOldLogs() {
        List<AuditLog> expiredLogs = findExpiredRetentionLogs();
        for (AuditLog log : expiredLogs) {
            log.markForArchive();
            entityManager.merge(log);
        }
    }
    
    public int getAuditCountForEmployee(String employeeId) {
        AtomicInteger counter = auditCounters.get(employeeId);
        return counter != null ? counter.get() : 0;
    }
    
    private void incrementAuditCounter(String employeeId) {
        auditCounters.computeIfAbsent(employeeId, k -> new AtomicInteger(0)).incrementAndGet();
    }
    
    public void clearAuditCounters() {
        auditCounters.clear();
    }
    
    public List<AuditLog> findUnarchivedLogs() {
        TypedQuery<AuditLog> query = entityManager.createQuery(
            "SELECT a FROM AuditLog a WHERE a.archived = false", 
            AuditLog.class);
        return query.getResultList();
    }
    
    public void markLogsForArchive(List<Long> logIds) {
        TypedQuery<AuditLog> query = entityManager.createQuery(
            "SELECT a FROM AuditLog a WHERE a.id IN :logIds", 
            AuditLog.class);
        query.setParameter("logIds", logIds);
        List<AuditLog> logs = query.getResultList();
        
        for (AuditLog log : logs) {
            log.markForArchive();
            entityManager.merge(log);
        }
    }
} 