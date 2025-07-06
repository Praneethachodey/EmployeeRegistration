package com.example.employee.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Entity
@Table(name = "employees")
@NamedQueries({
    @NamedQuery(name = "Employee.findByDepartment", query = "SELECT e FROM Employee e WHERE e.department = :department"),
    @NamedQuery(name = "Employee.findActiveEmployees", query = "SELECT e FROM Employee e WHERE e.status = 'ACTIVE'"),
    @NamedQuery(name = "Employee.findByManager", query = "SELECT e FROM Employee e WHERE e.managerId = :managerId")
})
public class Employee {

    @Id
    private String employeeID;
    
    private String name;
    private String department;
    private String email;
    private String phone;
    private String managerId;
    private String status = "ACTIVE";
    private String securityLevel = "BASIC";
    
    @Column(name = "created_date")
    private LocalDateTime createdDate;
    
    @Column(name = "last_modified")
    private LocalDateTime lastModified;
    
    @Column(name = "version")
    private Integer version = 1;
    
    @Column(name = "access_count")
    private Integer accessCount = 0;
    
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "employee_permissions", joinColumns = @JoinColumn(name = "employee_id"))
    @Column(name = "permission")
    private List<String> permissions;
    
    @Column(name = "salary_band")
    private String salaryBand;
    
    @Column(name = "location_code")
    private String locationCode;
    
    @Column(name = "cost_center")
    private String costCenter;
    
    @Transient
    private boolean isLocked = false;
    
    @Transient
    private String sessionToken;

    // Complex business logic methods
    public boolean hasPermission(String permission) {
        return permissions != null && permissions.contains(permission);
    }
    
    public boolean canAccessSensitiveData() {
        return "ADMIN".equals(securityLevel) || "MANAGER".equals(securityLevel);
    }
    
    public void incrementAccessCount() {
        accessCount++;
    }
    
    public boolean isEligibleForPromotion() {
        return "ACTIVE".equals(status) && accessCount > 100;
    }
    
    public String generateSessionToken() {
        this.sessionToken = employeeID + "_" + System.currentTimeMillis();
        return sessionToken;
    }
    
    public boolean validateSessionToken(String token) {
        return sessionToken != null && sessionToken.equals(token);
    }

    // Getters and Setters
    public String getEmployeeID() {
        return employeeID;
    }

    public void setEmployeeID(String employeeID) {
        this.employeeID = employeeID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getManagerId() {
        return managerId;
    }

    public void setManagerId(String managerId) {
        this.managerId = managerId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getSecurityLevel() {
        return securityLevel;
    }

    public void setSecurityLevel(String securityLevel) {
        this.securityLevel = securityLevel;
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

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public Integer getAccessCount() {
        return accessCount;
    }

    public void setAccessCount(Integer accessCount) {
        this.accessCount = accessCount;
    }

    public List<String> getPermissions() {
        return permissions;
    }

    public void setPermissions(List<String> permissions) {
        this.permissions = permissions;
    }

    public String getSalaryBand() {
        return salaryBand;
    }

    public void setSalaryBand(String salaryBand) {
        this.salaryBand = salaryBand;
    }

    public String getLocationCode() {
        return locationCode;
    }

    public void setLocationCode(String locationCode) {
        this.locationCode = locationCode;
    }

    public String getCostCenter() {
        return costCenter;
    }

    public void setCostCenter(String costCenter) {
        this.costCenter = costCenter;
    }

    public boolean isLocked() {
        return isLocked;
    }

    public void setLocked(boolean locked) {
        isLocked = locked;
    }

    public String getSessionToken() {
        return sessionToken;
    }

    public void setSessionToken(String sessionToken) {
        this.sessionToken = sessionToken;
    }
}
