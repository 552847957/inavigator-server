<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>

<%@ page import="java.util.List" %>
<%@ page import="ru.sberbank.syncserver2.gui.util.ButtonHelper" %>
<%@ page import="ru.sberbank.syncserver2.gui.util.JSPHelper" %>

<% final String PAGE_TITLE = "Контроллеры контента"; %>
<% final String SECTION_TITLE = "Контроллеры контента"; %>
<% request.setAttribute("PAGE_TYPE", "CONTENT_CONTROLLERS"); %>
<%@ include file="top.jsp" %>
<%
	List<String> apps = (List<String>) pageContext.findAttribute("apps");
%>
<link rel="stylesheet" href="../css/jquery.dataTables.css"/>
<link rel="stylesheet" href="../css/dataTables.tableTools.min.css"/>
<script src="../js/jquery.dataTables.min.js"></script>
<script src="../js/dataTables.tableTools.min.js"></script>
<script src="../js/table.initconfig.js"></script>
<script type="text/javascript">

function putPar(d) {
	d.table="true";
}

$(document).ready(function() { 
	var params={};
	var col = [            
               { "data": "0", orderable:true},
               { "data": "1", orderable:false},
               {
            	   "data": null,
            	   "orderable":      false,
            	   "defaultContent": "",
            	   render: function ( data, type, full, meta ) {
            		   var t = '<button type="button" class="delete">Удалить</button>';
            		   return t;
            		   }
               }
               ];
	
	params.init = $('#example');
	params.addParFn = putPar;
	params.inputs = $('#insinp');
	params.url = "show.contentcontrol.gui";
	params.columns = col;
	params.butImport = false;
	params.butReset = false;
	params.butSearch = false;
	var table = initTable(params);
	$('#example').on( 'draw.dt', function () {
		$('.delete').click(function() {
			var cols = $(this).closest('tr').find('td');
			$.get( "delete.contentcontrol.gui", 
					{ app: cols.eq(0).text(), userEmail: cols.eq(1).text() }
			).done(setTimeout(function(){table.draw(false);}, 100));
		});
	} );
	$('#add').click(function() {
		$.post( "add.contentcontrol.gui", 
				{ app: $('#app').val(), userEmail: $('#userEmail').val() } 
		).done(setTimeout(function(){table.draw(false);}, 500));
		$('#userEmail').val("");
	});
});
</script>

<table border=0 cellpadding=0 cellspacing=0 width=100%>
<tr class="listHeader">
  <th align="left">Список контроллеров контента </th>
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

	<form name="addForm" method="POST" action="add.contentcontrol.gui">
		Приложение:&nbsp;
		<select id="app">
			<option disabled>Выберите приложение</option>
	<% for (String s: apps)  {%>
  			<option><%=s %></option>
  		<%} %>
		</select>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
		Email:&nbsp;<input type="text" id="userEmail" value="">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
				<button id="add" type='button'>Добавить</button>
	</form> 
<br>
	<table id="example" class="display" width="100%" cellspacing="0">
        <thead>
            <tr>
                <th>Приложение</th>
                <th>Email пользователя</th>
                <th width="40"></th>
            </tr>
        </thead>
 
        <tfoot>
            <tr>
                <th>Приложение</th>
                <th>Email пользователя</th>
                <th></th>
            </tr>
        </tfoot>
    </table>

	</td>
</tr>	
</table>


<%=JSPHelper.composeActionButton("Назад", "location.href = 'task.generator.gui'", "goup")%>
<%@ include file="../common/bottom.jsp" %>
