<%@page import="ru.sberbank.syncserver2.gui.data.EmployeeRole"%>
<%@page import="ru.sberbank.syncserver2.gui.web.AuthController"%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%
    String pageType = (String) request.getAttribute("PAGE_TYPE");
    String emploeeClass  = "EMPLOYEE".equals(pageType)? "menuIconEmployeeHL"        : "menuIconEmployee";
    String databaseClass = "DATABASE".equals(pageType)? "menuIconDatabaseHL"        : "menuIconDatabase";
    String chPwdClass    = "CHPWD".equals(pageType)?    "menuIconPasswordChangeHL"  : "menuIconPasswordChange";

    String pushNotification    = "PUSH_NOTIFICATIONS".equals(pageType)?    "selected"  : "";
    String audit    = "AUDIT".equals(pageType)?    "selected"  : "";
    String mobaccess    = "MIS_ACCESS_ADMIN".equals(pageType)?    "selected"  : "";
    String pushMonitor    = "MONITOR_PUSH".equals(pageType)?    "selected"  : "";
%>

<script>
    var ie = /MSIE (\d+.\d+);/.test(navigator.userAgent);
    var ieVersion = new Number(RegExp.$1)
    if (ie && ieVersion < 7) {
        document.write('<link rel="stylesheet" href="../css/ie6/menuIcons.css" type="text/css">');
        document.write('<link rel="stylesheet" href="../css/ie6/menuIconsAdmin.css" type="text/css">');
    } else {
        document.write('<link rel="stylesheet" href="../css/menuIcons.css" type="text/css">');
        document.write('<link rel="stylesheet" href="../css/menuIconsAdmin.css" type="text/css">');
    }
    $(document).ready(function () {  
        $('#nav li').hover(
            function () {
                $('ul', this).slideDown(0,100); 
            },
            function () {
                $('ul', this).slideUp(0,100);
            }
        );  
    });
</script>
<link rel="stylesheet" href="../css/dropMenu.css"/>

<table border="0" id='menu-area'>
  <tr>
    <%@ include file="compLogo.jsp" %>
    <%@ include file="compUserInfo.jsp" %>
    <td>
        <table width="100%" border="0">
            <tr>
             	<% AuthContext authContext = AuthController.getAuthContext(request);
                    Employee employee = authContext==null ? null:authContext.getEmployee();
                    if(employee.getEmployeeRoleId()== EmployeeRole.ADMIN){
                %>
                <td width="1" class="menuIcon">
                    <a title="Настройки" class="menu <%=databaseClass%>" href="list.properties.gui">
                        <br/>
                        <br/>
                        <br/>
                        <br/>
                        Настройки</a>
                </td>
                <td class="menuIcon">
                    <img class="transparent" height="132" width="1" src="../images/green_line_without_blick.png" border="0">
                </td>
                <td width="1" class="menuIcon">
                    <a title="Сервисы" class="menu <%=databaseClass%>" href="folders.services.gui">
                        <br/>
                        <br/>
                        <br/>
                        <br/>
                        Сервисы</a>
                </td>
                <td class="menuIcon">
                    <img class="transparent" height="132" width="1" src="../images/green_line_without_blick.png" border="0">
                </td>
                <td width="1" class="menuIcon">
                    <a title="Информация" class="menu <%=databaseClass%>" href="show.systeminfo.gui">
                        <br/>
                        <br/>
                        <br/>
                        <br/>
                        Информация</a>
                </td>
                <td class="menuIcon">
                    <img class="transparent" height="132" width="1" src="../images/green_line_without_blick.png" border="0">
                </td>
                <td width="1" class="menuIcon">
                    <a title="Логи" class="menu <%=databaseClass%>" href="tags.logs.gui">
                        <br/>
                        <br/>
                        <br/>
                        <br/>
                        Логи</a>
                </td>
                <td class="menuIcon">
                    <img class="transparent" height="132" width="1" src="../images/green_line_without_blick.png" border="0">
                </td>
                <td width="1" class="menuIcon">
                    <a title="Администраторы" class="menu <%=emploeeClass%>" href="list.employee.gui">
                    <br/>
                    <br/>
                    <br/>
                    <br/>
                    Админы
                    </a>
                </td>
                <td class="menuIcon">
                    <img class="transparent" height="132" width="1" src="../images/green_line_without_blick.png" border="0">
                </td>
                <%} else { %>
                <td width="1" class="menuIcon">
                    <a title="Управление мобильным доступом" class="menu <%="MIS_ACCESS_ADMIN".equals(pageType)? "menuIconDatabaseHL":"menuIconDatabase"%>" href="main.mobaccess.gui">
                    <br/>
                    <br/>
                    <br/>
                    <br/>
                    Мобильные Доступы
                    </a>
                </td>
                <td class="menuIcon">
                    <img class="transparent" height="132" width="1" src="../images/green_line_without_blick.png" border="0">
                </td>
                <%} %>
                <td width="1" class="menuIcon">
                    <a title="Смена пароля" class="menu <%=chPwdClass%>" href="show.changepassword.gui">
                    <br/>
                    <br/>
                    <br/>
                    <br/>
                    Смена пароля</a>
                </td>
                <td class="menuIcon">
                    <img class="transparent" height="132" width="1" src="../images/green_line_without_blick.png" border="0">
                </td>
                <% if(employee.getEmployeeRoleId()== EmployeeRole.ADMIN){ %>
                <td width="1" class="menuIcon">
                	<br/>
                	<br/>
						<ul id="nav">
							<li><a class="menu <%=pushNotification %>" href="#">Еще..</a>
								<ul>
									<li><a title="Push уведомления" class="menu <%=pushNotification %>" href="sendform.push.gui">Push Уведомления</a></li>
									<li><a title="Аудит" class="menu <%=audit %>" href="log.audit.gui">Журнал Аудита</a></li>
									<li><a title="Управление мобильным доступом" class="menu <%=mobaccess %>" href="main.mobaccess.gui">Мобильные Доступы</a></li>
									<li><a title="Мониторинг уведомлений" class="menu <%=pushMonitor %>" href="main.dppush.gui">Мониторинг уведомлений</a></li>
									<li><a title="Ошибки онлайн запросов" class="menu " href="actions.logs.gui?tag=ONLINE+ERRORS">Ошибки онлайн запросов</a></li>
								</ul></li>
						</ul>
					</td>
				<% } %>
                <td width="100%" class="menuIcon">
                </td>
            </tr>
        </table>
    </td>
  </tr>
  <tr>
    <td>&nbsp;</td>
    <td>&nbsp;</td>
    <td>&nbsp;</td>
	<td>&nbsp;</td>
  </tr>
</table>
