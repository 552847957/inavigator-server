<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>

<%@ page import="java.util.List" %>
<%@ page import="ru.sberbank.syncserver2.gui.util.ButtonHelper" %>
<%@ page import="ru.sberbank.syncserver2.gui.util.JSPHelper" %>

<% final String PAGE_TITLE = "Администраторы"; %>
<% final String SECTION_TITLE = "Администраторы"; %>
<% request.setAttribute("PAGE_TYPE", "EMPLOYEE"); %>
<%@ include file="top.jsp" %>
<table width=100% border=0 cellpadding=0 cellspacing=0>

<tr class="listHeader">
  <th align="left">E-mail</th>
  <th align="left">ФИО</th>
  <th align="left"></th>
</tr>
<tr>
    <td colspan="4"><hr/></td>
    <td align="left"><%=ButtonHelper.showButton("Добавить", "edit.employee.gui?employeeId=-1", "plus-normal.png", "plus-mouseon.png")%></td>
</tr>
<%
  List employees = (List) pageContext.findAttribute("employees");
  for(int i=0; i<employees.size(); i++){
    Employee employee = (Employee)employees.get(i);
%>
<tr class="listData">
  <td><%=employee.getEmployeeEmail() %></td>
  <td><%=employee.getEmployeeName() %></td>
  <td align="left">
      <nobr>
          <%=ButtonHelper.showButton("Изменить"      ,"edit.employee.gui?employeeId="+employee.getEmployeeId()      , "buttons/i-edit.png")%>
          <%=ButtonHelper.showButton("Сменить пароль","show.changepassword.gui?employeeId="+employee.getEmployeeId() + "&adminMode=true", "buttons/i-pass.png")%>
          <%=ButtonHelper.showButton("Удалить"       ,"delete.employee.gui?employeeId="+employee.getEmployeeId()    , "minus-normal.png","minus-mouseon.png")%>
      </nobr>
<!--
      <input type=button value="Изменить"       onclick="javascript:window.location='edit.employee.gui?employeeId=<%=employee.getEmployeeId()%>';">
      <input type=button value="Сменить пароль" onclick="javascript:window.location='show.changepassword.gui?employeeId=<%=employee.getEmployeeId()%>';">
-->
</tr>
<%
  }
%>
</table>
<%=JSPHelper.composeActionButton("Назад", "location.href = 'welcome.public.gui'", "goup")%>
<%@ include file="../common/bottom.jsp" %>
