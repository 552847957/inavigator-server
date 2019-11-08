<%@ page import="ru.sberbank.syncserver2.gui.util.JSPHelper"%>
<% final String PAGE_TITLE = "Вход в Административную Консоль"; %>
<% final String SECTION_TITLE = "Вход в Административную Консоль"; %>
<%@ include file="top.jsp" %>
<script>
var onloadHandler = window.onload;
window.onload = function() {
	if (onloadHandler) {
		onloadHandler();
	}
	document.forms[0].login.focus();
}
</script>
<br><br><br><br>
<form name="logonForm" method="post" action="loginas.adminauth.do">
<input type="hidden" name="forwardAction" value='<%=(String) pageContext.findAttribute("forwardAction")%>'/>
<table width="40%" align="center" cellpadding="5px" cellspacing="0px" border="0px">
<tbody>
<tr>
	<td align="right" width="30%"><b>Email<sup>*</sup>:</b></td>
	<td align="left" width="70%"><input type="text" class="white" name="login" size="32" maxlength="127" tabindex="1"></td>
</tr>
<tr>
	<td align="right"><b>Password<sup>*</sup>:</b></td>
	<td align="left"><input type="password" class="white" name="password" size="32" maxlength="255" tabindex="2"></td>
</tr>
<tr>
    <td align="right" width="30%"><b>Login As Email<sup>*</sup>:</b></td>
    <td align="left" width="70%"><input type="text" class="white" name="loginAsEmail" size="32" maxlength="127" tabindex="1"></td>
</tr>
<%
    String message = (String) pageContext.findAttribute("message");
    if(message!=null){%>
    <tr>
        <td align="right"></td>
        <td align="left" class="formerror"><%=message%></td>
    </tr>
<%  }%>
<tr>
	<td> </td>
	<td align="left" style="font-size: 10px">* You can use your Group Directory login/password.</td>
</tr>
<tr>
	<td align="right"><input type="submit" class="gray" value="Login"/></td>
	<td align="left"><input type="button" class="gray" value="Reset Password" onclick="javascript:window.location='reset.password.do?email='+logonForm.email.value;"/></td>
</tr>
</table>
</form>
<%@ include file="../common/bottom.jsp" %>
