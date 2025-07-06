package com.example.employee.service;

import com.example.employee.entity.Employee;
import com.example.employee.repository.EmployeeRepository;
import jakarta.inject.Inject;
import jakarta.ejb.Stateless;

@Stateless
public class EmployeeService {

    @Inject
    private EmployeeRepository repository;

    public void register(Employee employee) {
        repository.save(employee);
    }

    public Employee find(String id) {
        return repository.findById(id);
    }

    public void delete(String id) {
        repository.deleteById(id);
    }
}
