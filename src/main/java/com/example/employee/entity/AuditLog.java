package com.example.employee.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "audit_logs")
@NamedQueries({
    @NamedQuery(name = "AuditLog.findByEmployeeId", query = "SELECT a FROM AuditLog a WHERE a.employeeId = :employeeId ORDER BY a.timestamp DESC"),
    @NamedQuery(name = "AuditLog.findByAction", query = "SELECT a FROM AuditLog a WHERE a.action = :action"),
    @NamedQuery(name = "AuditLog.findByDateRange", query = "SELECT a FROM AuditLog a WHERE a.timestamp BETWEEN :startDate AND :endDate")
})
public class AuditLog {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "employee_id")
    private String employeeId;
    
    @Column(name = "action")
    private String action;
    
    @Column(name = "details", length = 1000)
    private String details;
    
    @Column(name = "timestamp")
    private LocalDateTime timestamp;
    
    @Column(name = "user_id")
    private String userId;
    
    @Column(name = "session_id")
    private String sessionId;
    
    @Column(name = "ip_address")
    private String ipAddress;
    
    @Column(name = "source_ejb")
    private String sourceEJB;
    
    @Column(name = "transaction_id")
    private String transactionId;
    
    @Column(name = "security_level")
    private String securityLevel;
    
    @Column(name = "compliance_required")
    private boolean complianceRequired;
    
    @Column(name = "audit_level")
    private String auditLevel = "BASIC";
    
    @Column(name = "encrypted_data")
    private boolean encryptedData;
    
    @Column(name = "retention_days")
    private Integer retentionDays = 365;
    
    @Column(name = "archived")
    private boolean archived = false;
    
    @Column(name = "archive_date")
    private LocalDateTime archiveDate;
    
    public AuditLog() {
        this.timestamp = LocalDateTime.now();
    }
    
    public AuditLog(String employeeId, String action, String details) {
        this();
        this.employeeId = employeeId;
        this.action = action;
        this.details = details;
    }
    
    // Business logic methods
    public boolean isRetentionExpired() {
        return timestamp != null && 
               LocalDateTime.now().minusDays(retentionDays).isAfter(timestamp);
    }
    
    public boolean requiresEncryption() {
        return "SENSITIVE".equals(auditLevel) || "ADMIN".equals(securityLevel);
    }
    
    public void markForArchive() {
        this.archived = true;
        this.archiveDate = LocalDateTime.now();
    }
    
    public boolean isHighPriority() {
        return "CRITICAL".equals(auditLevel) || "SECURITY_BREACH".equals(action);
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(String employeeId) {
        this.employeeId = employeeId;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getSourceEJB() {
        return sourceEJB;
    }

    public void setSourceEJB(String sourceEJB) {
        this.sourceEJB = sourceEJB;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public String getSecurityLevel() {
        return securityLevel;
    }

    public void setSecurityLevel(String securityLevel) {
        this.securityLevel = securityLevel;
    }

    public boolean isComplianceRequired() {
        return complianceRequired;
    }

    public void setComplianceRequired(boolean complianceRequired) {
        this.complianceRequired = complianceRequired;
    }

    public String getAuditLevel() {
        return auditLevel;
    }

    public void setAuditLevel(String auditLevel) {
        this.auditLevel = auditLevel;
    }

    public boolean isEncryptedData() {
        return encryptedData;
    }

    public void setEncryptedData(boolean encryptedData) {
        this.encryptedData = encryptedData;
    }

    public Integer getRetentionDays() {
        return retentionDays;
    }

    public void setRetentionDays(Integer retentionDays) {
        this.retentionDays = retentionDays;
    }

    public boolean isArchived() {
        return archived;
    }

    public void setArchived(boolean archived) {
        this.archived = archived;
    }

    public LocalDateTime getArchiveDate() {
        return archiveDate;
    }

    public void setArchiveDate(LocalDateTime archiveDate) {
        this.archiveDate = archiveDate;
    }
} 