-- SQL Server schema for EmployeeRegistration
-- Run this script to initialize the database

-- Create the database
CREATE DATABASE EmployeeDB;
GO

-- Use the database
USE EmployeeDB;
GO

CREATE TABLE employees (
    employeeID VARCHAR(255) PRIMARY KEY,
    name VARCHAR(255),
    department VARCHAR(255),
    email VARCHAR(255),
    phone VARCHAR(255),
    managerId VARCHAR(255),
    status VARCHAR(255),
    securityLevel VARCHAR(255),
    created_date DATETIME2,
    last_modified DATETIME2,
    version INT,
    access_count INT,
    salary_band VARCHAR(255),
    location_code VARCHAR(255),
    cost_center VARCHAR(255)
);

CREATE TABLE employee_permissions (
    employee_id VARCHAR(255) NOT NULL,
    permission VARCHAR(255),
    FOREIGN KEY (employee_id) REFERENCES employees(employeeID)
);

CREATE TABLE hr_policies (
    policyId VARCHAR(255) PRIMARY KEY,
    description VARCHAR(255),
    category VARCHAR(255),
    status VARCHAR(255),
    requiredSecurityLevel VARCHAR(255),
    version VARCHAR(50),
    effective_date DATETIME2,
    expiry_date DATETIME2,
    created_by VARCHAR(255),
    created_date DATETIME2,
    last_modified DATETIME2,
    approval_status VARCHAR(255),
    priority_level INT,
    compliance_required BIT,
    audit_frequency_days INT
);

CREATE TABLE policy_departments (
    policy_id VARCHAR(255) NOT NULL,
    department VARCHAR(255),
    FOREIGN KEY (policy_id) REFERENCES hr_policies(policyId)
);

CREATE TABLE policy_conditions (
    policy_id VARCHAR(255) NOT NULL,
    condition_key VARCHAR(255),
    condition_value VARCHAR(255),
    FOREIGN KEY (policy_id) REFERENCES hr_policies(policyId)
);

CREATE TABLE audit_logs (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    employee_id VARCHAR(255),
    action VARCHAR(255),
    details VARCHAR(1000),
    timestamp DATETIME2,
    user_id VARCHAR(255),
    session_id VARCHAR(255),
    ip_address VARCHAR(255),
    source_ejb VARCHAR(255),
    transaction_id VARCHAR(255),
    security_level VARCHAR(255),
    compliance_required BIT,
    audit_level VARCHAR(255),
    encrypted_data BIT,
    retention_days INT,
    archived BIT,
    archive_date DATETIME2
);

CREATE TABLE security_contexts (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    session_id VARCHAR(255) UNIQUE,
    user_id VARCHAR(255),
    security_level VARCHAR(255),
    ip_address VARCHAR(255),
    user_agent VARCHAR(255),
    created_date DATETIME2,
    last_accessed DATETIME2,
    expiry_date DATETIME2,
    active BIT,
    encryption_key VARCHAR(255),
    signature VARCHAR(255),
    multi_factor_enabled BIT,
    mfa_token VARCHAR(255),
    failed_attempts INT,
    locked_until DATETIME2,
    compliance_level VARCHAR(255),
    audit_required BIT,
    source_ejb VARCHAR(255),
    transaction_id VARCHAR(255)
);

CREATE TABLE security_permissions (
    security_context_id BIGINT NOT NULL,
    permission VARCHAR(255),
    FOREIGN KEY (security_context_id) REFERENCES security_contexts(id)
);

CREATE TABLE security_roles (
    security_context_id BIGINT NOT NULL,
    role VARCHAR(255),
    FOREIGN KEY (security_context_id) REFERENCES security_contexts(id)
);

CREATE TABLE security_attributes (
    security_context_id BIGINT NOT NULL,
    attribute_key VARCHAR(255),
    attribute_value VARCHAR(255),
    FOREIGN KEY (security_context_id) REFERENCES security_contexts(id)
); 