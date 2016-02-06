<%-- 
    Document   : Welcome
    Created on : Jan 28, 2016, 9:47:06 PM
    Author     : JVO
--%>

<%@page import="Bean.User"%>

<%
    User u = (User) session.getAttribute("userInfo");
%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Home Page</title>
    </head>
    <body>
        <h1>Welcome <% out.print(u.getFirstname());%> <% out.print(u.getLastname());%></h1> <br />
        
        <p><a href="Edit.html"> Edit Name</p> <br />
        <p><a href="Logout"> Logout</p>
        
    </body>
</html>
