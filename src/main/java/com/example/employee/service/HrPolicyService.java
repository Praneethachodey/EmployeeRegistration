package com.example.employee.service;

import com.example.employee.entity.HrPolicy;
import com.example.employee.repository.HrPolicyRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.List;

@ApplicationScoped
public class HrPolicyService {

    @Inject
    private HrPolicyRepository policyRepository;

    public List<HrPolicy> getAllPolicies() {
        return policyRepository.findAll();
    }
}
