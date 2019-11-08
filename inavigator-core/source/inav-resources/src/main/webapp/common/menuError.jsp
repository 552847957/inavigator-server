<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>

<script>
    var ie = /MSIE (\d+.\d+);/.test(navigator.userAgent);
    var ieVersion = new Number(RegExp.$1)
    if (ie && ieVersion < 7) {
        document.write('<link rel="stylesheet" href="../css/ie6/menuIcons.css" type="text/css">');
    } else {
        document.write('<link rel="stylesheet" href="../css/menuIcons.css" type="text/css">');
    }
</script>
    
<!-- Таблица с меню -->

<table border="0" id='menu-area'>
  <tr>
    <%@ include file="compLogo.jsp" %>
    <%@ include file="compUserInfo.jsp" %>
    <td>&nbsp;</td>
  </tr>
  <tr>
    <td>&nbsp;</td>
    <td>&nbsp;</td>
    <td>&nbsp;</td>
    <td>&nbsp;</td>
    <td>&nbsp;</td>
  </tr>
</table>
