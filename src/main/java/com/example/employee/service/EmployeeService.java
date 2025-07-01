package com.example.employee.service;

import com.example.employee.entity.Employee;
import com.example.employee.repository.EmployeeRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class EmployeeService {

    @Inject
    private EmployeeRepository employeeRepository;

    public void register(Employee employee) {
        employeeRepository.save(employee);
    }

    public Employee getById(Integer id) {
        return employeeRepository.findById(id);
    }
}
