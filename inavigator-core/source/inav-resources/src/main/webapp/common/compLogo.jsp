<%@page import="ru.sberbank.syncserver2.version.ManifestVersionReader"%>
<%@ page import="java.util.Properties" %>
<%@ page import="java.io.InputStream" %>
<%@ page import="java.io.IOException" %>
<%@ page import="org.apache.commons.io.IOUtils" %>
<%@ page import="ru.sberbank.syncserver2.service.core.ServiceManager" %>
<%@ page import="ru.sberbank.syncserver2.service.core.config.ConfigLoader" %>
<%@ page import="ru.sberbank.syncserver2.version.DatabaseVersionReader" %>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
    <td width="81" height="74" style="vertical-align:top;"><img class="transparent" src="../images/logo-sb.png" width="81" height="74" alt="Сбербанк"></td>
    <td width="1%" style="white-space: nowrap; vertical-align:top;">
        <table width="100%" height="40px"><tr><td></td></tr></table>
        <a class="title" href="welcome.public.gui">Административная <br/> консоль</a><br/>
        <%
            Properties prop = ManifestVersionReader.getVersionProperties(config);
            String databaseVersion = DatabaseVersionReader.getDatabaseVersion();
        %>
        <span class="info">
        	<br>
            Версия web: Rev. <%= prop.get("Specification-Version")%><br>
            Дата БД: &nbsp;&nbsp;  &nbsp;&nbsp;<%= databaseVersion %>
        </span>
        <%
        	//}
        %>
    </td>
    <td width="30">&nbsp;</td>
