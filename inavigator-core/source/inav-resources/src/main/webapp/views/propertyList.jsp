<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>

<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>
<%@ page import="ru.sberbank.syncserver2.gui.util.ButtonHelper" %>
<%@ page import="ru.sberbank.syncserver2.gui.util.JSPHelper" %>
<%@ page import="ru.sberbank.syncserver2.gui.data.SyncConfig" %>

<% final String PAGE_TITLE = "Настройки"; %>
<% final String SECTION_TITLE = "Настройки"; %>
<% request.setAttribute("PAGE_TYPE", "SETTINGS"); %>
<%@ include file="top.jsp" %>
<script type="text/javascript">
$(function(){
   $(".send").click(function(){	
      var form = $(this).closest('form').find('input');
      var key = form.val();
      var val = form.next().val();
      var statusMsg;
      $.ajax({
         type: 'POST',
         url: "update.properties.gui",
         data: {"propertyKey": key, "propertyValue": val, json: true},
         cache: false,
         success: function(response){
             statusMsg=response;
         },
         error: function(xhr, str, e){
        	 statusMsg="Ошибка при отправке данных";
         },
         complete: function(xhr, str){
             form.parent().find('span').text(statusMsg).animate({opacity:1}).delay(1500).animate({opacity:0});
         },
         dataType: "json"
      });
      return false;
                                                               
    });
   $(".mini-tag").click(function() {
	   var serv = $(this).text().split('/', 2);
	   window.location.assign("list.services.gui?folder="+serv[0]+"#"+serv[1]);
	   
   });
});
</script>
<style>
span.mini-tag {
    border-radius: 4px;
    background-color: #1a642a !important;
    color: #ffffff !important;
    margin: 5px;
    padding: 0px 4px;
    cursor:pointer;
}
span.mini-tag:hover {
	background-color: #459e00 !important;	
}
</style>

<div id="groups">
<%
	Map<String, List<SyncConfig>> properties = (Map<String, List<SyncConfig>>) pageContext.findAttribute("properties");
	for (String group: properties.keySet()) {
%>
<h3><%=group %></h3>
<div>
<table  width="100%" border=1 cellpadding=7 cellspacing=0>
<tr class="listHeader" width="100%">
  <th align="left">Настройка</th>
  <th align="left">Зависимые сервисы</th>
  <th align="left">Значение</th>
</tr>
<%
  
  for(SyncConfig property: properties.get(group)){
    String key = property.getPropertyKey();
    String inputType = key.toLowerCase().contains("password") ? "password":"text";

%>
<tr class="listData">
  <td><%=property.getPropertyKey() %></td>
  <td><%for (String s: property.getServices()) {	%>
	  <span class="mini-tag"><%=s %></span> 	
		<%  } %>
  </td>
  <td>
      <form action="update.properties.gui" method="post" >
          <table border="0" width="100%">
          <tr>
              <td nowrap="nowrap" >
              <input type="hidden" name="propertyKey" value="<%=property.getPropertyKey()%>">
              <input type="<%=inputType%>" name="propertyValue" value="<%=property.getPropertyValue()%>" size="80" />
              
              <input type="submit" value="Сохранить" class="send"> <span style="opacity:0; width:250px; display:inline-block;"></span>
              </td>
          </tr>
          <tr>
              <td><%=property.getPropertyDesc() %></td>              
          </tr>
          </table>
      </form>
  </td>
</tr>

<%} %>
</table>
</div>
<%}%>
</div>
<script>
$( "#groups" ).accordion({
	active: false,
	collapsible: true,
	activate: function( event, ui ) {
		if (ui.newPanel.attr('id'))
			location.hash='#'+(ui.newPanel.attr('id').substring(6)/2 - 1);
	},
	create: function( event, ui ) {
		var open = location.hash.substring(1);
		if (open != '')
			$( "#groups" ).accordion( "option", "active", parseInt(open) );
	}
});
</script>
<%=JSPHelper.composeActionButton("Назад", "location.href = 'welcome.public.gui'", "goup")%>
<%@ include file="../common/bottom.jsp" %>
