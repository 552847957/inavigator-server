<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>

<%@ page import="java.util.List" %>
<%@ page import="ru.sberbank.syncserver2.gui.util.ButtonHelper" %>
<%@ page import="ru.sberbank.syncserver2.gui.util.JSPHelper" %>
<%@ page import="ru.sberbank.syncserver2.gui.data.*" %>

<%
  String appBundle  = (String)pageContext.findAttribute("appBundle");
  String appVersion = (String)pageContext.findAttribute("appVersion");
%>

<% final String PAGE_TITLE = "Настройки приложений"; %>
<% final String SECTION_TITLE = "Настройки для "+appBundle+" "+appVersion; %>
<% request.setAttribute("PAGE_TYPE", "CLIENT_SETTINGS"); %>
<%@ include file="top.jsp" %>

<form action="update.clientproperties.gui" method="post">
<input type="hidden" name="appBundle" value="<%=appBundle%>">
<input type="hidden" name="appVersion" value="<%=appVersion%>">
<table border=0 cellpadding=10 cellspacing=0>
<tr class="listHeader">
  <th align="left">Настройка</th>
  <th align="left">Значение</th>
</tr>
<%
  List properties = (List) pageContext.findAttribute("properties");
  for(int i=0; i<properties.size(); i++){
    ClientConfig property = (ClientConfig) properties.get(i);
%>
<tr class="listData">
  <td><%=property.getPropertyKey() %></td>
  <td>
          <input type="text" name="<%=property.getPropertyKey()%>" value="<%=property.getPropertyValue()%>" size="100">
  </td>
</tr>
<%
  }
%>
</table>
 <input type="submit" value="Сохранить">
</form>

<%=JSPHelper.composeActionButton("Назад", "location.href = 'versions.clientproperties.gui?appBundle="+appBundle+"&appVersion="+appVersion+"'", "goup")%>
<%@ include file="../common/bottom.jsp" %>
