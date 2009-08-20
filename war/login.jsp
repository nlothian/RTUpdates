<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">


<%@page import="com.google.appengine.api.users.UserService"%>
<%@page import="com.google.appengine.api.users.UserServiceFactory"%><html>
  <head>
    <meta http-equiv="content-type" content="text/html; charset=UTF-8">
    <title>Web Conversations</title>
  </head>

  <body>
    <h1>Web Conversations</h1>


	<% 
		UserService userService = UserServiceFactory.getUserService();
	%>

	<% if (request.getUserPrincipal() != null) { %>
		Hello <%= request.getUserPrincipal().getName()%>. <a href="<%= userService.createLogoutURL(request.getRequestURI()) %>">Logout</a>
	<% } else { %>
		<a href="<%= userService.createLoginURL(request.getRequestURI()) %>">Login</a>

	<% } %>	
  </body>
</html>
