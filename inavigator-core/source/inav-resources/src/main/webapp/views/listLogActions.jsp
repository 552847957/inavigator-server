<%@page import="ru.sberbank.syncserver2.gui.util.format.JSPFormatPool"%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>

<%@ page import="java.util.List" %>
<%@ page import="ru.sberbank.syncserver2.gui.util.ButtonHelper" %>
<%@ page import="ru.sberbank.syncserver2.gui.util.JSPHelper" %>

<% final String PAGE_TITLE = "Логирование - События"; %>
<% final String SECTION_TITLE = "Логирование - События"; %>
<% request.setAttribute("PAGE_TYPE", "LOG_TAGS"); %>
<%@ include file="top.jsp" %>
<%
   String tag = (String) pageContext.findAttribute("tag");	
	
%>
<link rel="stylesheet" href="../css/jquery.datetimepicker.css"/>
<link rel="stylesheet" href="../css/jquery.dataTables.css"/>
<link rel="stylesheet" href="../css/dataTables.tableTools.min.css"/>
<script src="../js/jquery.datetimepicker.js"></script>
<script src="../js/jquery.dataTables.min.js"></script>
<script src="../js/dataTables.tableTools.min.js"></script>
<script src="../js/table.initconfig.js"></script>
<script type="text/javascript">
function clFn() {
	$('#clear').find('input').val('').change();
};

function putPar(d) {
	d.json="true";
    d.tag="<%=tag.replace("\\", "\\\\") %>";    
}

$(document).ready(function() { 
	var params={};
	var col = [
               { data: "date",
               	"orderable": true},
               { data: "service" },
               { data: "msg",
               	"orderable": true }
               ];
	params.init = $('#example');
	params.addParFn = putPar;
	params.url = "actions.logs.gui";
	params.clearFn = clFn;
	params.columns = col;
	params.inputs = $('#insinp');
	params.butImport = true;
	initTable(params);
	$('#datetimestart').datetimepicker({
		format:'d.m.Y H:i',
		timepicker:true,
		step: 5
		});
	$('#datetimeend').datetimepicker({
		format:'d.m.Y H:i',
		timepicker:true,
		step: 5
		});	
	$('#datetimestart').on('keyup change', function() {
		$('#dateandtime').val($(this).val()+"!&!"+$('#datetimeend').val()).change();
		});
	$('#datetimeend').on('keyup change', function() {
		$('#dateandtime').val($('#datetimestart').val()+"!&!"+$(this).val()).change();
		});        
	});
</script>
<style>
.preformat { 
     width: 100%;
     white-space: pre-wrap;      /* CSS3 */   
     white-space: -moz-pre-wrap; /* Firefox */    
     white-space: -pre-wrap;     /* Opera <7 */   
     white-space: -o-pre-wrap;   /* Opera 7 */    
     word-wrap: break-word;      /* IE */
  }
</style>

<table border=0 cellpadding=0 cellspacing=0 width=100%>
<tr class="listHeader">
  <th align="left"><%="События для тэга " + tag %> </th>
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
	Дата с: <input type="text" id="datetimestart" placeholder="поиск с" />
	Дата до: <input type="text" id="datetimeend" placeholder="поиск до"/>		
	<span id='insinp'>
		<input type="hidden" id="dateandtime" />
		Источник: <input type="text" id="source" placeholder="поиск в источнике" />
		Сообщение: <input type="text" id="msg" placeholder="поиск в сообщении" />	
	</span>
	</div>	
	<br>
	<table id="example" class="display" width="100%" cellspacing="0">
        <thead>
            <tr>
                <th>Дата</th>
                <th>Источник</th>
                <th>Сообщение</th>
            </tr>
        </thead>
 
        <tfoot>
            <tr>
                <th>Дата</th>
                <th>Источник</th>
                <th>Сообщение</th>
            </tr>
        </tfoot>
    </table>

	</td>
</tr>	
</table>


<%=JSPHelper.composeActionButton("Назад", "location.href = 'tags.logs.gui'", "goup")%>
<%@ include file="../common/bottom.jsp" %>
