package com.example.employee.ejb;

import com.example.employee.dto.EmployeeWithPoliciesDTO;
import com.example.employee.entity.Employee;
import com.example.employee.service.EmployeeService;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;

@Stateless
public class EmployeeRegistrationEJB {

    @Inject
    private EmployeeService employeeService;

    @Inject
    private EmployeeDetailsEJB detailsEJB;

    public EmployeeWithPoliciesDTO registerEmployee(Employee employee) {
        employeeService.register(employee);
        return detailsEJB.fetchDetailsAndPolicies(employee.getEmployeeID());
    }
}
