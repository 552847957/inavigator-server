<%@ page import="java.util.List" %>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<% final String PAGE_TITLE = "Смена пароля"; %>
<% final String SECTION_TITLE = "Смена пароля"; %>
<% request.setAttribute("PAGE_TYPE", "CHPWD"); %>

<%@ include file="top.jsp" %>
<%=pageContext.findAttribute("successText")%>
<%@ include file="../common/bottom.jsp" %>