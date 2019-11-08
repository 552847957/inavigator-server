<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="ru.sberbank.syncserver2.gui.util.JSPHelper"%>
<%@ page import="ru.sberbank.syncserver2.util.ClusterHookProvider"%>
<% final String PAGE_TITLE = "Добро пожаловать"; %>
<% final String SECTION_TITLE = "Добро пожаловать"; %>
<%@ include file="top.jsp" %>
<table width="100%" cellpadding="0px" cellspacing="0px" border="0px">
<tr>
	<td width="20px"><img src="../images/pixel.gif" alt="" width="20px" height="1px"></td>
	<td align="justify" class="welcome">
		<br>
		Уважаемые коллеги,<br><br>
		Мы рады приветствовать Вас в Административной Консоли. <br><br>
<%  if(menuCtx == null || menuCtx.getEmployee() == null) { %>
		Для начала работы пожалуйста <a href="login.auth.gui">войдите в систему</a>.<br>
<%  } else { %>
      <% AuthContext authContext = AuthController.getAuthContext(request);
         Employee employee = authContext==null ? null:authContext.getEmployee();
         if(employee.getEmployeeRoleId()== EmployeeRole.ADMIN){
      %>

        Эта часть сайта предназначена для <br>
        <nl>
            <li><a href="list.properties.gui">изменения настроек</a></li>
            <li><a href="folders.services.gui">старта и запуска сервисов</a></li>
            <li><a href="tags.logs.gui">просмотра последних логов</a></li>
            <li><a href="list.employee.gui">изменения списка администраторов</a></li>
        </nl>
        <br>
        <%if (ClusterHookProvider.isClusterHooked())  {%>
        <b>Внимание!</b> В путях сервисов с ROOT_FOLDER используется дополнительный суффикс к названию приложения _<%=ClusterHookProvider.getSuffixForHook() %><br>
        <%} %>
     <% } %>
        
<%  } %>
		<br>
	</td>
	<td width="20px"><img src="../images/pixel.gif" alt="" width="20px" height="1px"></td>
</tr>
</table>
<%@ include file="../common/bottom.jsp" %>
