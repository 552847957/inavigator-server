<%@page import="ru.sberbank.syncserver2.gui.util.format.JSPFormatPool"%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>

<%@ page import="java.util.List" %>
<%@ page import="ru.sberbank.syncserver2.gui.util.ButtonHelper" %>
<%@ page import="ru.sberbank.syncserver2.gui.util.JSPHelper" %>

<% final String PAGE_TITLE = "Аудит"; %>
<% final String SECTION_TITLE = "Аудит"; %>
<% request.setAttribute("PAGE_TYPE", "AUDIT"); %>
<%@ include file="top.jsp" %>

<link rel="stylesheet" href="../css/jquery.datetimepicker.css"/>
<link rel="stylesheet" href="../css/jquery.dataTables.css"/>
<link rel="stylesheet" href="../css/dataTables.tableTools.min.css"/>
<script src="../js/jquery.datetimepicker.js"></script>
<script src="../js/jquery.dataTables.min.js"></script>
<script src="../js/dataTables.tableTools.min.js"></script>
<script src="../js/table.initconfig.js"></script>
<script src="../js/audit.init.js"></script>
<link rel="stylesheet" href="../css/jquery.arcticmodal-0.3.css"/>
<script src="../js/jquery.arcticmodal-0.3.min.js"></script>
<script type="text/javascript">
function clFn() {
	$('#clear').find('input').val('').change();
};
function putPar(d) {
	d.table="true";
}
$(document).ready(function() { 
	var params={};
	var col = [
               { data: "date",
               	"orderable": true},
               { data: "host",
                   	"orderable": false},
               { data: "email" },
               { data: "module",
               	"orderable": true },
               { data: "eventType",
                "orderable": true },
               { data: "description",
                "orderable": false }
               ];
	params.init = $('#example');
	params.addParFn = putPar;
	params.url = "log.audit.gui";
	params.clearFn = clFn;
	params.columns = col;
	params.inputs = $('#insinp');
	params.butImport = true;
	var table = initTable(params);	
	$('#datetimestart').on('keyup change', function() {
		$('#dateandtime').val($(this).val()+"&"+$('#datetimeend').val()).change();
	});
	$('#datetimeend').on('keyup change', function() {
		$('#dateandtime').val($('#datetimestart').val()+"&"+$(this).val()).change();
	});
	$('#example tbody').on( 'dblclick', 'tr', function () {
		var record = table.ajax.json().data[$(this).index()];		
		$.arcticmodal({
			type: 'ajax',
		    url: 'description.audit.gui',
		    ajax: {
		        type: 'GET',
		        cache: false,		        
		        data: {id: record.eventId}
		    },
	        openEffect: {type: "none"},
	        closeEffect: {type: "none"},
	        afterLoadingOnShow: function(data, el) {
	        	if ($('#modalTable').width()>500) {
	        		$('.box-modal').width($('#modalTable').width());
	        	} else {
	        		$('.box-modal').width(500);
	        	}
	        }
		});
	});	
});
</script>

<table border=0 cellpadding=0 cellspacing=0 width=100%>
<tr class="listHeader">
  <th align="left">АУДИТ</th>
  <th align="left"></th>
</tr>
<tr>
    <td  colspan="2"><hr/></td>
</tr>
<tr>
	<td valign='top'>
	</td>
</tr>
<tr>
	<td>
	<div id='clear'>
	Дата&nbsp;с:&nbsp;<input type="text" id="datetimestart" placeholder="поиск с" />
	Дата&nbsp;до:&nbsp;<input type="text" id="datetimeend" placeholder="поиск до"/>		
	<span id='insinp'>
		<input type="hidden" id="dateandtime"/>
		Email:&nbsp;<input type="text" placeholder="поиск в источнике" />
		Модуль:&nbsp;<input type="text" placeholder="поиск в сообщении" />	
		Сообщение:&nbsp;<input type="text" placeholder="поиск в сообщении" />
		Хост:&nbsp;<input type="text" placeholder="поиск с хостом" />
	</span>	
	</div>	
	<input type="button" id="clearAudit" value="Очистить аудит"/>
	<br/>
	<br/>
	<br/>
	<table id="example" class="display" width="100%" cellspacing="0">
        <thead>
            <tr>
                <th>Дата и время</th>
                <th>Хост</th>
                <th>Email</th>
                <th>Модуль</th>
                <th>Сообщение</th>
                <th>Описание</th>
            </tr>
        </thead>
 
        <tfoot>
            <tr>
                <th>Дата и время</th>
                <th>Хост</th>
                <th>Email</th>
                <th>Модуль</th>
                <th>Сообщение</th>
                <th>Описание</th>
            </tr>
        </tfoot>
    </table>

	</td>
</tr>	
</table>
<div class="hide">
	<div class="box-modal" id="modWindow" >
		<div class="box-modal_close arcticmodal-close">X</div>
		<h2>Подтвердите удаление.</h2>
		<table>
			<tr><td><b>Пароль:</b></td><td><input type="password" name="password" size="32" maxlength="127" tabindex="1" type="text" value=""></td></tr>
			<tr><td colspan="2" align="right"><input type="button" value="Отправить" tabindex="2" id="delete"/></td></tr>
			<tr><td colspan="2"> <br><b>Подробности:</b></td></tr><tr><td colspan="2">Удаление записей с <span></span>
				 по <span></span> с условиями:<br> Email: <span></span><br> Модуль: <span></span><br> Сообщение: <span></span><br>
				 Хост: <span></span><br></td></tr>
		</table>
	</div>	
</div>
<%=JSPHelper.composeActionButton("Назад", "location.href = 'welcome.public.gui'", "goup")%>
<%@ include file="../common/bottom.jsp" %>
