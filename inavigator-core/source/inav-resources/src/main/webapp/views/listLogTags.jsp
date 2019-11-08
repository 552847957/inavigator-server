<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>

<%@ page import="java.util.List" %>
<%@ page import="ru.sberbank.syncserver2.gui.util.ButtonHelper" %>
<%@ page import="ru.sberbank.syncserver2.gui.util.JSPHelper" %>
<%@ page import="java.net.URLEncoder" %>

<% final String PAGE_TITLE = "Логирование - Тэги"; %>
<% final String SECTION_TITLE = "Логирование - Тэги"; %>
<% request.setAttribute("PAGE_TYPE", "LOG_TAGS"); %>
<%@ include file="top.jsp" %>
<table border=0 cellpadding=0 cellspacing=0>

<tr class="listHeader">
  <th align="left">Тэги (группы сообщений)</th>
  <th align="left"></th>
</tr>
<tr>
    <td colspan="2"><hr/></td>
</tr>
<%
  List<String> tags = (List) pageContext.findAttribute("tags");
  int pageIndex = (Integer)pageContext.findAttribute("pageIndex");
  for(int i=0; i< tags.size(); i++){
      String tag = tags.get(i);
%>
<tr class="listData">
  <td><%=tags.get(i) %></td>
  <td align="left">
      <nobr>
          <%=ButtonHelper.showButton("Показать события", "actions.logs.gui?tag=" + (tag == null ? "NULL" : URLEncoder.encode(tag, "UTF-8")), "buttons/i-pass.png")%>
      </nobr>
   </td>
</tr>
<%
  }
%>
<tr>
    <td colspan="2">
<% if(pageIndex>0){%>
        <a href="<%="tags.logs.gui?pageIndex=" + (pageIndex-1)%>">Предыдущая страница</a>
<% }
   if(tags.size()>0){%>
        <a href="<%="tags.logs.gui?pageIndex=" + (pageIndex+1)%>">Следующая страница</a>
<% }%>
    </td>
</tr>
</table>
<%=JSPHelper.composeActionButton("Назад", "location.href = 'welcome.public.gui'", "goup")%>
<%@ include file="../common/bottom.jsp" %>
