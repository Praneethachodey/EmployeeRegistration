<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>Register Employee</title>
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
        .form-btn { background: #007bff; color: #fff; border: none; padding: 10px 22px; border-radius: 4px; font-size: 1rem; cursor: pointer; }
        .form-btn:hover { background: #0056b3; }
        .message { padding: 12px; border-radius: 4px; margin-bottom: 18px; font-size: 1rem; }
        .success { background: #d4edda; color: #155724; border: 1px solid #c3e6cb; }
        .error { background: #f8d7da; color: #721c24; border: 1px solid #f5c6cb; }
        @media (max-width: 700px) { .container { padding: 12px; } }
    </style>
</head>
<body>
    <div class="container">
        <div class="nav-tabs">
            <a href="register.jsp" class="nav-tab active">📝 Employee Registration</a>
            <a href="employee-details.jsp" class="nav-tab">👤 Employee Details</a>
            <a href="hr-policies.jsp" class="nav-tab">📋 HR Policies</a>
        </div>
        <h1>Employee Management System</h1>
        <div class="section">
            <h2>📝 Employee Registration Form</h2>
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
            <form action="register" method="post">
                <label class="form-label">Employee ID</label>
                <input class="form-input" type="text" name="employeeId" required>
                <label class="form-label">Name</label>
                <input class="form-input" type="text" name="name" required>
                <label class="form-label">Department</label>
                <select class="form-input" name="department" required>
                    <option value="">Select Department</option>
                    <option value="IT">IT</option>
                    <option value="HR">HR</option>
                    <option value="Finance">Finance</option>
                    <option value="Marketing">Marketing</option>
                    <option value="Sales">Sales</option>
                </select>
                <label class="form-label">Email</label>
                <input class="form-input" type="email" name="email" required>
                <label class="form-label">Phone</label>
                <input class="form-input" type="text" name="phone" required>
                <button class="form-btn" type="submit">Register</button>
            </form>
        </div>
    </div>
</body>
</html>
