package com.example.employee.repository;

import com.example.employee.entity.Employee;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.*;

@ApplicationScoped
public class EmployeeRepository {

    @PersistenceContext(unitName = "employeePU")
    private EntityManager em;

    public void save(Employee employee) {
        em.persist(employee);
    }

    public Employee findById(Integer id) {
        return em.find(Employee.class, id);
    }
}
