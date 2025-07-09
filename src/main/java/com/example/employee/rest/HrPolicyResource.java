package com.example.employee.rest;

import com.example.employee.ejb.HrPolicyEJB;
import com.example.employee.entity.HrPolicy;
import com.example.employee.service.SecurityService;
import jakarta.ejb.EJB;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

import java.util.List;

@Path("/policies") // Final endpoint: /api/policies
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class HrPolicyResource
{

    @EJB
    private HrPolicyEJB hrPolicyEJB;

    @Inject
    private SecurityService securityService;

    @GET
    public List<HrPolicy> getPoliciesByDepartment(@QueryParam("department") String department, @QueryParam("sessionId") String sessionId)
    {

        List<HrPolicy> policies;

        if (department != null && !department.trim().isEmpty())
        {
            // Get policies for specific department
            policies = hrPolicyEJB.getPoliciesByDepartment(department, sessionId);

        }
        else
        {
            // Get all policies
            policies = hrPolicyEJB.getPolicies();
        }
        return policies;

    }
}
