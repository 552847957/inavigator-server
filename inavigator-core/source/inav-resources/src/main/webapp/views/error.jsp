<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>

<%@page import="java.lang.Throwable"%>
<%@page import="java.io.PrintWriter"%>

<% 
    Throwable t = (Throwable) pageContext.findAttribute("exception");
%>
    
<h2>В работе приложения произошла ошибка: "<%=t.getMessage() %>".</h2>

<b>Stack trace:</b>
	<pre>
	<%
		t.printStackTrace(new PrintWriter(out));
	%>
	</pre>

<a href="javascript:history.back()">Вернуться назад</a>
