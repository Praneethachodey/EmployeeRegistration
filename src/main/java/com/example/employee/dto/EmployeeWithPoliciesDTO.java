package com.example.employee.dto;

import com.example.employee.entity.Employee;
import com.example.employee.entity.HrPolicy;
import java.util.List;

public class EmployeeWithPoliciesDTO {

    private Employee employee;
    private List<HrPolicy> policies;

    public EmployeeWithPoliciesDTO() {
    }

    public EmployeeWithPoliciesDTO(Employee employee, List<HrPolicy> policies) {
        this.employee = employee;
        this.policies = policies;
    }

    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }

    public List<HrPolicy> getPolicies() {
        return policies;
    }

    public void setPolicies(List<HrPolicy> policies) {
        this.policies = policies;
    }
}
