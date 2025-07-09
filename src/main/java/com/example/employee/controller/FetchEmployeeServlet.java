package com.example.employee.controller;

import com.example.employee.dto.EmployeeWithPoliciesDTO;
import com.example.employee.ejb.EmployeeDetailsEJB;
import com.example.employee.service.SecurityService;
import jakarta.ejb.EJB;
import jakarta.inject.Inject;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.UUID;

public class FetchEmployeeServlet extends HttpServlet
{

    @EJB
    private EmployeeDetailsEJB employeeDetailsEJB;

    @Inject
    private SecurityService securityService;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {

        try
        {
            String employeeId = request.getParameter("employeeId");

            if (employeeId == null || employeeId.trim().isEmpty())
            {
                request.setAttribute("error", "Employee ID is required");
                request.getRequestDispatcher("/register.jsp").forward(request, response);
                return;
            }

            // Create a session ID for this request
            String sessionId = UUID.randomUUID().toString();

            // Create security context
            securityService.createSecurityContext("WEB_USER", "BASIC", sessionId);

            // Fetch employee details with policies
            EmployeeWithPoliciesDTO employeeWithPolicies = employeeDetailsEJB.getEmployeeWithPolicies(employeeId, sessionId);

            if (employeeWithPolicies != null && employeeWithPolicies.getEmployee() != null)
            {
                // Set employee data
                request.setAttribute("employee", employeeWithPolicies.getEmployee());
                request.setAttribute("policies", employeeWithPolicies.getPolicies());
                request.setAttribute("message", "Employee details fetched successfully!");
            }
            else
            {
                request.setAttribute("error", "Employee not found: " + employeeId);
            }

            // Forward to result page
            request.getRequestDispatcher("/employee-details.jsp").forward(request, response);

        }
        catch (Exception e)
        {
            // Set error message
            request.setAttribute("error", "Failed to fetch employee: " + e.getMessage());
            request.getRequestDispatcher("/register.jsp").forward(request, response);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        String action = request.getParameter("action");
        if ("update".equalsIgnoreCase(action))
        {
            try
            {
                String employeeId = request.getParameter("employeeId");
                String name = request.getParameter("name");
                String department = request.getParameter("department");
                String email = request.getParameter("email");
                String phone = request.getParameter("phone");
                String sessionId = UUID.randomUUID().toString();
                securityService.createSecurityContext("WEB_USER", "BASIC", sessionId);

                // Fetch updated details
                EmployeeWithPoliciesDTO employeeWithPolicies = employeeDetailsEJB.updateEmployee(employeeId, name, department, email, phone, sessionId);
                if (employeeWithPolicies != null && employeeWithPolicies.getEmployee() != null)
                {
                    request.setAttribute("employee", employeeWithPolicies.getEmployee());
                    request.setAttribute("policies", employeeWithPolicies.getPolicies());
                    request.setAttribute("message", "Employee updated successfully!");
                }
                else
                {
                    request.setAttribute("error", "Employee not found after update: " + employeeId);
                }
                request.getRequestDispatcher("/employee-details.jsp").forward(request, response);
            }
            catch (Exception e)
            {
                request.setAttribute("error", "Failed to update employee: " + e.getMessage());
                request.getRequestDispatcher("/employee-details.jsp").forward(request, response);
            }
        }
        else if ("delete".equalsIgnoreCase(action))
        {
            try
            {
                String employeeId = request.getParameter("employeeId");
                String sessionId = UUID.randomUUID().toString();
                securityService.createSecurityContext("WEB_USER", "BASIC", sessionId);
                // Call EJB to delete
                employeeDetailsEJB.deleteEmployee(employeeId, sessionId);
                request.setAttribute("message", "Employee deleted successfully!");
                request.getRequestDispatcher("/employee-details.jsp").forward(request, response);
            }
            catch (Exception e)
            {
                request.setAttribute("error", "Failed to delete employee: " + e.getMessage());
                request.getRequestDispatcher("/employee-details.jsp").forward(request, response);
            }
        }
        else
        {
            doGet(request, response);
        }
    }
}
