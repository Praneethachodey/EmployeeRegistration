<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>HR Policies</title>
    <style>
        body { font-family: Arial, sans-serif; margin: 20px; }
        .nav-tabs { margin-bottom: 20px; }
        .nav-tab { 
            background-color: #007bff; color: white; padding: 10px 20px; 
            text-decoration: none; border: none; cursor: pointer; margin-right: 10px;
        }
        .nav-tab:hover { background-color: #0056b3; }
        .nav-tab.active { background-color: #0056b3; }
        .search-form { margin: 20px 0; padding: 15px; background-color: #f8f9fa; border-radius: 5px; }
        .search-form select { padding: 8px; margin-right: 10px; width: 200px; }
        .search-form button { padding: 8px 15px; background-color: #28a745; color: white; border: none; cursor: pointer; }
        .policies-section { margin-top: 20px; }
        .policy-item { border: 1px solid #ddd; padding: 15px; margin: 10px 0; background-color: white; border-radius: 5px; }
        .policy-header { background-color: #f8f9fa; padding: 10px; margin: -15px -15px 10px -15px; border-radius: 5px 5px 0 0; }
    </style>
</head>
<body>
    <h1>Employee Management System</h1>
    
    <!-- Navigation Tabs -->
    <div class="nav-tabs">
        <a href="register.jsp" class="nav-tab">📝 Employee Registration</a>
        <a href="employee-details.jsp" class="nav-tab">👤 Employee Details</a>
        <a href="hr-policies.jsp" class="nav-tab active">📋 HR Policies</a>
    </div>

    <h2>📋 HR Policies Browser</h2>

    <% if (request.getAttribute("message") != null) { %>
        <div style="color: green; background-color: #d4edda; padding: 10px; margin: 10px 0; border: 1px solid #c3e6cb;">
            <%= request.getAttribute("message") %>
        </div>
    <% } %>

    <% if (request.getAttribute("error") != null) { %>
        <div style="color: red; background-color: #f8d7da; padding: 10px; margin: 10px 0; border: 1px solid #f5c6cb;">
            <%= request.getAttribute("error") %>
        </div>
    <% } %>

    <!-- Search Form -->
    <div class="search-form">
        <h3>Search Policies by Department</h3>
        <form action="policies" method="get">
            <select name="department">
                <option value="">All Departments</option>
                <option value="IT">IT</option>
                <option value="HR">HR</option>
                <option value="Finance">Finance</option>
                <option value="Marketing">Marketing</option>
                <option value="Sales">Sales</option>
            </select>
            <button type="submit">🔍 Get Policies</button>
        </form>
    </div>

    <!-- Policies Display -->
    <% if (request.getAttribute("policies") != null) { %>
        <div class="policies-section">
            <h3>📋 HR Policies</h3>
            
            <% if (request.getAttribute("department") != null) { %>
                <p><strong>Department:</strong> <%= request.getAttribute("department") %></p>
            <% } else { %>
                <p><strong>All Departments</strong></p>
            <% } %>

            <% 
            java.util.List<com.example.employee.entity.HrPolicy> policies = 
                (java.util.List<com.example.employee.entity.HrPolicy>) request.getAttribute("policies");
            if (policies != null && !policies.isEmpty()) {
                for (com.example.employee.entity.HrPolicy policy : policies) {
            %>
                <div class="policy-item">
                    <div class="policy-header">
                        <strong>Policy ID:</strong> <%= policy.getPolicyId() %> | 
                        <strong>Category:</strong> <%= policy.getCategory() %> | 
                        <strong>Status:</strong> <%= policy.getStatus() %>
                    </div>
                    
                    <strong>Description:</strong> <%= policy.getDescription() %><br><br>
                    
                    <strong>Security Level:</strong> <%= policy.getRequiredSecurityLevel() %><br>
                    <strong>Version:</strong> <%= policy.getVersion() %><br>
                    <strong>Effective Date:</strong> <%= policy.getEffectiveDate() %><br>
                    <strong>Priority Level:</strong> <%= policy.getPriorityLevel() %><br>
                    <strong>Compliance Required:</strong> <%= policy.isComplianceRequired() ? "Yes" : "No" %><br>
                    <strong>Audit Frequency:</strong> <%= policy.getAuditFrequencyDays() %> days
                </div>
            <%
                }
            } else {
            %>
                <div style="padding: 20px; background-color: #f8f9fa; border-radius: 5px; text-align: center;">
                    <p>No policies found for the selected criteria.</p>
                </div>
            <%
            }
            %>
        </div>
    <% } else { %>
        <div style="padding: 20px; background-color: #f8f9fa; border-radius: 5px; text-align: center;">
            <p>Select a department above to view HR policies.</p>
        </div>
    <% } %>
</body>
</html> 