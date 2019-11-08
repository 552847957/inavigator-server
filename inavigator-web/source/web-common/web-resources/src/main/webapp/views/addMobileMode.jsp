<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="ru.sberbank.syncserver2.gui.util.JSPHelper" %>
<%@ page import="ru.sberbank.syncserver2.gui.util.ButtonHelper" %>
<% final String PAGE_TITLE = "Сбор логов"; %>
<% final String SECTION_TITLE = "Сбор логов"; %>
<% request.setAttribute("PAGE_TYPE", "MOBILE_LOGS"); %>
<%@ include file="top.jsp" %>

<form name="editForm" method="POST" action="add.mobile.gui" >
<table width="100%" cellpadding="5px" cellspacing="0px" border="1px" bordercolor="#A5ACB2">
<tr>
  <th width="20%">E-mail/Логин  </th>
  <td>
	<input type="text" name="userEmail" value="" size="110" style="WIDTH: 695px">
  </td>
</tr>
</table>
<%=ButtonHelper.showSave("editForm")%>
</form>
<%=JSPHelper.composeActionButton("Назад", "location.href = 'mode.mobile.gui'", "goup")%>
<%@ include file="../common/bottom.jsp" %>
