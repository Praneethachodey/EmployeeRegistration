package com.example.employee.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Entity
@Table(name = "security_contexts")
@NamedQueries({
    @NamedQuery(name = "SecurityContext.findBySessionId", query = "SELECT s FROM SecurityContext s WHERE s.sessionId = :sessionId"),
    @NamedQuery(name = "SecurityContext.findByUserId", query = "SELECT s FROM SecurityContext s WHERE s.userId = :userId"),
    @NamedQuery(name = "SecurityContext.findActiveSessions", query = "SELECT s FROM SecurityContext s WHERE s.active = true")
})
public class SecurityContext {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "session_id", unique = true)
    private String sessionId;
    
    @Column(name = "user_id")
    private String userId;
    
    @Column(name = "security_level")
    private String securityLevel;
    
    @Column(name = "ip_address")
    private String ipAddress;
    
    @Column(name = "user_agent")
    private String userAgent;
    
    @Column(name = "created_date")
    private LocalDateTime createdDate;
    
    @Column(name = "last_accessed")
    private LocalDateTime lastAccessed;
    
    @Column(name = "expiry_date")
    private LocalDateTime expiryDate;
    
    @Column(name = "active")
    private boolean active = true;
    
    @Column(name = "encryption_key")
    private String encryptionKey;
    
    @Column(name = "signature")
    private String signature;
    
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "security_permissions", joinColumns = @JoinColumn(name = "security_context_id"))
    @Column(name = "permission")
    private List<String> permissions;
    
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "security_roles", joinColumns = @JoinColumn(name = "security_context_id"))
    @Column(name = "role")
    private List<String> roles;
    
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "security_attributes", joinColumns = @JoinColumn(name = "security_context_id"))
    @MapKeyColumn(name = "attribute_key")
    @Column(name = "attribute_value")
    private Map<String, String> attributes = new java.util.HashMap<>();
    
    @Column(name = "multi_factor_enabled")
    private boolean multiFactorEnabled = false;
    
    @Column(name = "mfa_token")
    private String mfaToken;
    
    @Column(name = "failed_attempts")
    private Integer failedAttempts = 0;
    
    @Column(name = "locked_until")
    private LocalDateTime lockedUntil;
    
    @Column(name = "compliance_level")
    private String complianceLevel = "BASIC";
    
    @Column(name = "audit_required")
    private boolean auditRequired = false;
    
    @Column(name = "source_ejb")
    private String sourceEJB;
    
    @Column(name = "transaction_id")
    private String transactionId;
    
    public SecurityContext() {
        this.createdDate = LocalDateTime.now();
        this.lastAccessed = LocalDateTime.now();
    }
    
    public SecurityContext(String sessionId, String userId, String securityLevel) {
        this();
        this.sessionId = sessionId;
        this.userId = userId;
        this.securityLevel = securityLevel;
    }
    
    // Complex business logic methods
    public boolean isExpired() {
        return expiryDate != null && LocalDateTime.now().isAfter(expiryDate);
    }
    
    public boolean isLocked() {
        return lockedUntil != null && LocalDateTime.now().isBefore(lockedUntil);
    }
    
    public boolean hasPermission(String permission) {
        return permissions != null && permissions.contains(permission);
    }
    
    public boolean hasRole(String role) {
        return roles != null && roles.contains(role);
    }
    
    public void incrementFailedAttempts() {
        failedAttempts++;
        if (failedAttempts >= 3) {
            lockedUntil = LocalDateTime.now().plusMinutes(30);
        }
    }
    
    public void resetFailedAttempts() {
        failedAttempts = 0;
        lockedUntil = null;
    }
    
    public boolean requiresMFA() {
        return multiFactorEnabled && mfaToken == null;
    }
    
    public boolean canAccessSensitiveData() {
        return "ADMIN".equals(securityLevel) || "MANAGER".equals(securityLevel);
    }
    
    public void updateLastAccessed() {
        this.lastAccessed = LocalDateTime.now();
    }
    
    public boolean isSessionValid() {
        return active && !isExpired() && !isLocked();
    }
    
    public String getAttribute(String key) {
        return attributes.get(key);
    }
    
    public void setAttribute(String key, String value) {
        attributes.put(key, value);
    }
    
    public boolean requiresAudit() {
        return auditRequired || "HIGH".equals(complianceLevel);
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getSecurityLevel() {
        return securityLevel;
    }

    public void setSecurityLevel(String securityLevel) {
        this.securityLevel = securityLevel;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public LocalDateTime getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(LocalDateTime createdDate) {
        this.createdDate = createdDate;
    }

    public LocalDateTime getLastAccessed() {
        return lastAccessed;
    }

    public void setLastAccessed(LocalDateTime lastAccessed) {
        this.lastAccessed = lastAccessed;
    }

    public LocalDateTime getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(LocalDateTime expiryDate) {
        this.expiryDate = expiryDate;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getEncryptionKey() {
        return encryptionKey;
    }

    public void setEncryptionKey(String encryptionKey) {
        this.encryptionKey = encryptionKey;
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    public List<String> getPermissions() {
        return permissions;
    }

    public void setPermissions(List<String> permissions) {
        this.permissions = permissions;
    }

    public List<String> getRoles() {
        return roles;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }

    public Map<String, String> getAttributes() {
        return attributes;
    }

    public void setAttributes(Map<String, String> attributes) {
        this.attributes = attributes;
    }

    public boolean isMultiFactorEnabled() {
        return multiFactorEnabled;
    }

    public void setMultiFactorEnabled(boolean multiFactorEnabled) {
        this.multiFactorEnabled = multiFactorEnabled;
    }

    public String getMfaToken() {
        return mfaToken;
    }

    public void setMfaToken(String mfaToken) {
        this.mfaToken = mfaToken;
    }

    public Integer getFailedAttempts() {
        return failedAttempts;
    }

    public void setFailedAttempts(Integer failedAttempts) {
        this.failedAttempts = failedAttempts;
    }

    public LocalDateTime getLockedUntil() {
        return lockedUntil;
    }

    public void setLockedUntil(LocalDateTime lockedUntil) {
        this.lockedUntil = lockedUntil;
    }

    public String getComplianceLevel() {
        return complianceLevel;
    }

    public void setComplianceLevel(String complianceLevel) {
        this.complianceLevel = complianceLevel;
    }

    public boolean isAuditRequired() {
        return auditRequired;
    }

    public void setAuditRequired(boolean auditRequired) {
        this.auditRequired = auditRequired;
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
} 