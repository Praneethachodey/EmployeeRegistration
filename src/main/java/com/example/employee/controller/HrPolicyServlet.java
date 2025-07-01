package com.example.employee.controller;

import com.example.employee.ejb.HrPolicyEJB;
import com.example.employee.entity.HrPolicy;

import jakarta.ejb.EJB;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

@WebServlet("/policies")
public class HrPolicyServlet extends HttpServlet {

    @EJB
    private HrPolicyEJB policyEJB;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        List<HrPolicy> policies = policyEJB.getAllPolicies();

        resp.setContentType("text/html");
        PrintWriter out = resp.getWriter();

        out.println("<html><body>");
        out.println("<h2>All HR Policies</h2><ul>");
        policies.forEach(policy ->
                out.println("<li><strong>" + policy.getPolicyName() + ":</strong> " + policy.getDescription() + "</li>")
        );
        out.println("</ul></body></html>");
    }
}
