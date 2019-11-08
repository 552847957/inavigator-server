<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List" %>
<%@ page import="java.lang.Boolean" %>


<% final String PAGE_TITLE = "Режим технической поддержки"; %>
<% final String SECTION_TITLE = "Режим технической поддержки"; %>
<% request.setAttribute("PAGE_TYPE", "MAINTENANCE"); %>
<% 
	Boolean active = (Boolean) pageContext.findAttribute("active");
	String emails = (String) pageContext.findAttribute("emails");
%>

<%@ include file="top.jsp" %>

<script>
function addRow(initRow, value) {
    var newRow = initRow.clone().removeAttr('id').addClass('new').insertBefore(initRow),
        deleteRow = $('<a class="rowDelete"><img src="../images/delete_label.png"></a>');
   
    newRow
        .append(deleteRow)
        
        .slideDown(100, function() {
            $(this).find('input').val(value).focus();
        })
		.on('click', 'a.rowDelete', function() {
            removeRow(newRow);
        })
}
function refresh() {
	$('.new').remove();
	$('textarea').val('');
}
        
function removeRow(newRow) {
    newRow
        .slideUp(200, function() {
            $(this)
                .next('div:not(#initRow)')
                    .find('input').focus()
                    .end()
                .end()
                .remove();
        });
}

function send() {
	$('#maintenanceForm').find('.new>input, a, #initRow>input, textarea').prop( "disabled", false);
}

$(function () {
    var initRow = $('#initRow');
    
    initRow.on('focus', 'input', function() {
        addRow(initRow, '');
    });
	initRow.on('change', 'input', function() {
        $(this).val('');
    });
	$( "input[type=checkbox]" ).on( "change", function() {
		if ($(this).prop("checked")) {
			$('#maintenanceForm').find('.new>input, a, #initRow>input, textarea').prop( "disabled", false);
		} else {
			$('#maintenanceForm').find('.new>input, a, #initRow>input, textarea').prop( "disabled", true);
		}
	});
	<% if (emails != null && !emails.trim().equals(""))
		for (String email: emails.split(";")) {%>
			addRow(initRow, '<%=email %>');
	<%	} %>
	$("input[type=checkbox]").change();
});
</script>
<style>
#maintenanceForm {
    padding: 10px;        
}

#maintenanceForm > div {
    padding: 5px;
    width: 100%;
}

div.new {
    background-color: rgb(230,230,230);
    border-bottom: 1px dotted #ccc;
    display: none;
}

input {
    border: 1px solid #eee;
    border-radius: 4px;
    line-height: 1.5em;
    padding: 4px;
	
}
.emailField {
	width: 40%;
}

a.rowDelete {
    cursor: pointer;
    padding-left: 9px;
}
</style>
<form id="maintenanceForm" method="POST" action="save.maintenance.gui" >
	<p>Активировать режим тех. поддержки: <input type="checkbox" name="active" <%= active ? "checked" : "" %> ></p>
	<br> <br>
	Текст сообщения:
	<p><textarea rows="10" cols="60" name="message"><%= pageContext.findAttribute("message") != null ? pageContext.findAttribute("message") : "" %></textarea></p>
	Список разрешенных пользователей:
    <div id="initRow">
        <input class="emailField" type="text" name="emails" placeholder="E-Mail">
    </div>
	<p><input type="submit" value="Сохранить" onclick="send()">
   <input type="button" value="Очистить" onclick="refresh()"></p>
</form>

<%@ include file="../common/bottom.jsp" %>
