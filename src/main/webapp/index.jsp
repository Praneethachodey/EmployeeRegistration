<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>Employee Management System</title>
    <style>
        body { font-family: Arial, sans-serif; margin: 20px; }
        .nav-tabs { margin-bottom: 20px; }
        .nav-tab { 
            background-color: #007bff; color: white; padding: 10px 20px; 
            text-decoration: none; border: none; cursor: pointer; margin-right: 10px;
        }
        .nav-tab:hover { background-color: #0056b3; }
        .nav-tab.active { background-color: #0056b3; }
        .content-area { padding: 20px; border: 1px solid #ddd; border-radius: 5px; }
    </style>
</head>
<body>
    <h1>Employee Management System</h1>
    
    <!-- Navigation Tabs -->
    <div class="nav-tabs">
        <a href="register.jsp" class="nav-tab">📝 Employee Registration</a>
        <a href="employee-details.jsp" class="nav-tab">👤 Employee Details</a>
        <a href="hr-policies.jsp" class="nav-tab">📋 HR Policies</a>
    </div>

    <div class="content-area">
        <h2>Welcome to Employee Management System</h2>
        <p>Please select one of the tabs above to perform different operations:</p>
        
        <ul>
            <li><strong>Employee Registration:</strong> Register new employees with complete details and policies</li>
            <li><strong>Employee Details:</strong> Search and view employee information</li>
            <li><strong>HR Policies:</strong> Browse and search HR policies by department</li>
        </ul>
    </div>
</body>
</html> 