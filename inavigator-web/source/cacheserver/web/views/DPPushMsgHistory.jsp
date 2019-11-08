<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>

<%@ page import="java.util.List" %>
<%@ page import="ru.sberbank.syncserver2.gui.util.ButtonHelper" %>
<%@ page import="ru.sberbank.syncserver2.gui.util.JSPHelper" %>

<% final String PAGE_TITLE = "Push уведомления"; %>
<% final String SECTION_TITLE = "Push уведомления"; %>
<% request.setAttribute("PAGE_TYPE", "MONITOR_PUSH"); %>
<%@ include file="top.jsp" %>
<link rel="stylesheet" href="../css/jquery.dataTables.css"/>
<link rel="stylesheet" href="../css/dataTables.tableTools.min.css"/>
<script src="../js/jquery.dataTables.min.js"></script>
<script src="../js/dataTables.tableTools.min.js"></script>
<script src="../js/table.initconfig.js"></script>
<script type="text/javascript">
$(document).ready(function() { 
	var params={};
	var col = [            
               { "data": "1", orderable:false},
               { "data": "2", orderable:false}
               ];
	
	params.init = $('#example');
	params.addParFn = function(d) {d.table="true";};
	params.inputs = $('#insinp');
	params.url = "table.dppush.gui";
	params.columns = col;
	params.butImport = false;
	params.butReset = false;
	params.butSearch = false;
	params.paging = false;
	params.rowCallback = function( row, data, dataIndex ) {
		if ($('#tiphead').text()=="История статусов") {
			if (data[2]=='1') {
				$(row).find("td:last").text('готово к отправке');
			} else if ( data[2] == '0' ) {				
				$(row).find("td:last").text('не готово к отправке');
		    } else if (data[2] == '2') {
		    	$(row).find("td:last").text('выгружено в подсистему отправки');	    	
		    } else if (data[2] == '3') {
		    	$(row).find("td:last").text('отправлено успешно');	    	
		    } else if (data[2] == '4') {
		    	$(row).find("td:last").text('отправлено с ошибкой');    	
		    } else if (data[2] == '5') {
		    	$(row).find("td:last").text('отменено');	    	
		    } else {
		    	$(row).find("td:last").text('неизвестный статус');	    	
		    }    
		}
	}; 
	var table = initTable(params);
	var nId;
	$('#example tbody').on( 'dblclick', 'tr', function () {
		if ($('#tiphead').text()=="История статусов") {
			return false;
		}		
		var record = table.ajax.json().data[$(this).index()];	
		table.ajax.url('table.dppush.gui?NID='+record[0] ).load();
		$('.col1').text("Дата и время");
		$('.col2').text("Статус");
		nId = record[0];
		$('#msg').text(record[1]);
		$('#email').text(record[2]);
		$('#notification').show();
		$('#cancel').show();
		$('#tiphead').text("История статусов");		
		return false;
	});
	$('#back').click(function() {
		table.ajax.url('table.dppush.gui').load();
		$('#tiphead').text("Список уведомлений");
		$('#notification').hide();
		$('.col1').text("Сообщение");
		$('.col2').text("Получатель");		
		return false;
	});
	$('#notification').hide(); 
	$('#cancel').click(function() {
		if (confirm('Дальнейшая отправка этого уведомления будет невозможна, вы действительно хотите отменить это уведомление?')) {
			$.get('cancel.dppush.gui?NID='+nId, function() {
				table.ajax.reload();
				$('#cancel').hide();
			});			
		}
	});
});
</script>

<table border=0 cellpadding=0 cellspacing=0 width=100%>
<tr class="listHeader">
  <th align="left" id="tiphead">Список уведомлений</th>
  <th align="left"></th>
</tr>
<tr>
    <td  colspan="2"><hr/></td>
</tr>
<tr>
	<td valign='top' align="left">
	<div id="notification">
		<a id="back" href="">Список уведомлений</a>
		<br/>
		<br/>
		<table>
			<tr>
				<th>Сообщение</th>
				<td id="msg"></td>
				<td/>
			</tr>
			<tr>			
				<th>Получатель</th>
				<td id="email"></td>
				<td align="right" width="100"><button id="cancel">Отменить</button></td>
			</tr>
		</table>
		<br/>
	</div>
	</td>
</tr>
<tr>
	<td>
	<table id="example" class="display" width="100%" cellspacing="0">
        <thead>
            <tr>
                <th class="col1">Сообщение</th>
                <th class="col2">Получатель</th>
            </tr>
        </thead>
 
        <tfoot>
            <tr>
                <th class="col1">Сообщение</th>
                <th class="col2">Получатель</th>
            </tr>
        </tfoot>
    </table>

	</td>
</tr>	
</table>


<%=JSPHelper.composeActionButton("Назад", "location.href = 'welcome.public.gui'", "goup")%>
<%@ include file="../common/bottom.jsp" %>
