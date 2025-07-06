package com.example.employee.repository;

import com.example.employee.entity.HrPolicy;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import java.util.List;

@Stateless
public class HrPolicyRepository {

    @PersistenceContext
    private EntityManager em;

    public List<HrPolicy> findAllPolicies() {
        return em.createQuery("SELECT p FROM HrPolicy p", HrPolicy.class).getResultList();
    }
    
    public HrPolicy findPolicyById(String policyId) {
        TypedQuery<HrPolicy> query = em.createQuery("SELECT p FROM HrPolicy p WHERE p.policyId = :policyId", HrPolicy.class);
        query.setParameter("policyId", policyId);
        List<HrPolicy> results = query.getResultList();
        return results.isEmpty() ? null : results.get(0);
    }
    
    public void updatePolicy(HrPolicy policy) {
        em.merge(policy);
    }
    
    public void savePolicy(HrPolicy policy) {
        em.persist(policy);
    }
    
    public void deletePolicy(String policyId) {
        HrPolicy policy = findPolicyById(policyId);
        if (policy != null) {
            em.remove(policy);
        }
    }
}
