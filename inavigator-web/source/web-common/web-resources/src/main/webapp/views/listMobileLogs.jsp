<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>

<%@ page import="java.util.List" %>
<%@ page import="ru.sberbank.syncserver2.gui.util.ButtonHelper" %>
<%@ page import="ru.sberbank.syncserver2.gui.util.JSPHelper" %>

<% final String PAGE_TITLE = "Клиентские логи"; %>
<% final String SECTION_TITLE = "Клиентские логи"; %>
<% request.setAttribute("PAGE_TYPE", "MOBILE_LOGS"); %>
<%@ include file="top.jsp" %>
<%
	List<String> models = (List<String>) pageContext.findAttribute("models");	
	List<String> emails = (List<String>) pageContext.findAttribute("emails");
	
%>
<link rel="stylesheet" href="../css/jquery.datetimepicker.css"/>
<link rel="stylesheet" href="../css/jquery.dataTables.css"/>
<link rel="stylesheet" href="../css/dataTables.tableTools.min.css"/>
<script src="../js/jquery.dataTables.min.js"></script>
<script src="../js/dataTables.tableTools.min.js"></script>
<script src="../js/jquery.datetimepicker.js"></script>
<script src="../js/table.initconfig.js"></script>
<script type="text/javascript">
function clFn() {
	$('#clear').find('input').val('').change();
	$('#deviceModel').val('').change();
};

function putPar(d) {
	d.table="true";
	d.model=$("#deviceModel").val();
	d.email=$('#Email').val();
}

$(document).ready(function() { 
	var params={};
	var col = [
	           {
	        	"class": "details-control",
	        	"orderable":      false,
	        	"data":           null,
	        	"defaultContent": ""
	        	},               
               { data: "7", orderable:true},
               { data: "9", orderable:false},
               { data: "11", orderable:false},
               { data: "1", orderable:false},
               { data: "3", orderable:false},
               { data: "15", orderable:false, 
            	   render: function ( data, type, full, meta ) {
            		   if (full[15])  {
            			   return type === 'display' && full[15].length > 40 ? full[15].substr( 0, 38 )+'...' : full[15];
            		   } else {
            			   return null;
            		   }             		             	
            	   }
               }
               ];
	params.init = $('#example');
	params.addParFn = putPar;
	params.url = "logs.mobile.gui";
	params.clearFn = clFn;
	params.columns = col;
	params.inputs = $('#insinp');
	params.butImport = false;
	$('#date1').datetimepicker({
		format:'d.m.Y H:i',
		timepicker:true,
		step: 5
		});
	$('#date2').datetimepicker({
		format:'d.m.Y H:i',
		timepicker:true,
		step: 5
		});	
	var table = initTable(params);
	table.order( [ 1, 'asc' ] ).draw();	
	
	function format ( d ) {
	    return 'Идентификатор источника информации об устройстве: '+d[0]+'<br>'+
	        'Версия iOS: '+d[2]+'<br>'+
	        'Идентификатор приложения: '+d[4]+'<br>'+
	        'Время последнего обновления: '+d[5]+'<br><br>'+
	        'Идентификатор источника события: '+d[6]+'<br>'+
	        'Часовой пояс: '+d[8]+'<br>'+
	        'Тип события: '+d[10]+'<br>'+
	        'IP адресс: '+d[12]+'<br>'+
	        'DataServer: '+d[13]+'<br>'+
	        'DistribServer: '+d[14]+'<br>'+
	        'Сервер конфигурации: '+d[17]+'<br>'+
	        'Информация о событии: '+d[15]+'<br>'+
	        'ErrorStackTrace: '+d[16];
	}
    var detailRows = [];    
    $('#example tbody').on( 'click', 'tr td:first-child', function () {
        var tr = $(this).closest('tr');
        var row = table.row( tr );
        var idx = $.inArray( tr.attr('id'), detailRows );
 
        if ( row.child.isShown() ) {
            tr.removeClass( 'details' );
            row.child.hide(); 
            detailRows.splice( idx, 1 );
        }
        else {
            tr.addClass( 'details' );
            row.child( format( row.data() ) ).show(); 
            if ( idx === -1 ) {
                detailRows.push( tr.attr('id') );
            }
        }
    } ); 
    table.on( 'draw', function () {
        $.each( detailRows, function ( i, id ) {
            $('#'+id+' td:first-child').trigger( 'click' );
        } );
    } );
    $(".send").click(function(){	
        var email = $("#Email").val();
        if (confirm("Очистить все логи пользователя "+email))
        $.ajax({
           type: 'GET',
           url: "clear.mobile.gui",
           data: {"email": email, ajax: true},
           cache: false,
           success: function(response){
        	   clFn();
        	   table.draw();
           },
           error: function(xhr, str, e){
          	 alert("Ошибка при отправке данных");
           }
        });
        return false;                                                                 
      });
    $("#Email").change(function() {
    	clFn();
    	table.draw();
    });

});
</script>

<table border=0 cellpadding=0 cellspacing=0 width=100%>
<tr class="listHeader">
  <th align="left">Клиентские логи </th>
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
	<form method="GET" action="clear.mobile.gui" >			
		Email:&nbsp;<select id="Email" name="email">
			<% for (String s: emails)  {%>
			<option <%= s.equals(request.getParameter("email"))?"selected":"" %> value="<%=s %>"><%=s %></option> 	
			<%} %>
		</select>
	&nbsp;&nbsp;&nbsp;&nbsp;<input  type="submit" value="очистить все" class="send"/>
	</form>
		<br>
	<div id='clear'>	
	<span id='insinp'>
		Дата с: <input type="text" id="date1" placeholder="поиск по дате" />
		Дата&nbsp;по:&nbsp;<input type="text" id="date2" placeholder="поиск по дате" />	
		Модель&nbsp;устройства:&nbsp;<select id="deviceModel"> 
		<option></option>
		<% for (String s: models)  {%>
			<option><%=s %></option> 	
			<%} %>
		</select>
		Версия&nbsp;приложения:&nbsp;<input type="text" placeholder="поиск по версии" />	
	</span>	
	</div>	
	<br>
	<table id="example" class="display" width="100%" cellspacing="0">
        <thead>
            <tr>
            	<th></th>
                <th>Время события</th>
                <th>Email</th>
                <th>Описание события</th>
                <th>Модель устройства</th>
                <th>Версия приложения</th>
                <th>Информация о событиии</th>
            </tr>
        </thead>
 
        <tfoot>
            <tr>
            	<th></th>
                <th>Время события</th>
                <th>Email</th>
                <th>Описание события</th>
                <th>Модель устройства</th>
                <th>Версия приложения</th>
                <th>Информация о событиии</th>
            </tr>
        </tfoot>
    </table>

	</td>
</tr>	
</table>


<%=JSPHelper.composeActionButton("Назад", "location.href = 'mode.mobile.gui'", "goup")%>
<%@ include file="../common/bottom.jsp" %>
