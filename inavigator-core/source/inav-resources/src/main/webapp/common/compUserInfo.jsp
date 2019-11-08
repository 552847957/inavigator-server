<%@ page import="ru.sberbank.syncserver2.gui.data.Employee" %>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<% Employee loggedMenuEmployee = menuCtx.getEmployee(); %>

    <td width="1%" class="info" style="white-space: nowrap; vertical-align:top;">
        <table width="100%" height="40px"><tr><td></td></tr></table>
        <u><span class="infoBold"><%=loggedMenuEmployee.getEmployeeName()%></span></u><br/>
    </td>
    <td width="10">&nbsp;</td>
	<td width="21" style="text-align: right;">
        <a class="logout" title="Выход" href="../gui/logout.auth.gui"></a>
    </td>
    <td width="50">&nbsp;</td>
