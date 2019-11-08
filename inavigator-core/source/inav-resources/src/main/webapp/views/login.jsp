<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <title>Административная консоль - вход в систему</title>
    <link rel="stylesheet" href="../css/common.css" type="text/css">
</head>
<body id='body-st' bgcolor="#004121">

<script>
var onloadHandler = window.onload;
window.onload = function() {
	if (onloadHandler) {
		onloadHandler();
	}
	document.forms[0].login.focus();
}

function submitonEnter(evt){
    var charCode = (evt.which) ? evt.which : event.keyCode;
    if(charCode == "13"){
        document.logonForm.submit();
    }
}
</script>
<%
   String message = (String) pageContext.findAttribute("message");
   int tableSize = message!=null ? 655:639;
%>
<form name="logonForm" method="post" action="login.auth.gui">
<input type="hidden" name="forwardAction" value='<%=(String) pageContext.findAttribute("forwardAction")%>'/>
<table background="../images/splash-login.jpg" width="982" height="<%=tableSize%>" align="center">
<tr height="208"></tr>
<tr height="20">
	<td width="370"></td>
	<td align="center" height="14"><input class="login" type="text" name="login" size="30" maxlength="127" tabindex="1" ></td>
</tr>
<tr height="13"></tr>
<tr height="20">
	<td></td>
	<td align="center" height="14"><input class="login" type="password" name="password" size="30" maxlength="255" tabindex="2" onKeyDown="javascript:return submitonEnter(event);"></td>
</tr>
<tr height="10"></tr>
<% if(message!=null){ %>
<tr height="20">
	<td></td>
	<td align="center" height="14"><font color="red"><%=message%></font></td>
</tr>
<% } else { %>
<tr height="10"></tr>
<% } %>
<tr>
	<td></td>
	<td align="center" height="14"><img src="../images/admin/login-enter-normal.jpg" alt="Вход" onmouseover="javascript:this.src = '../images/admin/login-enter-mouseon.jpg'" onmouseout="javascript:this.src = '../images/admin/login-enter-normal.jpg'" onclick="javascript:logonForm.submit()"/></td>
</tr>
<tr height="255"></tr>
</table>
</form>
</body>
</html>