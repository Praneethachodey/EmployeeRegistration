package com.example.employee.ejb;

import com.example.employee.entity.HrPolicy;
import com.example.employee.service.HrPolicyService;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;

import java.util.List;

@Stateless
public class HrPolicyEJB {

    @Inject
    private HrPolicyService hrPolicyService;

    public List<HrPolicy> getAllPolicies() {
        return hrPolicyService.getAllPolicies();
    }
}
