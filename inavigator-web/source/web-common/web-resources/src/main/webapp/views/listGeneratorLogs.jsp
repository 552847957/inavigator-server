<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>

<%@ page import="ru.sberbank.syncserver2.gui.util.JSPHelper" %>
<%@ page import="ru.sberbank.syncserver2.service.generator.single.data.ActionInfo" %>
<%@ page import="java.util.List" %>
<%@ page import="ru.sberbank.syncserver2.service.log.LogMsg" %>
<%@ page import="ru.sberbank.syncserver2.service.log.LogEventType" %>

<% final String PAGE_TITLE = "Sync Generator - Логи"; %>
<% final String SECTION_TITLE = "Sync Generator - Логи"; %>
<% request.setAttribute("PAGE_TYPE", "GENERATOR_LOGS"); %>
<%@ include file="top.jsp" %>

<link rel="stylesheet" href="../css/jquery.dataTables.css"/>
<link rel="stylesheet" href="../css/dataTables.tableTools.min.css"/>
<script src="../js/jquery.dataTables.min.js"></script>
<script src="../js/dataTables.tableTools.min.js"></script>
<script src="../js/table.initconfig.js"></script>
<%
  String dataFileName = (String) pageContext.findAttribute("dataFileName");
  List<String> listFiles = (List<String>) pageContext.findAttribute("allowedFiles");
  List<String> listDates = (List<String>) pageContext.findAttribute("generationDates");
  String date = (String) pageContext.findAttribute("date");
  Boolean debug = (Boolean) pageContext.findAttribute("debug");
%>
<script type="text/javascript">
function clFn() {
	
};
function putPar(d) {
	d.table="true";
	d.dataFileName=$('#fileName').val();
	d.debug=$('#debug').prop("checked");
	d.date=$('#generationDate').val();
}
$(document).ready(function() { 
	var params={};
	var col = [
               { data: "date",
               	"orderable": false},
               { data: "text",
                   	"orderable": false },
               { data: "eventType",
               		"visible": false }
               ];
	params.init = $('#example');
	params.addParFn = putPar;
	params.url = "logs.generator.gui";
	params.clearFn = clFn;
	params.columns = col;
	params.inputs = $('#insinp');
	params.butImport = true;
	params.butSearch = false;
	params.butReset = false;
	params.processing = false;
	params.rowCallback = function( row, data, dataIndex ) {
	    if ( data.eventType == "<%=LogEventType.ERROR%>" ) {
	        $(row).addClass( 'formerror' );
	      }
	    };
	var table = initTable(params);	
	$.fn.dataTable.ext.errMode='none';
	$('#fileName').change(function() {
		window.location.replace("logs.generator.gui?dataFileName="+$('#fileName').val()+"&debug="+$('#debug').prop("checked"));
	});
	$('#generationDate').change(function() {
		table.draw();
	});
	$('#debug').change(function() {
		table.draw();
	});
	
	function updateTable(seconds) {
		table.ajax.reload(function (){
			table.columns.adjust();
			}, false);
		setTimeout(function() {updateTable(seconds)}, seconds * 1000);
	}	
	setTimeout(function() {updateTable(5)}, 4 * 1000);

});
</script>

<table border=0 cellpadding=10 cellspacing=10 id="logTable" width=100%>

<tr>
	<td width="260">Логи для файла:&nbsp;
		<select id="fileName">
		<% for (String s: listFiles)  {%>
   			<option <%=s.equals(dataFileName)?"selected":"" %>><%=s %></option>
   		<%} %>
		</select>		
	</td>
	<td>
	дата старта:&nbsp;
	 	<select id="generationDate">
		<% for (String s: listDates)  {%>
   			<option <%=s.equals(date)?"selected":"" %>><%=s %></option>
   		<%} %>
		</select>	 
	</td>
	<td align="right">
	<input type="checkbox" id="debug" <%=debug?"checked":"" %>>&nbsp;Показывать информацию для ИТ
	</td>
</tr>
<tr><td colspan="3">
		<table id="example" class="display" width="100%" cellspacing="0">
        <thead>
            <tr>
                <th>Дата и время</th>
                <th>Текст</th>
            </tr>
        </thead> 
        <tfoot>
            <tr>
                <th>Дата и время</th>
                <th>Текст</th>
            </tr>
        </tfoot>
    	</table>
    </td>
</tr>
</table>
<a name="#end"></a>
<%=JSPHelper.composeActionButton("Назад", "location.href = 'tasks.generator.gui'", "goup")%>


<%@ include file="../common/bottom.jsp" %>


