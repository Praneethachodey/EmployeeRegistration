package com.example.employee.ejb;

import com.example.employee.dto.EmployeeWithPoliciesDTO;
import com.example.employee.entity.Employee;
import com.example.employee.entity.HrPolicy;
import com.example.employee.service.EmployeeService;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;

import java.util.List;

@Stateless
public class EmployeeDetailsEJB {

    @Inject
    private EmployeeService employeeService;

    @Inject
    private HrPolicyEJB hrPolicyEJB;

    public EmployeeWithPoliciesDTO fetchDetailsAndPolicies(Integer employeeId) {
        Employee employee = employeeService.getById(employeeId);
        List<HrPolicy> policies = hrPolicyEJB.getAllPolicies();
        return new EmployeeWithPoliciesDTO(employee, policies);
    }
}
