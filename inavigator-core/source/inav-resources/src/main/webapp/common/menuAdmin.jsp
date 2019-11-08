<%@page import="ru.sberbank.syncserver2.gui.data.EmployeeRole"%>
<%@page import="ru.sberbank.syncserver2.gui.web.AuthController"%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%
    String pageType = (String) request.getAttribute("PAGE_TYPE");
    String emploeeClass  = "EMPLOYEE".equals(pageType)? "menuIconEmployeeHL"        : "menuIconEmployee";
    String databaseClass = "DATABASE".equals(pageType)? "menuIconDatabaseHL"        : "menuIconDatabase";
    String chPwdClass    = "CHPWD".equals(pageType)?    "menuIconPasswordChangeHL"  : "menuIconPasswordChange";
    String audit    = "AUDIT".equals(pageType)?    "selected"  : "";
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
                $('ul', this).slideDown(100); 
            },
            function () {
                $('ul', this).slideUp(100);
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
                <td width="1" class="menuIcon">
                    <a title="Смена пароля" class="menu <%=chPwdClass%>" href="show.changepassword.gui?adminMode=false">
                    <br/>
                    <br/>
                    <br/>
                    <br/>
                    Смена пароля</a>
                </td>
                <td class="menuIcon">
                    <img class="transparent" height="132" width="1" src="../images/green_line_without_blick.png" border="0">
                </td>
                <td width="1" class="menuIcon">
                	<br/>
                	<br/>
						<ul id="nav">
							<li><a class="menu <%=audit %>" href="#">Еще..</a>
								<ul>
									<li><a title="Аудит" class="menu <%=audit %>" href="log.audit.gui">Аудит</a></li>
								</ul></li>
						</ul>
					</td>
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
