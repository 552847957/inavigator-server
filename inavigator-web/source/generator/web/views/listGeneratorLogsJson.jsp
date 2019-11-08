<%@ page contentType="application/json; charset=UTF-8" pageEncoding="UTF-8" %>
<%@page import="java.util.List"%>
<%@page import="ru.sberbank.syncserver2.service.log.LogMsg"%>

<%
  String dataFileName = (String) pageContext.findAttribute("dataFileName");
  List<LogMsg> logs = (List) pageContext.findAttribute("logs");
  Boolean debug = (Boolean) pageContext.findAttribute("debug");
  String lastLogId = (String) pageContext.findAttribute("lastLogId");
%>

{"logs":[ 
	<%  
	for(int i=0;i<logs.size();i++) {
		LogMsg msg = logs.get(i); %>
		{"eventTime":"<%=msg.getEventTime() %>", "eventDesc":"<%=msg.getEventDesc() %>",  "eventType":"<%=msg.getEventType() %>",  "eventId":"<%=msg.getId() %>"}<%= (i<logs.size()-1)?",":"" %>
	<% } %>
	],
	"dataFileName":"<%=dataFileName %>",
	"debug":"<%=debug %>",
	"lastLogId":"<%=lastLogId %>"
}