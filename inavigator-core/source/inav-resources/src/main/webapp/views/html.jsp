<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>

<%@ page import="ru.sberbank.syncserver2.gui.util.JSPHelper" %>

<% final String PAGE_TITLE = "Информация"; %>
<% final String SECTION_TITLE = "Информация"; %>
<% request.setAttribute("PAGE_TYPE", "INFO"); %>
<%@ include file="top.jsp" %>
<%
    String html = (String)pageContext.findAttribute("html");
%>
<%=html%>
<%=JSPHelper.composeActionButton("Назад", "location.href = 'welcome.public.gui'", "goup")%>
<%@ include file="../common/bottom.jsp" %>
