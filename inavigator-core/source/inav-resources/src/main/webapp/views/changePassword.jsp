<%@ page import="ru.sberbank.syncserver2.gui.util.JSPHelper" %>
<%@ page import="ru.sberbank.syncserver2.gui.data.EmployeeRole" %>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<% final String PAGE_TITLE = "Смена пароля"; %>
<% final String SECTION_TITLE = "Смена пароля"; %>
<% request.setAttribute("PAGE_TYPE", "CHPWD"); %>
<%@ include file="top.jsp" %>
<br><br><br><br>
<%
    String s = String.valueOf(pageContext.findAttribute("adminMode"));
    boolean adminMode =  "true".equalsIgnoreCase(s);
%>
<script>

var onloadHandler = window.onload;
window.onload = function() {
	if (onloadHandler) {
		onloadHandler();
	}
	document.forms[0].currentPassword.focus();
}
</script>

<form name="resetForm" method="post" action="show.changepassword.gui">
<input type="hidden" name="employeeId" value="<%=request.getParameter("employeeId")%>">
<input type="hidden" name="adminMode" value="<%=request.getParameter("adminMode")%>">
<table width="40%" align="center" cellpadding="5px" cellspacing="0px" border="0px">
<tbody>
<tr>
    <% if (request.getParameter("firstIn") != null) {%>
    	<td colspan="2"><div style="color:red;font-weight:bold">При первом входе необходимо обязательно сменить пароль!</div></td>
    <%}%>
</tr>

<tr>
    <% if (!adminMode) {%>
    <td align="center"><b>Текущий пароль:&nbsp;&nbsp;</b></td><td><input type="password" name="currentPassword" size="32" maxlength="127" tabindex="1" type="text" class="white" value=""></td>
    <%}%>
</tr>
<tr>
	<td align="center"><b>Пароль:&nbsp;&nbsp;</b></td><td><input type="password" name="password" size="32" maxlength="127" tabindex="1" type="text" class="white" value=""></td>
</tr>
<tr>
	<td align="center"><b>Повтор пароля:&nbsp;&nbsp;</b></td><td><input type="password" name="passwordAgain" size="32" maxlength="127" tabindex="1" type="text" class="white" value=""></td>
</tr>
<%
    String error = (String) pageContext.findAttribute("error");
    if(!JSPHelper.isStringEmpty(error)){ %>
<tr>
	<td class="formerror" colspan="2"><%=error%></td>
</tr>
<%  }%>
<tr>
	<td align="center" colspan="2"><input type="submit" class="gray" value="Сохранить"/></td>
</tr>
</table>
</form>
<%=JSPHelper.composeActionButton("Назад", "location.href = 'list.employee.gui'", "goup")%>
<%@ include file="../common/bottom.jsp" %>
