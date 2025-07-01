<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>Register Employee</title>
</head>
<body>
<h2>Employee Registration Form</h2>
<form action="register" method="post">
    <label>First Name:</label><br>
    <input type="text" name="firstName" required><br><br>

    <label>Last Name:</label><br>
    <input type="text" name="lastName" required><br><br>

    <label>Age:</label><br>
    <input type="number" name="age" required><br><br>

    <label>Date of Birth (YYYY-MM-DD):</label><br>
    <input type="text" name="dateOfBirth" required><br><br>

    <label>Place:</label><br>
    <input type="text" name="place" required><br><br>

    <label>Email:</label><br>
    <input type="email" name="email" required><br><br>

    <label>Phone Number:</label><br>
    <input type="text" name="phoneNumber" required><br><br>

    <input type="submit" value="Register">
</form>
</body>
</html>
