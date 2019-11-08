<%@page import="ru.sberbank.syncserver2.service.generator.single.data.ActionState"%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>

<%@ page import="ru.sberbank.syncserver2.gui.util.JSPHelper" %>
<%@ page import="ru.sberbank.syncserver2.service.generator.single.data.ActionInfo" %>
<%@ page import="java.util.List" %>
<%@ page import="ru.sberbank.syncserver2.service.generator.single.OneCallablePerTagThreadPool" %>
<%@ page import="ru.sberbank.syncserver2.gui.util.TimeIntervalHelper" %>
<%@ page import="java.util.Date" %>

<% final String PAGE_TITLE = "Sync Generator - Задачи"; %>
<% final String SECTION_TITLE = "Sync Generator - Задачи"; %>
<% request.setAttribute("PAGE_TYPE", "GENERATOR_TASKS"); %>
<%@ include file="top.jsp" %>

<script src= "js/generator.js"> </script> 


<script>
function checkShutDownDraftMode(draftExists) {
	if (draftExists) { 
		alert("Переключение в режим публикации возможно только  при отсутсвии черновика. Необходимо или опубликовать или удалить черновик.");
		return false;
	}	
	return confirm('Вы уверены, что хотите отключить режим черновика? Продолжать не рекомендуется.'); 
}

</script>

<%String activeNodeMessage = (String)pageContext.findAttribute("activeNodeMessage"); %>
<%if(activeNodeMessage!=null){ %><br>
<%=activeNodeMessage%> 
<%} %>
<br>
<a align="right" href="show.contentcontrol.gui">Список контроллеров контента</a>
<table border=0 cellpadding=10 cellspacing=10 id="taskTable">

<%
  String tag = (String) pageContext.findAttribute("tag");
%>

<tr>
    <td colspan="10"><hr/></td>
