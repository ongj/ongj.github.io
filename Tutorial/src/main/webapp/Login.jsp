<%-- 
    Document   : Login
    Created on : Feb 1, 2016, 3:28:55 PM
    Author     : JVO
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Login Page</title>
    </head>
    <body>
        <form method="post" action="Login">
            Username:<input type="text" name="username" /><br/>
            Password:<input type="password" name="pass" /><br/>
        <input type="submit" name="login" value="Login" />
        </form>
    </body>
</html>
