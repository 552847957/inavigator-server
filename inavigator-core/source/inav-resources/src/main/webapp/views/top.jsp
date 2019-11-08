<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="ru.sberbank.syncserver2.gui.data.AuthContext" %>
<%@ page import="ru.sberbank.syncserver2.gui.data.Employee" %>
<%@ include file="../common/top.jsp" %>
<%
    AuthContext menuCtx = (AuthContext)request.getSession().getAttribute("user");
    if(menuCtx!=null && menuCtx.getEmployee()!=null) {
%>
        <%@ include file="../common/menuAdmin.jsp" %>
<%
    } else {
%>
        <%@ include file="../common/menuUnauthorized.jsp" %>
<%
    }
%>

<script type="text/javascript">
    <!--
    function showConfirm(text,href){
        if (confirm(text)) {
            window.location = href;
        }
    }
    //-->
</script>

<%@ include file="../common/contentTop.jsp" %>
