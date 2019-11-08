<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List" %>
<%@ page import="org.springframework.validation.Errors" %>
<%@ page import="ru.sberbank.syncserver2.gui.util.JSPHelper" %>
<%@ page import="ru.sberbank.syncserver2.gui.data.Employee" %>
<%@ page import="ru.sberbank.syncserver2.gui.util.ButtonHelper" %>
<%@ page import="ru.sberbank.syncserver2.gui.data.EmployeeRole" %>
<% final String PAGE_TITLE = "Администраторы"; %>
<% final String SECTION_TITLE = "Администраторы"; %>
<% request.setAttribute("PAGE_TYPE", "EMPLOYEE"); %>
<%@ include file="top.jsp" %>
<%
	Employee employee = (Employee) pageContext.findAttribute("employee");
	Errors errors = (Errors) pageContext.findAttribute("errors");
	List roles = (List) pageContext.findAttribute("roles");
%>
<form name="editForm" method="POST" action="edit.employee.gui" >
<table width="100%" cellpadding="5px" cellspacing="0px" border="1px" bordercolor="#A5ACB2">
<input type="hidden" name="employeeId"  value=<%=employee.getEmployeeId() %> />

<tr>
  <th width="20%">E-mail/Логин  </th>
  <td>
     <table align="left" width="100%" border="0">
     <tr><td><input type="text" name="employeeEmail" value="<%=employee.getEmployeeEmail()%>" size="110" style="WIDTH: 695px"></td></tr>
  <% if(errors.getFieldErrorCount("employeeEmail")>0){ %>
     <tr><td class=formerror><%= JSPHelper.getAllFieldErrors(errors, "employeeEmail") %></td></tr>
  <% } %>
     </table>
  </td>
</tr>
<tr>
  <th width="20%">ФИО</th>
  <td>
     <table align="left" width="100%" border="0">
     <tr><td><input type="text" name="employeeName" value="<%=employee.getEmployeeName()%>" size="110" style="WIDTH: 695px"></td></tr>
  <% if(errors.getFieldErrorCount("employeeName")>0){ %>
     <tr><td class=formerror><%= JSPHelper.getAllFieldErrors(errors,"employeeName") %></td></tr>
  <% } %>
     </table>
  </td>
</tr>
<% if(roles.size()>=2){%>
<tr>
    <th width="20%">Роль пользователя</th>
    <td>
        <table align="left" width="100%" border="0">
            <tr>
                <td>
                <% for (Object o: roles) {
                	EmployeeRole role = (EmployeeRole) o;	%>
                   <input type="radio" name="employeeRoleId" value="<%=role.getRoleId()%>"    <%=employee.getEmployeeRoleId()==role.getRoleId()    ? "checked":""%> > <%=role.getRoleName() %>
                   <%} %>
                </td>
            </tr>
            <% if(errors.getFieldErrorCount("employeeName")>0){ %>
            <tr><td class=formerror><%= JSPHelper.getAllFieldErrors(errors,"employeeName") %></td></tr>
            <% } %>
        </table>
    </td>
</tr>
<% } %>
    <% if(employee.getEmployeeId()==-1){%>

<tr>
  <th width="20%">Пароль</th>
  <td>
     <table align="left" width="100%" border="0">
     <tr><td><input type="password" name="employeePassword" value="<%=employee.getEmployeePassword()%>" size="110" style="WIDTH: 695px"></td></tr>
  <% if(errors.getFieldErrorCount("employeePassword")>0){ %>
     <tr><td class=formerror><%= JSPHelper.getAllFieldErrors(errors,"employeePassword") %></td></tr>
  <% } %>
     </table>
  </td>
</tr>
<tr>
	<th width="20%">Повтор пароля</th>
	<td>
  	 	<table align="left" width="100%" border="0">
   		<tr><td><input type="password" name="employeePasswordAgain" value="<%=employee.getEmployeePassword()%>" size="110" style="WIDTH: 695px"></td></tr>
   		</table>
	</td>
</tr>
<% } %>
<tr>
	<th width="20%">Удаленный пользователь</th>
	<td>
  	 	<table align="left" width="100%" border="0">
   		<tr><td><input type="checkbox" name="remote" <%=employee.isRemote()?"checked":""%> ></td></tr>
   		</table>
	</td>
</tr>
<tr>
	<th width="20%">Разрешить только чтение</th>
	<td>
  	 	<table align="left" width="100%" border="0">
   		<tr><td><input type="checkbox" name="readOnly" <%=employee.isReadOnly()?"checked":""%> ></td></tr>
   		</table>
	</td>
</tr>
</table>
<%=ButtonHelper.showSave("editForm")%>
</form>
<%=JSPHelper.composeActionButton("Назад", "location.href = 'list.employee.gui'", "goup")%>
<%@ include file="../common/bottom.jsp" %>
