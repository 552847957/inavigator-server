<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>

<%@ page import="java.util.List" %>
<%@ page import="ru.sberbank.syncserver2.gui.util.ButtonHelper" %>
<%@ page import="ru.sberbank.syncserver2.gui.util.JSPHelper" %>
<%@ page import="java.net.URLEncoder" %>

<% final String PAGE_TITLE = "Статусы чекеров"; %>
<% final String SECTION_TITLE = "Статусы чекеров"; %>
<% request.setAttribute("PAGE_TYPE", "CHECKERS"); %>
<%@ include file="top.jsp" %>
<%
  List<String[]> names = (List<String[]>) pageContext.findAttribute("names");   
%>
<script type="text/javascript">
function callRequestTaskInfo(seconds) {
	$.ajax({ 
		url: "status.checkers.gui"
		,success: function(data){
			data.forEach(function(checker, i, checkers) {
				var $node = $("#"+checker.name);
				$node.find("tr:gt(1)").remove();
				checker.statuses.forEach(function(info, j, all) {
					var $newRow = $('<tr class="listData" />');
					var color = "";
					if (info.status == 0) color = "green";
					if (info.status == 1) color = "yellow";
					if (info.status == 2) color = "red";
					$('<td>'+info.code+'</td><td>'+info.txt+'</td>').css( "background-color", color ).appendTo( $newRow );
					$newRow.appendTo( $node );
				});
				$node.find("span").text(checker.lastUpdate);
			});
			var formatter = new Intl.DateTimeFormat("ru", {day: "numeric",month: "numeric",year: "numeric",hour: "numeric",minute: "numeric",second: "numeric"});
			$('#dateNow').html(formatter.format(new Date));
			setTimeout(function() {callRequestTaskInfo(seconds)}, seconds * 1000);
		}
		,error: function(a,b,c) {
			location.reload();
		}
		,dataType: "json"
	});
}
$(document).ready(function() {
    var TIMEOUT_SECONDS = 15;    
    callRequestTaskInfo(TIMEOUT_SECONDS);
});
</script>
Время последнего обновления страницы: <span id="dateNow"></span>
<hr/>
<table border=1 cellpadding=5px cellspacing=0 style="border-collapse: collapse">
<%  
  for(String[] name: names){      
%>

<tbody id="<%=name[0] %>">
<tr class="listData applicationHeader">
  <td colspan="2"><br/><b><a title="перейти к логам сервиса" href="actions.logs.gui?tag=<%=name[0] %>"><%=name[0] %></a></b> <em>(<%=name[1] %>)</em> <b>Дата последней проверки </b><span></span></td>
</tr>  

<tr class="listHeader" >
  <th align="left">Код проверки</th>
  <th align="left">Сообщение</th>
</tr>
</tbody>

<%
  }
%>
</table>
<br>
<nobr><table width=30px bgcolor="green" style="display:inline;" cellpadding="7">
<tr><td height=30px/></tr>
</table> - успешная проверка</nobr>
<nobr><table width=30px bgcolor="yellow" style="display:inline;" cellpadding="7">
<tr><td height=30px/></tr>
</table> - неуспешная проверка (временный сбой)</nobr>
<nobr><table width=30px bgcolor="red" style="display:inline;" cellpadding="7">
<tr><td height=30px/></tr>
</table> - неуспешная проверка (отправлено уведомление в ТП)</nobr> 
<%=JSPHelper.composeActionButton("Назад", "location.href = 'welcome.public.gui'", "goup")%>
<%@ include file="../common/bottom.jsp" %>
