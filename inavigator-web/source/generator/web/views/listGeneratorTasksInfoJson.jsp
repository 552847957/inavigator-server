<%@page import="ru.sberbank.syncserver2.service.generator.single.OneCallablePerTagThreadPool"%>
<%@page import="ru.sberbank.syncserver2.service.generator.single.data.ActionState"%>
<%@page import="ru.sberbank.syncserver2.service.generator.single.data.ActionInfo"%>
<%@ page contentType="application/json; charset=UTF-8" pageEncoding="UTF-8" %>
<%@page import="java.util.List"%>
<%@page import="ru.sberbank.syncserver2.service.log.LogMsg"%>
<%@ page import="ru.sberbank.syncserver2.gui.util.TimeIntervalHelper" %>

<%
  List<ActionInfo> actionInfos = (List<ActionInfo>)pageContext.findAttribute("actionInfos");
%>

{"actionInfos":[ 
	<%  
	for(int i=0;i<actionInfos.size();i++) {
		ActionInfo actionInfo = actionInfos.get(i);
		java.util.Date previousDate = null;%>
		{
		"canStop":"<%=(actionInfo.getRunStatus()!= OneCallablePerTagThreadPool.RUN_STATUS.NONE && actionInfo.getRunStatus()!= OneCallablePerTagThreadPool.RUN_STATUS.CANCELLING)%>", 
		"canStart":"<%=(actionInfo.getRunStatus() == OneCallablePerTagThreadPool.RUN_STATUS.NONE)%>", 
		"canCopy":"<%=(actionInfo.isHasArchiveFile())%>",
		"canPublish":"<%=(actionInfo.getPublishedStatus().isCanPublish())%>", 
		"canDeleteDraft":"<%=(actionInfo.getPublishedStatus().isCanPublish())%>", 
		"canForcePublish":"<%=(actionInfo.isGenerationModeDraft())%>", 
		"application":"<%=actionInfo.getApplication()%>", 
		"dataFileName":"<%=actionInfo.getDataFileName()%>", 
		"status":"<%=actionInfo.getRunStatusDesc()%>", 
		"isGenerationModeDraft":"<%=actionInfo.isGenerationModeDraft()%>", 
		"publishStatus":"<%=actionInfo.getPublishedStatus() != null?actionInfo.getPublishedStatus().getStatusName():""%>", 
		"actionGenStates":[
		<%	java.text.DateFormat df = new java.text.SimpleDateFormat("dd.MM.yyyy HH:mm");
			   if (actionInfo.getActionGenStates() != null) {
				   int i1 = 0;				   
				   for(String ph:ActionState.getAllAlphaStates()) {%>
				   	{
					  <% if (actionInfo.getActionGenStates().containsKey(ph)) {
					     ActionState as = actionInfo.getActionGenStates().get(ph);%> 
						"colorFileName":"images/signals/<%=as.getSignalColorFileName() %>", 
						<% String duration = as.getStatusCode().equals(ActionState.STATUS_PERFORM)?TimeIntervalHelper.getFormatedTimeInterval(", длительность: ",previousDate):TimeIntervalHelper.getFormatedTimeInterval(", длительность: ",previousDate,as.getEventDate());%>
						"title":"<%=((as.getWebHostName()!=null)?(as.getWebHostName()+": "):"") + as.getStatusName() + (as.getEventDate()!=null?(" (" + df.format(as.getEventDate()) +duration+")"):"") %>"
					<% previousDate = as.getEventDate();
					} else {%>
						"colorFileName":"images/signals/black.png",	
						"title":"Не Выполнялось"	
					<% } %>
					}
					<%= (i1<ActionState.getAllAlphaStates().length-1)?",":"" %>
				<% i1++; } %>
			<% } %> 
		],
		"actionLoadStates": [
			<% if (actionInfo.getActionLoadStates() != null) {
				int i1= 0;
			   for(ActionState as:actionInfo.getActionLoadStates().values()) {%>
					{			   
				  <% if (as.getStatusCode() != null) {%> 
						"colorFileName":"images/signals/<%=as.getSignalColorFileName() %>", 
						<% String duration = as.getStatusCode().equals(ActionState.STATUS_PERFORM)?TimeIntervalHelper.getFormatedTimeInterval(", длительность: ",previousDate):TimeIntervalHelper.getFormatedTimeInterval(", длительность: ",previousDate,as.getEventDate());%>
						"title":"<%= as.getSigmaHost() + ": " + as.getStatusName() + (as.getEventDate()!=null?(" (" + df.format(as.getEventDate()) +duration+ ")"):"") %>"
					<% } else {%>
						"colorFileName":"images/signals/black.png",
						"title":"<%= as.getSigmaHost() %>: Не загружалось"
					<% } %>
					}		
				<%= (i1<actionInfo.getActionLoadStates().size()-1)?",":"" %>
				<% i1++; }} %>
		
		]
		
	}
		
	<%= (i<actionInfos.size()-1)?",":"" %>
	
	<% } %>
	]
}