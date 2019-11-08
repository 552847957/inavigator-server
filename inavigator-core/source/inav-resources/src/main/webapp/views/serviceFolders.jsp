<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>

<%@ page import="java.util.List" %>
<%@ page import="ru.sberbank.syncserver2.gui.util.ButtonHelper" %>
<%@ page import="ru.sberbank.syncserver2.gui.util.JSPHelper" %>
<%@ page import="ru.sberbank.syncserver2.service.core.config.Folder" %>

<% final String PAGE_TITLE = "Сервисы"; %>
<% final String SECTION_TITLE = "Сервисы"; %>
<% request.setAttribute("PAGE_TYPE", "SERVICES"); %>
<%@ include file="top.jsp" %>
<table border=0 cellpadding=10 cellspacing=10 width="100%">

<tr class="listHeader">
  <th align="left">Группа сервисов</th>
  <th align="left">Описание</th>
  <th align="left"></th>
</tr>
<tr>
    <td colspan="2"><hr/></td>
</tr>
<%
  List<Folder> folders = (List) pageContext.findAttribute("folders");
  for(int i=0; i<folders.size(); i++){
      Folder folder = folders.get(i);
%>
<tr class="listData">
  <td><%=folder.getCode() %></td>
  <td><%=folder.getDescription() %></td>
  <td align="left">
      <nobr>
          <%=ButtonHelper.showButton("Показать сервисы", "list.services.gui?folder=" + folder.getCode(), "buttons/i-pass.png")%>
      </nobr>
   </td>
</tr>
<%
  }
%>
</table>
<%=JSPHelper.composeActionButton("Назад", "location.href = 'welcome.public.gui'", "goup")%>
<%@ include file="../common/bottom.jsp" %>
