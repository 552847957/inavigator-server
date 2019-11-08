<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>

<%@ page import="java.util.List" %>
<%@ page import="ru.sberbank.syncserver2.gui.util.ButtonHelper" %>
<%@ page import="ru.sberbank.syncserver2.gui.util.JSPHelper" %>
<%@ page import="ru.sberbank.syncserver2.gui.data.SyncConfig" %>
<%
  String appBundle = (String)pageContext.findAttribute("appBundle");
%>
<% final String PAGE_TITLE = "Настройки приложений"; %>
<% final String SECTION_TITLE = "Версии для "+appBundle; %>
<% request.setAttribute("PAGE_TYPE", "CLIENT_SETTINGS"); %>
<%@ include file="top.jsp" %>

<style>
	.ui-widget {
	    font-size: 12px;
	}
</style>

<script>

// после загрузки создаем диалог
$(function() {
	 
	$( "#confirmDialog" ).dialog({
		 autoOpen: false,
		 height: 120,
		 width: 350,
		 modal: true,
		 buttons: {
		 "Сохранить копию":function() {
			 var id = $( "#confirmDialog" ).prop('currentFieldId');
			 document.getElementById(id).value = $("#versionNumber").val()
			 $( "#confirmDialog" ).prop('currentForm').submit();
			 $( "#confirmDialog" ).dialog( "close" );
		 } ,
		 'Отмена': function() {
			 $("#versionNumber").val('');
			 $( "#confirmDialog" ).dialog( "close" );
		  }
		 },
		 close: function() {
			 $("#versionNumber").val('');
			 $( "#confirmDialog" ).dialog( "close" );
		 }
	});
	
});	 

// Функция запроса номера новой конфигурации для  копирования настроек
function fillVersionNumber(form,field) {
	$( '#confirmDialog' ).prop('currentForm',form);
	$( '#confirmDialog' ).prop('currentFieldId',field);
	$( '#confirmDialog' ).dialog( "open" );
	return false;
}	 

</script>

<table border=0 cellpadding=0 cellspacing=0>
<tr class="listHeader">
  <th align="left">Версия</th>
  <th align="left"></th>
</tr>
<%
  List versions = (List) pageContext.findAttribute("versions");
  for(int i=0; i<versions.size(); i++){
    String appVersion = (String) versions.get(i);
%>
<tr class="listData">
  <td><%=appVersion%></td>
  <td>
      <table><tr>
          <td>
              <form action="properties.clientproperties.gui" method="get">
                  <input type="hidden" name="appBundle" value="<%=appBundle%>">
                  <input type="hidden" name="appVersion" value="<%=appVersion%>">
                  <input type="submit" value="Изменить настройки">
              </form>
         </td>
          <td>
              <form action="delete.clientproperties.gui" method="post">
                  <input type="hidden" name="appBundle" value="<%=appBundle%>">
                  <input type="hidden" name="appVersion" value="<%=appVersion%>">
                  <input type="submit" value="Удалить">
              </form>
              </nobr>
          </td>
          <td>
              <form action="copy.clientproperties.gui" method="get">
                  <input type="hidden" name="appBundle" value="<%=appBundle%>">
                  <input type="hidden" name="fromAppVersion" value="<%=appVersion%>">
                  <input type="hidden" name="toAppVersion" id="toAppVersion_<%=appVersion%>" value="1">
                  <input type="submit" value="Копировать" onclick="return fillVersionNumber(this.form,'toAppVersion_<%=appVersion%>');">
              </form>
              </nobr>
          </td>
      </tr></table>
  </td>
</tr>
<%
  }
%>
<tr class="listData">
<td colspan="2">
      <form action="add.clientproperties.gui" method="post">
          <input type="hidden" name="appBundle" value="<%=appBundle%>">
          <input type="text" name="appVersion" value="">
          <input type="submit" value="Добавить">
      </form>
</td>
</tr>
</table>

<div id="confirmDialog" title="Создание копии настроек">
	<form>
		Номер версии: <input id="versionNumber" />
	</form>
</div>

<%=JSPHelper.composeActionButton("Назад", "location.href = 'apps.clientproperties.gui'", "goup")%>
<%@ include file="../common/bottom.jsp" %>
