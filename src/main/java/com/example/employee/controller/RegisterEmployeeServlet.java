package com.example.employee.controller;

import com.example.employee.entity.Employee;
import com.example.employee.dto.EmployeeWithPoliciesDTO;
import com.example.employee.ejb.RegisterEmployeeEJB;
import com.example.employee.service.SecurityService;
import jakarta.ejb.EJB;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.UUID;

public class RegisterEmployeeServlet extends HttpServlet {
    
    @EJB
    private RegisterEmployeeEJB registerEJB;
    
    @EJB
    private SecurityService securityService;
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        try {
            // Create a session ID for this request
            String sessionId = UUID.randomUUID().toString();
            
            // Create security context
            securityService.createSecurityContext("WEB_USER", "BASIC", sessionId);
            
            // Get form parameters
            String employeeId = request.getParameter("employeeId");
            String name = request.getParameter("name");
            String department = request.getParameter("department");
            String email = request.getParameter("email");
            String phone = request.getParameter("phone");
            
            // Create employee object
            Employee employee = new Employee();
            employee.setEmployeeID(employeeId);
            employee.setName(name);
            employee.setDepartment(department);
            employee.setEmail(email);
            employee.setPhone(phone);
            employee.setCreatedDate(LocalDateTime.now());
            employee.setLastModified(LocalDateTime.now());
            employee.setStatus("ACTIVE");
            employee.setSecurityLevel("BASIC");
            
            // Register employee and get complete details with policies
            EmployeeWithPoliciesDTO employeeWithPolicies = registerEJB.registerEmployee(employee, sessionId);
            
            // Set success message
            request.setAttribute("message", "Employee registered successfully!");
            request.setAttribute("employeeId", employeeId);
            
            // Set the complete employee data with policies
            request.setAttribute("employee", employeeWithPolicies.getEmployee());
            request.setAttribute("policies", employeeWithPolicies.getPolicies());
            request.setAttribute("responseTimestamp", employeeWithPolicies.getResponseTimestamp());
            request.setAttribute("isCached", employeeWithPolicies.isCached());
            
            // Forward to editable employee details page
            request.getRequestDispatcher("/employee-details.jsp").forward(request, response);
            
        } catch (Exception e) {
            // Set error message
            request.setAttribute("error", "Registration failed: " + e.getMessage());
            request.getRequestDispatcher("/register.jsp").forward(request, response);
        }
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        // Forward to registration form
        request.getRequestDispatcher("/register.jsp").forward(request, response);
    }
} 