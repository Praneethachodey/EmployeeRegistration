package com.example.employee.controller;

import com.example.employee.entity.HrPolicy;
import com.example.employee.ejb.HrPolicyEJB;
import com.example.employee.service.SecurityService;
import jakarta.ejb.EJB;
import jakarta.inject.Inject;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

public class HrPolicyServlet extends HttpServlet {
    
    @EJB
    private HrPolicyEJB hrPolicyEJB;
    
    @Inject
    private SecurityService securityService;
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        try {
            String department = request.getParameter("department");
            
            // Create a session ID for this request
            String sessionId = UUID.randomUUID().toString();
            
            // Create security context
            securityService.createSecurityContext("WEB_USER", "BASIC", sessionId);
            
            List<HrPolicy> policies;
            
            if (department != null && !department.trim().isEmpty()) {
                // Get policies for specific department
                policies = hrPolicyEJB.getPoliciesByDepartment(department, sessionId);
                request.setAttribute("department", department);
            } else {
                // Get all policies
                policies = hrPolicyEJB.getPolicies();
            }
            
            request.setAttribute("policies", policies);
            request.setAttribute("message", "Policies fetched successfully!");
            
            // Forward to result page
            request.getRequestDispatcher("/hr-policies.jsp").forward(request, response);
            
        } catch (Exception e) {
            // Set error message
            request.setAttribute("error", "Failed to fetch policies: " + e.getMessage());
            request.getRequestDispatcher("/hr-policies.jsp").forward(request, response);
        }
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        // Redirect to GET method
        doGet(request, response);
    }
} 