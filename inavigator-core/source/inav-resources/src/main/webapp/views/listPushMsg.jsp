<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>

<%@ page import="java.util.List" %>
<%@ page import="ru.sberbank.syncserver2.gui.util.ButtonHelper" %>
<%@ page import="ru.sberbank.syncserver2.gui.util.JSPHelper" %>

<% final String PAGE_TITLE = "Push уведомления"; %>
<% final String SECTION_TITLE = "Push уведомления"; %>
<% request.setAttribute("PAGE_TYPE", "PUSH_MESSAGES"); %>
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
               { "data": "0", orderable:false},
               { "data": "2", "visible": false}
               ];
	
	params.init = $('#example');
	params.addParFn = function(d) {d.table="true";};
	params.inputs = $('#insinp');
	params.url = "messages.push.gui";
	params.columns = col;
	params.butImport = false;
	params.butReset = false;
	params.butSearch = false;
	params.rowCallback = function( row, data, dataIndex ) {
		if ($('#tiphead').text()=="Получатели") {
			if ( data[2] == 'OK' ) {				
		        $(row).addClass( 'formsuccess' );
		    } else if (data[2] != 'undefined') {
		    	$(row).addClass( 'formerror' );		    	
		    } 
		}
	};
		
	var table = initTable(params);
	var scroll = window.pageYOffset;
	var pageIndex = table.page();
	
	$('#example tbody').on( 'dblclick', 'tr', function () {
		if ($('#tiphead').text()=="Получатели" || $(this).find("td").hasClass("dataTables_empty")) {
			return false;
		}
		scroll = window.pageYOffset;
		pageIndex = table.page();		
		var record = table.ajax.json().data[$(this).index()];	
		$('.col1').text("Email");
		$('.col2').text("Устройство");
		$('#back').removeClass("hide");
		$('#tiphead').text("Получатели");
		table.column(2).visible(true);
		table.ajax.url('messages.push.gui?NID='+record[2] ).load(function() {
			table.columns.adjust();
		});
		return false;
	});

	function restorePage() {
		table.page(pageIndex).draw('page');
		table.columns.adjust();
		window.scrollTo(0, scroll);
	};
	$('#back').click(function() {
		$('#tiphead').text("Список push сообщений");
		$('#back').addClass("hide");
		$('.col1').text("Сообщение");
		$('.col2').text("Время");
		table.column(2).visible(false);
		table.ajax.url('messages.push.gui').load(function() {
			setTimeout(restorePage, 0);
		});				
		return false;
	});
	$('#back').addClass("hide");
});
</script>

<table border=0 cellpadding=0 cellspacing=0 width=100%>
<tr class="listHeader">
  <th align="left" id="tiphead">Список push сообщений</th>
  <th align="left"></th>
</tr>
<tr>
    <td  colspan="2"><hr/></td>
</tr>
<tr>
	<td valign='top' align="left">
	<a id="back" href="">Список сообщений</a>
	</td>
</tr>
<tr>
	<td>
	<table id="example" class="display" width="100%" cellspacing="0">
        <thead>
            <tr>
                <th class="col2">Время</th>
                <th class="col1">Сообщение</th>
                <th >Статус</th>
            </tr>
        </thead>
 
        <tfoot>
            <tr>
                <th class="col2">Время</th>
                <th class="col1">Сообщение</th>
                <th >Статус</th>
            </tr>
        </tfoot>
    </table>

	</td>
</tr>	
</table>


<%=JSPHelper.composeActionButton("Назад", "location.href = 'welcome.public.gui'", "goup")%>
<%@ include file="../common/bottom.jsp" %>
