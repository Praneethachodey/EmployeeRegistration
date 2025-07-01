package com.example.employee.controller;

import com.example.employee.dto.EmployeeWithPoliciesDTO;
import com.example.employee.ejb.EmployeeDetailsEJB;
import jakarta.ejb.EJB;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.io.PrintWriter;

@WebServlet("/employee")
public class FetchEmployeeServlet extends HttpServlet {

    @EJB
    private EmployeeDetailsEJB detailsEJB;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        int empId = Integer.parseInt(req.getParameter("empId"));
        EmployeeWithPoliciesDTO dto = detailsEJB.fetchDetailsAndPolicies(empId);

        resp.setContentType("text/html");
        PrintWriter out = resp.getWriter();

        out.println("<html><body>");
        out.println("<h2>Employee Details</h2>");
        out.println("<p>ID: " + dto.getEmployee().getEmployeeID() + "</p>");
        out.println("<p>Name: " + dto.getEmployee().getFirstName() + " " + dto.getEmployee().getLastName() + "</p>");
        out.println("<p>Email: " + dto.getEmployee().getEmail() + "</p>");
        out.println("<h3>HR Policies</h3><ul>");
        dto.getPolicies().forEach(policy ->
                out.println("<li><strong>" + policy.getPolicyName() + ":</strong> " + policy.getDescription() + "</li>")
        );
        out.println("</ul></body></html>");
    }
}
