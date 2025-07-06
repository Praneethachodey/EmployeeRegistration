package com.example.employee.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Entity
@Table(name = "hr_policies")
@NamedQueries({
    @NamedQuery(name = "HrPolicy.findByCategory", query = "SELECT p FROM HrPolicy p WHERE p.category = :category"),
    @NamedQuery(name = "HrPolicy.findActivePolicies", query = "SELECT p FROM HrPolicy p WHERE p.status = 'ACTIVE'"),
    @NamedQuery(name = "HrPolicy.findBySecurityLevel", query = "SELECT p FROM HrPolicy p WHERE p.requiredSecurityLevel = :securityLevel")
})
public class HrPolicy {

    @Id
    private String policyId;
    
    private String description;
    private String category;
    private String status = "ACTIVE";
    private String requiredSecurityLevel = "BASIC";
    private String version = "1.0";
    
    @Column(name = "effective_date")
    private LocalDateTime effectiveDate;
    
    @Column(name = "expiry_date")
    private LocalDateTime expiryDate;
    
    @Column(name = "created_by")
    private String createdBy;
    
    @Column(name = "created_date")
    private LocalDateTime createdDate;
    
    @Column(name = "last_modified")
    private LocalDateTime lastModified;
    
    @Column(name = "approval_status")
    private String approvalStatus = "PENDING";
    
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "policy_departments", joinColumns = @JoinColumn(name = "policy_id"))
    @Column(name = "department")
    private List<String> applicableDepartments;
    
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "policy_conditions", joinColumns = @JoinColumn(name = "policy_id"))
    @MapKeyColumn(name = "condition_key")
    @Column(name = "condition_value")
    private Map<String, String> conditions = new java.util.HashMap<>();
    
    @Column(name = "priority_level")
    private Integer priorityLevel = 1;
    
    @Column(name = "compliance_required")
    private boolean complianceRequired = false;
    
    @Column(name = "audit_frequency_days")
    private Integer auditFrequencyDays = 30;
    
    @Transient
    private boolean isCached = false;
    
    @Transient
    private LocalDateTime lastAccessed;
    
    @Transient
    private int accessCount = 0;

    // Complex business logic methods
    public boolean isApplicableForDepartment(String department) {
        return applicableDepartments != null && applicableDepartments.contains(department);
    }
    
    public boolean isActive() {
        LocalDateTime now = LocalDateTime.now();
        return "ACTIVE".equals(status) && 
               (effectiveDate == null || effectiveDate.isBefore(now)) &&
               (expiryDate == null || expiryDate.isAfter(now));
    }
    
    public boolean requiresSecurityLevel(String userSecurityLevel) {
        return "ADMIN".equals(requiredSecurityLevel) || 
               ("MANAGER".equals(requiredSecurityLevel) && !"BASIC".equals(userSecurityLevel));
    }
    
    public void incrementAccessCount() {
        accessCount++;
        lastAccessed = LocalDateTime.now();
    }
    
    public boolean needsAudit() {
        return lastAccessed != null && 
               LocalDateTime.now().minusDays(auditFrequencyDays).isAfter(lastAccessed);
    }
    
    public String getConditionValue(String key) {
        return conditions.get(key);
    }
    
    public void addCondition(String key, String value) {
        conditions.put(key, value);
    }
    
    public boolean isComplianceRequired() {
        return complianceRequired;
    }
    
    public void setComplianceRequired(boolean complianceRequired) {
        this.complianceRequired = complianceRequired;
    }

    // Getters and Setters
    public String getPolicyId() {
        return policyId;
    }

    public void setPolicyId(String policyId) {
        this.policyId = policyId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getRequiredSecurityLevel() {
        return requiredSecurityLevel;
    }

    public void setRequiredSecurityLevel(String requiredSecurityLevel) {
        this.requiredSecurityLevel = requiredSecurityLevel;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public LocalDateTime getEffectiveDate() {
        return effectiveDate;
    }

    public void setEffectiveDate(LocalDateTime effectiveDate) {
        this.effectiveDate = effectiveDate;
    }

    public LocalDateTime getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(LocalDateTime expiryDate) {
        this.expiryDate = expiryDate;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public LocalDateTime getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(LocalDateTime createdDate) {
        this.createdDate = createdDate;
    }

    public LocalDateTime getLastModified() {
        return lastModified;
    }

    public void setLastModified(LocalDateTime lastModified) {
        this.lastModified = lastModified;
    }

    public String getApprovalStatus() {
        return approvalStatus;
    }

    public void setApprovalStatus(String approvalStatus) {
        this.approvalStatus = approvalStatus;
    }

    public List<String> getApplicableDepartments() {
        return applicableDepartments;
    }

    public void setApplicableDepartments(List<String> applicableDepartments) {
        this.applicableDepartments = applicableDepartments;
    }

    public Map<String, String> getConditions() {
        return conditions;
    }

    public void setConditions(Map<String, String> conditions) {
        this.conditions = conditions;
    }

    public Integer getPriorityLevel() {
        return priorityLevel;
    }

    public void setPriorityLevel(Integer priorityLevel) {
        this.priorityLevel = priorityLevel;
    }

    public Integer getAuditFrequencyDays() {
        return auditFrequencyDays;
    }

    public void setAuditFrequencyDays(Integer auditFrequencyDays) {
        this.auditFrequencyDays = auditFrequencyDays;
    }

    public boolean isCached() {
        return isCached;
    }

    public void setCached(boolean cached) {
        isCached = cached;
    }

    public LocalDateTime getLastAccessed() {
        return lastAccessed;
    }

    public void setLastAccessed(LocalDateTime lastAccessed) {
        this.lastAccessed = lastAccessed;
    }

    public int getAccessCount() {
        return accessCount;
    }

    public void setAccessCount(int accessCount) {
        this.accessCount = accessCount;
    }
}
