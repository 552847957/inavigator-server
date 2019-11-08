<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>

<%
    String s = (String) pageContext.findAttribute("message");
%>
    
<h2><%=s%></h2>

<a href="javascript:history.back()">Вернуться назад</a>
