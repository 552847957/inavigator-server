<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>

<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
	<meta http-equiv="X-UA-Compatible" content="IE=edge">

    <title>Административная консоль <%=PAGE_TITLE != null? " - " + PAGE_TITLE : ""%></title>
    <!--
        <script type="text/javascript" src="../js/CalendarPopup.js"></script>
        <script type="text/javascript" src="../js/AnchorPosition.js"></script>
        <script type="text/javascript" src="../js/date.js"></script>
        <script type="text/javascript" src="../js/PopupWindow.js"></script>
    -->

	<link rel="stylesheet" href="../css/fonts.css" type="text/css">
	<link rel="stylesheet" href="../css/common.css" type="text/css">
	<link rel="stylesheet" href="../css/jquery-ui.min.css" type="text/css">

	<script src="../js/jquery-1.12.0.js" type="text/javascript"></script>
	<script src="../js/jquery.cookie.js" type="text/javascript"></script>
	<script src="../js/jquery-ui.min.js" type="text/javascript"></script>
</head>


<script type="text/javascript">
    $(document).ready(function(){
        var parts = location.pathname.split(".");
        var cookieStr = "scrollY" + parts[parts.length-1] + parts[parts.length - 2];
        $(window).scrollTop($.cookie(cookieStr));
        $.removeCookie(cookieStr);
        $('img').click(function(){
            var parts = location.pathname.split(".");
            var cookieStr = "scrollY" + parts[parts.length-1] + parts[parts.length - 2];
            $.cookie(cookieStr, (self.pageYOffset ||
                    (document.documentElement && document.documentElement.scrollTop) ||
                    (document.body && document.body.scrollTop)), {
                expires: 20
            });
        });
    });

    function storeScrollY() {
        var parts = location.pathname.split(".");
        var cookieStr = "scrollY" + parts[parts.length-1] + parts[parts.length - 2];
        $.cookie(cookieStr, $(window).scrollTop());
    }
</script>

<body id='body-st' bgcolor="#1a642a">
