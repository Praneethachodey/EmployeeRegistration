package com.example.employee.dto;

import com.example.employee.entity.Employee;
import com.example.employee.entity.HrPolicy;
import com.example.employee.entity.SecurityContext;
import com.example.employee.entity.AuditLog;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class EmployeeWithPoliciesDTO {
    private Employee employee;
    private List<HrPolicy> policies;
    private LocalDateTime responseTimestamp;
    private boolean cached;
    
    // Complex nested response structure
    private SecurityValidationResult securityValidation;
    private BusinessRuleValidation businessRuleValidation;
    private AuditTrail auditTrail;
    private CrossReferenceData crossReferences;
    private ResponseMetadata metadata;
    private List<ValidationError> validationErrors;
    private Map<String, Object> dynamicFields;
    
    // Nested classes for complex response structure
    public static class SecurityValidationResult {
        private boolean isAuthorized;
        private String securityLevel;
        private List<String> permissions;
        private SecurityContext context;
        private Map<String, Boolean> accessRights;
        private String validationChain;
        
        // Getters and setters
        public boolean isAuthorized() { return isAuthorized; }
        public void setAuthorized(boolean authorized) { isAuthorized = authorized; }
        public String getSecurityLevel() { return securityLevel; }
        public void setSecurityLevel(String securityLevel) { this.securityLevel = securityLevel; }
        public List<String> getPermissions() { return permissions; }
        public void setPermissions(List<String> permissions) { this.permissions = permissions; }
        public SecurityContext getContext() { return context; }
        public void setContext(SecurityContext context) { this.context = context; }
        public Map<String, Boolean> getAccessRights() { return accessRights; }
        public void setAccessRights(Map<String, Boolean> accessRights) { this.accessRights = accessRights; }
        public String getValidationChain() { return validationChain; }
        public void setValidationChain(String validationChain) { this.validationChain = validationChain; }
    }
    
    public static class BusinessRuleValidation {
        private boolean isCompliant;
        private List<String> complianceChecks;
        private Map<String, Object> businessRules;
        private String departmentValidation;
        private String policyCompliance;
        private List<String> warnings;
        private Map<String, Boolean> ruleResults;
        
        // Getters and setters
        public boolean isCompliant() { return isCompliant; }
        public void setCompliant(boolean compliant) { isCompliant = compliant; }
        public List<String> getComplianceChecks() { return complianceChecks; }
        public void setComplianceChecks(List<String> complianceChecks) { this.complianceChecks = complianceChecks; }
        public Map<String, Object> getBusinessRules() { return businessRules; }
        public void setBusinessRules(Map<String, Object> businessRules) { this.businessRules = businessRules; }
        public String getDepartmentValidation() { return departmentValidation; }
        public void setDepartmentValidation(String departmentValidation) { this.departmentValidation = departmentValidation; }
        public String getPolicyCompliance() { return policyCompliance; }
        public void setPolicyCompliance(String policyCompliance) { this.policyCompliance = policyCompliance; }
        public List<String> getWarnings() { return warnings; }
        public void setWarnings(List<String> warnings) { this.warnings = warnings; }
        public Map<String, Boolean> getRuleResults() { return ruleResults; }
        public void setRuleResults(Map<String, Boolean> ruleResults) { this.ruleResults = ruleResults; }
    }
    
    public static class AuditTrail {
        private List<AuditLog> auditLogs;
        private String processingChain;
        private Map<String, LocalDateTime> timestamps;
        private List<String> operations;
        private String transactionId;
        private Map<String, String> contextData;
        
        // Getters and setters
        public List<AuditLog> getAuditLogs() { return auditLogs; }
        public void setAuditLogs(List<AuditLog> auditLogs) { this.auditLogs = auditLogs; }
        public String getProcessingChain() { return processingChain; }
        public void setProcessingChain(String processingChain) { this.processingChain = processingChain; }
        public Map<String, LocalDateTime> getTimestamps() { return timestamps; }
        public void setTimestamps(Map<String, LocalDateTime> timestamps) { this.timestamps = timestamps; }
        public List<String> getOperations() { return operations; }
        public void setOperations(List<String> operations) { this.operations = operations; }
        public String getTransactionId() { return transactionId; }
        public void setTransactionId(String transactionId) { this.transactionId = transactionId; }
        public Map<String, String> getContextData() { return contextData; }
        public void setContextData(Map<String, String> contextData) { this.contextData = contextData; }
    }
    
    public static class CrossReferenceData {
        private Map<String, Employee> relatedEmployees;
        private Map<String, List<HrPolicy>> departmentPolicies;
        private Map<String, Object> externalData;
        private List<String> dependencies;
        private Map<String, String> references;
        
        // Getters and setters
        public Map<String, Employee> getRelatedEmployees() { return relatedEmployees; }
        public void setRelatedEmployees(Map<String, Employee> relatedEmployees) { this.relatedEmployees = relatedEmployees; }
        public Map<String, List<HrPolicy>> getDepartmentPolicies() { return departmentPolicies; }
        public void setDepartmentPolicies(Map<String, List<HrPolicy>> departmentPolicies) { this.departmentPolicies = departmentPolicies; }
        public Map<String, Object> getExternalData() { return externalData; }
        public void setExternalData(Map<String, Object> externalData) { this.externalData = externalData; }
        public List<String> getDependencies() { return dependencies; }
        public void setDependencies(List<String> dependencies) { this.dependencies = dependencies; }
        public Map<String, String> getReferences() { return references; }
        public void setReferences(Map<String, String> references) { this.references = references; }
    }
    
    public static class ResponseMetadata {
        private String version;
        private String source;
        private LocalDateTime generatedAt;
        private String processingTime;
        private Map<String, Object> configuration;
        private List<String> transformations;
        
        // Getters and setters
        public String getVersion() { return version; }
        public void setVersion(String version) { this.version = version; }
        public String getSource() { return source; }
        public void setSource(String source) { this.source = source; }
        public LocalDateTime getGeneratedAt() { return generatedAt; }
        public void setGeneratedAt(LocalDateTime generatedAt) { this.generatedAt = generatedAt; }
        public String getProcessingTime() { return processingTime; }
        public void setProcessingTime(String processingTime) { this.processingTime = processingTime; }
        public Map<String, Object> getConfiguration() { return configuration; }
        public void setConfiguration(Map<String, Object> configuration) { this.configuration = configuration; }
        public List<String> getTransformations() { return transformations; }
        public void setTransformations(List<String> transformations) { this.transformations = transformations; }
    }
    
    public static class ValidationError {
        private String field;
        private String message;
        private String severity;
        private String source;
        
        // Getters and setters
        public String getField() { return field; }
        public void setField(String field) { this.field = field; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        public String getSeverity() { return severity; }
        public void setSeverity(String severity) { this.severity = severity; }
        public String getSource() { return source; }
        public void setSource(String source) { this.source = source; }
    }
    
    // Constructor
    public EmployeeWithPoliciesDTO() {
        this.dynamicFields = new HashMap<>();
        this.responseTimestamp = LocalDateTime.now();
    }
    
    // Basic getters and setters
    public Employee getEmployee() { return employee; }
    public void setEmployee(Employee employee) { this.employee = employee; }
    public List<HrPolicy> getPolicies() { return policies; }
    public void setPolicies(List<HrPolicy> policies) { this.policies = policies; }
    public LocalDateTime getResponseTimestamp() { return responseTimestamp; }
    public void setResponseTimestamp(LocalDateTime responseTimestamp) { this.responseTimestamp = responseTimestamp; }
    public boolean isCached() { return cached; }
    public void setCached(boolean cached) { this.cached = cached; }
    
    // Complex getters and setters
    public SecurityValidationResult getSecurityValidation() { return securityValidation; }
    public void setSecurityValidation(SecurityValidationResult securityValidation) { this.securityValidation = securityValidation; }
    public BusinessRuleValidation getBusinessRuleValidation() { return businessRuleValidation; }
    public void setBusinessRuleValidation(BusinessRuleValidation businessRuleValidation) { this.businessRuleValidation = businessRuleValidation; }
    public AuditTrail getAuditTrail() { return auditTrail; }
    public void setAuditTrail(AuditTrail auditTrail) { this.auditTrail = auditTrail; }
    public CrossReferenceData getCrossReferences() { return crossReferences; }
    public void setCrossReferences(CrossReferenceData crossReferences) { this.crossReferences = crossReferences; }
    public ResponseMetadata getMetadata() { return metadata; }
    public void setMetadata(ResponseMetadata metadata) { this.metadata = metadata; }
    public List<ValidationError> getValidationErrors() { return validationErrors; }
    public void setValidationErrors(List<ValidationError> validationErrors) { this.validationErrors = validationErrors; }
    public Map<String, Object> getDynamicFields() { return dynamicFields; }
    public void setDynamicFields(Map<String, Object> dynamicFields) { this.dynamicFields = dynamicFields; }
    
    // Helper methods for complex operations
    public void addDynamicField(String key, Object value) {
        this.dynamicFields.put(key, value);
    }
    
    public void addValidationError(String field, String message, String severity, String source) {
        if (this.validationErrors == null) {
            this.validationErrors = new java.util.ArrayList<>();
        }
        ValidationError error = new ValidationError();
        error.setField(field);
        error.setMessage(message);
        error.setSeverity(severity);
        error.setSource(source);
        this.validationErrors.add(error);
    }
    
    public void addTransformation(String transformation) {
        if (this.metadata == null) {
            this.metadata = new ResponseMetadata();
        }
        if (this.metadata.getTransformations() == null) {
            this.metadata.setTransformations(new java.util.ArrayList<>());
        }
        this.metadata.getTransformations().add(transformation);
    }
}
