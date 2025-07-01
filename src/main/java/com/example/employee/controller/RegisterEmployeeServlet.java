package com.example.employee.controller;

import com.example.employee.dto.EmployeeWithPoliciesDTO;
import com.example.employee.ejb.EmployeeRegistrationEJB;
import com.example.employee.entity.Employee;

import jakarta.ejb.EJB;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDate;

@WebServlet("/register")
public class RegisterEmployeeServlet extends HttpServlet {

    @EJB
    private EmployeeRegistrationEJB registrationEJB;

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        Employee emp = new Employee();
        emp.setFirstName(req.getParameter("firstName"));
        emp.setLastName(req.getParameter("lastName"));
        emp.setAge(Integer.parseInt(req.getParameter("age")));
        emp.setDateOfBirth(LocalDate.parse(req.getParameter("dateOfBirth")));
        emp.setPlace(req.getParameter("place"));
        emp.setEmail(req.getParameter("email"));
        emp.setPhoneNumber(req.getParameter("phoneNumber"));

        EmployeeWithPoliciesDTO dto = registrationEJB.registerEmployee(emp);

        resp.setContentType("text/html");
        PrintWriter out = resp.getWriter();

        out.println("<html><body>");
        out.println("<h2>Employee Registered</h2>");
        out.println("<p>Name: " + dto.getEmployee().getFirstName() + " " + dto.getEmployee().getLastName() + "</p>");
        out.println("<p>Email: " + dto.getEmployee().getEmail() + "</p>");
        out.println("<h3>HR Policies</h3><ul>");
        dto.getPolicies().forEach(policy ->
                out.println("<li><strong>" + policy.getPolicyName() + ":</strong> " + policy.getDescription() + "</li>")
        );
        out.println("</ul></body></html>");
    }
}
