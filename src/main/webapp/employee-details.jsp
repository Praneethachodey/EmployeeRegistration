<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>Employee Details</title>
    <style>
        body { font-family: Arial, sans-serif; margin: 0; background: #f4f6f8; }
        .container { max-width: 600px; margin: 40px auto; background: #fff; border-radius: 8px; box-shadow: 0 2px 8px #0001; padding: 32px; }
        .nav-tabs { margin-bottom: 30px; text-align: center; }
        .nav-tab { 
            background-color: #007bff; color: white; padding: 10px 24px; 
            text-decoration: none; border: none; cursor: pointer; margin: 0 6px; border-radius: 4px 4px 0 0;
            display: inline-block;
        }
        .nav-tab:hover, .nav-tab.active { background-color: #0056b3; }
        h1 { text-align: center; margin-bottom: 10px; }
        h2 { margin-top: 0; }
        .section { margin-bottom: 28px; }
        .form-label { display: block; margin-bottom: 6px; font-weight: bold; }
        .form-input { width: 100%; padding: 8px; margin-bottom: 16px; border: 1px solid #ccc; border-radius: 4px; }
        .form-btn { background: #28a745; color: #fff; border: none; padding: 10px 22px; border-radius: 4px; font-size: 1rem; cursor: pointer; }
        .form-btn:hover { background: #218838; }
        .form-btn.delete { background: #dc3545; }
        .form-btn.delete:hover { background: #c82333; }
        .message { padding: 12px; border-radius: 4px; margin-bottom: 18px; font-size: 1rem; }
        .success { background: #d4edda; color: #155724; border: 1px solid #c3e6cb; }
        .error { background: #f8d7da; color: #721c24; border: 1px solid #f5c6cb; }
        .employee-details, .policy-list { background: #f9f9f9; border-radius: 6px; padding: 18px; margin-bottom: 18px; }
        .policy-item { border-bottom: 1px solid #eee; padding: 10px 0; }
        .policy-item:last-child { border-bottom: none; }
        .section-title { margin-bottom: 12px; color: #007bff; font-size: 1.2rem; }
        @media (max-width: 700px) { .container { padding: 12px; } }
    </style>
</head>
<body>
    <div class="container">
        <div class="nav-tabs">
            <a href="register.jsp" class="nav-tab">📝 Employee Registration</a>
            <a href="employee-details.jsp" class="nav-tab active">👤 Employee Details</a>
            <a href="hr-policies.jsp" class="nav-tab">📋 HR Policies</a>
        </div>
        <h1>Employee Management System</h1>
        <div class="section">
            <h2>👤 Employee Details Search</h2>
            <% if (request.getAttribute("message") != null) { %>
                <div class="message success">
                    <%= request.getAttribute("message") %>
                </div>
            <% } %>
            <% if (request.getAttribute("error") != null) { %>
                <div class="message error">
                    <%= request.getAttribute("error") %>
                </div>
            <% } %>
            <form class="section" action="employee" method="get">
                <label class="form-label">Search Employee by ID</label>
                <input class="form-input" type="text" name="employeeId" placeholder="Enter Employee ID" required>
                <button class="form-btn" type="submit">🔍 Search Employee</button>
            </form>
        </div>
        <% if (request.getAttribute("employee") != null) { 
            com.example.employee.entity.Employee emp = (com.example.employee.entity.Employee)request.getAttribute("employee"); %>
            <div class="section employee-details">
                <div class="section-title">Edit Employee Information</div>
                <form action="employee" method="post">
                    <input type="hidden" name="action" value="update">
                    <input type="hidden" name="employeeId" value="<%= emp.getEmployeeID() %>">
                    <label class="form-label">Name</label>
                    <input class="form-input" type="text" name="name" value="<%= emp.getName() %>">
                    <label class="form-label">Department</label>
                    <input class="form-input" type="text" name="department" value="<%= emp.getDepartment() %>">
                    <label class="form-label">Email</label>
                    <input class="form-input" type="email" name="email" value="<%= emp.getEmail() %>">
                    <label class="form-label">Phone</label>
                    <input class="form-input" type="text" name="phone" value="<%= emp.getPhone() %>">
                    <button class="form-btn" type="submit">Update</button>
                </form>
                <form action="employee" method="post" style="margin-top: 10px;">
                    <input type="hidden" name="action" value="delete">
                    <input type="hidden" name="employeeId" value="<%= emp.getEmployeeID() %>">
                    <button class="form-btn delete" type="submit" onclick="return confirm('Are you sure you want to delete this employee? This action cannot be undone.')">🗑️ Delete Employee</button>
                </form>
                <div style="margin-top: 10px;">
                    <strong>Status:</strong> <%= emp.getStatus() %> &nbsp; | &nbsp;
                    <strong>Security Level:</strong> <%= emp.getSecurityLevel() %>
                </div>
            </div>
            <% if (request.getAttribute("policies") != null) { %>
                <div class="section policy-list">
                    <div class="section-title">HR Policies for <%= emp.getDepartment() %> Department</div>
                    <% 
                    java.util.List<com.example.employee.entity.HrPolicy> policies = 
                        (java.util.List<com.example.employee.entity.HrPolicy>) request.getAttribute("policies");
                    if (policies != null && !policies.isEmpty()) {
                        for (com.example.employee.entity.HrPolicy policy : policies) {
                    %>
                        <div class="policy-item">
                            <strong>Policy ID:</strong> <%= policy.getPolicyId() %><br>
                            <strong>Category:</strong> <%= policy.getCategory() %><br>
                            <strong>Description:</strong> <%= policy.getDescription() %><br>
                            <strong>Status:</strong> <%= policy.getStatus() %><br>
                            <strong>Security Level:</strong> <%= policy.getRequiredSecurityLevel() %><br>
                            <strong>Version:</strong> <%= policy.getVersion() %><br>
                            <strong>Effective Date:</strong> <%= policy.getEffectiveDate() %><br>
                            <strong>Priority Level:</strong> <%= policy.getPriorityLevel() %>
                        </div>
                    <%
                        }
                    } else { %>
                        <p>No policies found for this department.</p>
                    <% } %>
                </div>
            <% } %>
        <% } %>
    </div>
</body>
</html> 