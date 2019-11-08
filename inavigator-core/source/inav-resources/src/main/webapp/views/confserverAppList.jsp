<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>

<%@ page import="java.util.List" %>
<%@ page import="ru.sberbank.syncserver2.gui.util.ButtonHelper" %>
<%@ page import="ru.sberbank.syncserver2.gui.util.JSPHelper" %>
<%@ page import="ru.sberbank.syncserver2.gui.data.SyncConfig" %>

<% final String PAGE_TITLE = "Настройки приложений"; %>
<% final String SECTION_TITLE = "Список приложений"; %>
<% request.setAttribute("PAGE_TYPE", "CLIENT_SETTINGS"); %>
<%@ include file="top.jsp" %>
<table border=0 cellpadding=10 cellspacing=0>

<tr class="listHeader">
  <th align="left">Идентификатор приложения</th>
  <th align="left"></th>
</tr>
<%
  List apps = (List) pageContext.findAttribute("apps");
  for(int i=0; i<apps.size(); i++){
    String app = (String) apps.get(i);
%>
<tr class="listData">
  <td><%=app%></td>
  <td>
      <form action="versions.clientproperties.gui" method="get">
          <input type="hidden" name="appBundle" value="<%=app%>">
          <input type="submit" value="Показать версии">
      </form>
  </td>
</tr>
<%
  }
%>
</table>
<%=JSPHelper.composeActionButton("Назад", "location.href = 'welcome.public.gui'", "goup")%>
<%@ include file="../common/bottom.jsp" %>
