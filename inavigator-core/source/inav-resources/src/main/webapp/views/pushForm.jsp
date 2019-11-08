<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List" %>

<% final String PAGE_TITLE = "Отправка Push уведомлений"; %>
<% final String SECTION_TITLE = "Форма отправки уведомлений"; %>
<% request.setAttribute("PAGE_TYPE", "PUSH_NOTIFICATIONS"); %>
<% String contextPath = getServletContext().getContextPath();  %>

<%@ include file="top.jsp" %>

<style>
select {
	color:black;
    font-size: 16px;
    padding: 2px 2px;
    width: 378px;
    *background: #58B14C;
}

</style>

 
<script>
function resetList(classes,isMulti) {
    for (var i = 0; i < classes.length; i++) {
    	$("."+classes[i]).html("");
    }
}
	
function initList(componentClass,url,querystring,emptyElement) {
	$.ajax({
		url: url + querystring,
		dataType: 'json'
	}).done( function( data ) {
		if (emptyElement)
			output = '<option value=""></option>';
		else
			output = '';
	    for (var i = 0; i < data.results.length; i++) {
	    	output = output + '<option value=' + data.results[i].code + '>' + data.results[i].value + '</option>';
		}		
		$("."+componentClass).html(output);
	});
}

function validate() {
	if ($(".message").val() == '') {
		alert("Необходимо ввести текст сообщения");
		return false;	
	}
	/* if ($(".message").val().length > 230) {
		alert("Сообщение не должно превышать 230 символов.");
		return false;	
	} */
		
	return true;
}


$(document).ready(function()  {
	resetList(['combo-os','combo-app','combo-vers'],false);
	resetList(['combo-email','combo-device'],true);
	initList('combo-email', '<%=contextPath %>/gui/push/list/emails',"",true);   
	initList('combo-os', '<%=contextPath %>/gui/push/list/os', "",true);

	$('.combo-email').change( function(){
		resetList(['combo-device'],true);
		resetList(['combo-os','combo-app','combo-vers'],false);
		initList('combo-device', '<%=contextPath %>/gui/push/list/devices',"?email=" + encodeURIComponent($(".combo-email").val()), false);
		initList('combo-os', '<%=contextPath %>/gui/push/list/os', "",true);
	});
	$('.combo-os').change( function(){
		resetList(['combo-email','combo-device'],true);
		resetList(['combo-app','combo-vers'],false);
		initList('combo-app', '<%=contextPath %>/gui/push/list/applications',"?os=" + encodeURIComponent($(".combo-os").val()),true);
		initList('combo-email', '<%=contextPath %>/gui/push/list/emails',"",true);   
	});
	$('.combo-app').change( function(){
		resetList(['combo-vers'],false);
		initList('combo-vers', '<%=contextPath %>/gui/push/list/versions',"?os=" + encodeURIComponent($(".combo-os").val())+"&app=" + encodeURIComponent($(".combo-app").val()),true);
	});
})



</script>

<font color="red"> <%=pageContext.findAttribute("message") != null ?pageContext.findAttribute("message"):"" %> </font>
<a href="messages.push.gui">История сообщений</a>

<form name="sendForm" method="POST" action="" >
	<input type="hidden" name="data" value="data"></input>
	<table   style="width:100%">
    <tr>
		<td width="200px">Выберите операционную систему: </td>
		<td width="30%"><select name="os" class="combo-os"></select></td>

		<td colspan="2" rowspan="4" valign="top">
			<table width="80%">
				<tr>
					<td width="200px">Выберите Email:</td>
					<td><select name="emails" class="combo-email"> </select></td>
				</tr>
				<tr>
					<td>Выберите Устройство:</td>
					<td><select  multiple="multiple" name="devices[]" class="combo-device"></select></td>
				</tr>
			</table>
		</td>
	</tr>
	<tr>
		<td>Выберите приложение: </td>
		<td><select name="app" class="combo-app"></select></td>
	</tr>
	<tr>
		<td>Выберите версию приложения: </td>
		<td><select name="vers" class="combo-vers"></select></td>
		<td ></td>
	</tr>
	<tr>
		<td>Текст сообщения: </td>
		<td colspan="1"><textarea class="message" cols="80" name="message" rows="15"></textarea></td>
	</tr>
	<tr>
		<td colspan="4"><button type="submit" onclick="return validate();">Отправить</button></td>
	</tr>
	</table>
</form>


<%@ include file="../common/bottom.jsp" %>
