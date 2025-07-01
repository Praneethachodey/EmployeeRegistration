package com.example.employee.repository;

import com.example.employee.entity.HrPolicy;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.*;

import java.util.List;

@ApplicationScoped
public class HrPolicyRepository {

    @PersistenceContext(unitName = "employeePU")
    private EntityManager em;

    public List<HrPolicy> findAll() {
        return em.createQuery("SELECT p FROM HrPolicy p", HrPolicy.class)
                .getResultList();
    }
}
