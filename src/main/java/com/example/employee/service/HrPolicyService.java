package com.example.employee.service;

import com.example.employee.dto.EmployeeWithPoliciesDTO;
import com.example.employee.entity.Employee;
import com.example.employee.entity.HrPolicy;
import com.example.employee.repository.EmployeeRepository;
import com.example.employee.repository.HrPolicyRepository;
import jakarta.inject.Inject;
import jakarta.ejb.Stateless;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Stateless
public class HrPolicyService {

    @Inject
    private EmployeeRepository employeeRepository;

    @Inject
    private HrPolicyRepository policyRepository;

    public EmployeeWithPoliciesDTO getEmployeeDetailsWithPolicies(String id, String externalPolicy) {
        Employee employee = employeeRepository.findById(id);
        List<HrPolicy> policies = policyRepository.findAllPolicies();
        
        // Create DTO using new complex structure
        EmployeeWithPoliciesDTO dto = new EmployeeWithPoliciesDTO();
        dto.setEmployee(employee);
        dto.setPolicies(policies);
        dto.setResponseTimestamp(LocalDateTime.now());
        
        // Add dynamic fields instead of old metadata
        dto.addDynamicField("externalPolicy", externalPolicy);
        dto.addDynamicField("employeeId", id);
        dto.addDynamicField("policyCount", policies.size());
        dto.addDynamicField("sourceEJB", "HrPolicyService");
        dto.addDynamicField("requestId", UUID.randomUUID().toString());
        
        // Add validation errors if needed
        if (employee == null) {
            dto.addValidationError("EMPLOYEE", "Employee not found", "ERROR", "HrPolicyService");
        }
        if (policies.isEmpty()) {
            dto.addValidationError("POLICIES", "No policies available", "WARNING", "HrPolicyService");
        }
        
        // Add transformations
        dto.addTransformation("EMPLOYEE_FETCH");
        dto.addTransformation("POLICY_RETRIEVAL");
        dto.addTransformation("DTO_POPULATION");
        
        return dto;
    }
}
