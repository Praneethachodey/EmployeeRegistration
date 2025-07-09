package com.example.employee.repository;

import com.example.employee.entity.Employee;
import jakarta.ejb.Stateless;
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@Stateless
public class EmployeeRepository {

    @PersistenceContext
    private EntityManager em;

    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void save(Employee employee) {
        em.persist(employee);
        em.flush();
    }

    public Employee findById(String id) {
        return em.find(Employee.class, id);
    }

    public void deleteById(String id) {
        Employee employee = findById(id);
        if (employee != null) {
            em.remove(employee);
        }
    }
}