</tr>
<%
  List<ActionInfo> actionInfos = (List) pageContext.findAttribute("actionInfos");
  String previousApp = "";
  for(int i=0; i< actionInfos.size(); i++){
      ActionInfo actionInfo = actionInfos.get(i);
      Date previousDate = null;

      if(i==0 || !previousApp.equalsIgnoreCase(actionInfo.getApplication())){ %>
    <tr class="listData applicationHeader">
        <td colspan="10" bgcolor="#adff2f"><%=actionInfo.getApplication()%></td>
     </tr>
<tr class="listHeader">
  <th align="left">Файл</th>
  <th align="left">Тип выполнения</th>
  <th align="left">Статус</th>
  <th align="left">Режим генерации</th>
  <th align="left">Статус публикации</th>
  <th align="left">Подключение к БД</th>
  <th align="left">Генерация файла</th>
  <th align="left">Отправка в Sigma</th>
  <th align="left">Кэширование в Sigma</th>
  <th align="left">Действия</th>
</tr>


<%       previousApp = actionInfo.getApplication();
      }

      String dataFileName = actionInfo.getDataFileName();
      if(dataFileName.toLowerCase().endsWith(".sqlite")){
        dataFileName = dataFileName.substring(0, dataFileName.length()-7);
      }
%>
<tr class="listData" id="<%=actionInfo.getApplication() + "_" + actionInfo.getDataFileName()%>">
    <td><%=dataFileName%></td>
    <td>
    <% if (actionInfo.isAutoRun()) { %>
 	   <%="<a title=\"" + (actionInfo.isAutoGenEnabled()?"Нажмите чтобы отключить":"Нажмите чтобы включить") + "\" style=\"text-decoration:none;" + (actionInfo.isAutoGenEnabled()?"color:#0A6B31":"color:silver")+"\" href=\"changeautogen.generator.gui?appCode=" +actionInfo.getApplication() + "&fileName=" + actionInfo.getDataFileName() + "&enabled="+(actionInfo.isAutoGenEnabled()?"false":"true")+"\">" + (actionInfo.isAutoGenEnabled()?"Автоматически (Активно)" : "Автоматически (Неактивно)")+ "</a>"  %> </td>
 	<% } else { %>
 	<%="Ручное"%>
 	<% } %>
    <td><%=actionInfo.getRunStatusDesc()  %></td>
    <td><%="<a class=\"modeLink\" " + ((actionInfo.isGenerationModeDraft())?" onclick=\"return checkShutDownDraftMode(" + actionInfo.getPublishedStatus().isCanPublish() + ");\" ":"") + " title=\"" + (actionInfo.isGenerationModeDraft()?"Нажмите чтобы отключить черновик":"Нажмите чтобы включить черновик") + "\" style=\"text-decoration:none;" + (!actionInfo.isGenerationModeDraft()?"color:#0A6B31":"color:silver")+"\" href=\"changegenmode.generator.gui?appCode=" +actionInfo.getApplication() + "&fileName=" + actionInfo.getDataFileName() + "&enabled="+(actionInfo.isGenerationModeDraft()?"false":"true")+"\">" + (actionInfo.isGenerationModeDraft()?"Черновик" : "Публикация")+ "</a>"  %> </td>
    <td><%=(actionInfo.getPublishedStatus() != null)?actionInfo.getPublishedStatus().getStatusName():"" %></td>
	     
	  <% 
	   java.text.DateFormat df = new java.text.SimpleDateFormat("dd.MM.yyyy HH:mm");
	   if (actionInfo.getActionGenStates() != null) {	   
	   for(String ph:ActionState.getAllAlphaStates()) {%>
   		<td>
		  <% if (actionInfo.getActionGenStates().containsKey(ph)) {
		     ActionState as = actionInfo.getActionGenStates().get(ph);%>		
		     <% String duration = as.getStatusCode().equals(ActionState.STATUS_PERFORM)?TimeIntervalHelper.getFormatedTimeInterval(", длительность: ",previousDate):TimeIntervalHelper.getFormatedTimeInterval(", длительность: ",previousDate,as.getEventDate());%>     
			<img src="images/signals/<%=as.getSignalColorFileName() %>" title="<%=((as.getWebHostName()!=null)?(as.getWebHostName()+": "):"") + as.getStatusName() + (as.getEventDate()!=null?(" (" + df.format(as.getEventDate()) +duration + ")"):"") %>" />
			<% previousDate = as.getEventDate();%> 
		<% } else {%>
			<img src="images/signals/black.png"	title="Не Выполнялось" />	
		<% } %>
	   	</td>
		<% } %>
	<% } else {%>
		<td colspan="3"/>		
	<% } %>
	<td>
	<% if (actionInfo.getActionLoadStates() != null) {
	   for(ActionState as:actionInfo.getActionLoadStates().values()) {
		   if (as.getStatusCode() != null) {%> 
		   <% String duration = as.getStatusCode().equals(ActionState.STATUS_PERFORM)?TimeIntervalHelper.getFormatedTimeInterval(", длительность: ",previousDate):TimeIntervalHelper.getFormatedTimeInterval(", длительность: ",previousDate,as.getEventDate());%>
			<img src="images/signals/<%=as.getSignalColorFileName() %>" title="<%= as.getSigmaHost() + ": " + as.getStatusName() + (as.getEventDate()!=null?(" (" + df.format(as.getEventDate()) +duration+")"):"") %>" />
		<% } else {%>
			<img src="images/signals/black.png" title="<%= as.getSigmaHost() %>: Не загружалось" />		
		<% }} %>
	<% } %>
	</td>


    <td>
        <table>
            <tr>
                <% 
                	boolean canStop = (actionInfo.getRunStatus()!= OneCallablePerTagThreadPool.RUN_STATUS.NONE && actionInfo.getRunStatus()!= OneCallablePerTagThreadPool.RUN_STATUS.CANCELLING);
                	boolean canRun =  (actionInfo.getRunStatus() == OneCallablePerTagThreadPool.RUN_STATUS.NONE);
                	boolean canCopy = (actionInfo.isHasArchiveFile());
                	boolean canPublish = (actionInfo.getPublishedStatus().isCanPublish());
                	boolean canDeleteDraft = (actionInfo.getPublishedStatus().isCanPublish());
                	boolean forsePublish = (actionInfo.isGenerationModeDraft());
                %>
                <td><a class="stopButtonClass" style="<%=canStop?"":"display:none" %>" href="stop.generator.gui?dataFileName=<%=actionInfo.getDataFileName()%>" title="Остановить генерацию файла"><img src="images/buttons/button_task_stop.png" /></a></td>
                <td><a class="startButtonClass" style="<%=(!canStop && canRun)?"":"display:none" %>" href="start.generator.gui?dataFileName=<%=actionInfo.getDataFileName()%>" title="Генерация файла из MIS Портала <%=actionInfo.getJndiLastComponent()%> и отправка на мобильное устройство"><img src="images/buttons/button_task_start.jpg" /></a></td>
                <td><a class="forsePublisButtonClass" style="<%=(forsePublish && canRun && !canStop)?"":"display:none" %>" onclick="return confirm('Запуск принудительной публикации в режиме черновика не рекомендован к использованию. Вы уверены, что хотите продолжить?');" href="forsepublish.generator.gui?dataFileName=<%=actionInfo.getDataFileName()%>&appCode=<%=actionInfo.getApplication()%>" title="Запустить генерацию напрямую в статус 'Опубликовано'"><img  width="38" src="images/buttons/button_task_forse_publish.png" /></a></td> 
                <td><a class="logsButtonClass" href="logs.generator.gui?dataFileName=<%=actionInfo.getDataFileName()%>"  title="Журнал генерации файла"><img src="images/buttons/button_task_journal.png" /></a></td>
                <td><a class="copyButtonClass" style="<%=canCopy?"":"display:none" %>" href="copyagain.generator.gui?dataFileName=<%=actionInfo.getDataFileName()%>" title="Повторная отправка ранее сгенерированного файла на мобильное устройство"><img src="images/buttons/button_task_copyagain.jpg" /></a></td>
                <td><a class="publishButtonClass" style="<%=canPublish?"":"display:none" %>" onclick="return confirm('Вы уверены, что хотите опубликовать текущий черновик? После публикации изменения будут доступны всем пользователям.');" href="publishdraft.generator.gui?dataFileName=<%=actionInfo.getDataFileName()%>&appCode=<%=actionInfo.getApplication()%>" title="Опубликовать текущий черновик"><img src="images/buttons/button_task_publish.jpg" /></a></td>
                <td><a class="deleteDraftButtonClass" style="<%=canDeleteDraft?"":"display:none" %>" onclick="return confirm('Вы уверены, что хотите удалить текущий черновик?');" href="deletedraft.generator.gui?dataFileName=<%=actionInfo.getDataFileName()%>&appCode=<%=actionInfo.getApplication()%>" title="Удалить текущий черновик"><img src="images/buttons/button_task_deletedraft.jpg" /></a></td>
            </tr>
        </table>
    </td>
</tr>
<%
  }
%>
</table>
<%=JSPHelper.composeActionButton("Назад", "location.href = 'tags.logs.gui'", "goup")%>
<script>
$(document).ready(function() {
    var TIMEOUT_SECONDS = 5; 
    setTimeout(function(){callRequestTaskInfo(TIMEOUT_SECONDS)}, TIMEOUT_SECONDS * 1000);
    window.location.hash="DETAILS";    
});

</script>
<%@ include file="../common/bottom.jsp" %>
