<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.lang.String" %>
<%@ page import="java.util.List" %>
<!DOCTYPE script PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
	<meta http-equiv="X-UA-Compatible" content="IE=edge">

	<title>Онлайн мониторинг</title>

	<script src="../js/jquery-1.12.0.js" type="text/javascript"></script>

	<style>
#content-svg {
	margin-left: auto;
	margin-right: auto;
	display: block;
	width: 80%; 
	height: 450px;
}

#errors {
	color: #FF0000;
	width: 80%;
	min-height: 100px;
	margin-left: 5%;
	/* outline: 2px solid #000; */
}
#times {
	margin-left: 5%;
	margin-top: 3%;
}
#svgWrapper {
  	align-items: center;
  	justify-content: center;
    vertical-align: middle;
}
#checkers {
  	margin-left: 5%;
  	width: 80%;
}
	</style>
</head>
<body id='body-st' >
<% 	String[] targets = (String[]) pageContext.findAttribute("targets");
	boolean ldap = (Boolean) pageContext.findAttribute("ldap");
	boolean pns = (Boolean) pageContext.findAttribute("pns");
	List<String> names = (List<String>) pageContext.findAttribute("checkers");
	if (targets != null) { 
	String ids = "";
	String nodes = "";
	for (String target: targets) {
		ids +=",'#"+target+"'";
		nodes += ",new Node('"+target+"', null)";
	}
	nodes = nodes.substring(1);
	%>
	<script>

var ids = ['#syncserver','#ldap','#pns','#datapower','#proxyserver'<%=ids%>];

function Node(obj, nextNodes) {
    this.value = obj;
	this.next = nextNodes;
	this.marked = false;
    this.setMark = function(t) {
		if (t == this.value)
			this.marked = true;
		else
			if (this.next != null)
				this.next.forEach(function(item, i, arr) {
						item.setMark(t)
				});
	}
	this.getMarkedBehind = function(skipFirst) {
		var result = [];
		if (this.marked) {
			if (skipFirst)
				skipFirst = false;
			else
				result.push(this.value);
		}
		if (this.next != null)
				this.next.forEach(function(item, i, arr) {
						result = result.concat(item.getMarkedBehind(skipFirst));
				});
		return result;
	}
	this.clear = function() {
		this.marked = false;
		if (this.next != null)
			this.next.forEach(function(item, i, arr) {
					item.clear();
			});
	}		
}
var head = new Node('syncserver', [new Node('ldap',null), new Node('pns', null), new Node('datapower', [ new Node('proxyserver', [<%=nodes %>])])]);


function updateData(url, type) {
	$.ajax({
		url: url,
		type : type,
		contentType : "application/json",
		dataType : 'json'
	}).done(function (data) {
		clearStatus();
		head.clear();
		$('.syncserver').attr("fill", "#01D70A");
		data.result.forEach(function(item, i, arr) {
			$('.'+item[0]).attr("fill", "#C8D0DB");
			$('#'+item[0]).attr("fill", "red");
			head.setMark(item[0]);
		});
		$("#checkers td").each(function(){
			$( this ).attr("bgcolor", "#01D70A");
		});
		data.additionals.forEach(function(item, i, arr) {
			$('#'+item).attr("bgcolor", "red");
		});
		head.getMarkedBehind(true).forEach(function(item, i, arr) {
			$('#'+item).find("g").attr("visibility", "visible");				
		});
		//var formatter = new Intl.DateTimeFormat("ru", {day: "numeric",month: "numeric",year: "numeric",hour: "numeric",minute: "numeric",second: "numeric"});
		$('#lastUpdate').html(new Date(data.lastUpdate).toLocaleString());
		$('#dateNow').html(new Date().toLocaleString());
		
		setTimeout('refreshStatus()',7000);
		
	}).fail(function (jqXHR, textStatus, errorThrown) {
		location.reload();
	});
};

function refreshStatus() {
	updateData("status.public-monitor.gui", "GET", null);	
}

function clearStatus() {
	ids.forEach(function(item, i, arr) {
		$(item).removeAttr("fill");
		$(item).find("g").attr("visibility", "hidden");
	});
	$('.syncserver').attr("fill", "#C8D0DB");
}

$(document).ready(function()  {
	refreshStatus();
})
</script>
	<div id="times">
		Время последнего обновления страницы: <span id="dateNow"></span> <br />
		Дата последней проверки: <span id="lastUpdate"></span>
		<br /><br /><br /><br /><br />
	</div>
	<div id="svgWrapper">
		<svg xmlns="http://www.w3.org/2000/svg" width="100%" height="50%"
			id="content-svg" viewBox="350 0 1450 400" version="1.1"
			preserveAspectRatio="xMinYMin meet"> <symbol id="arrow"
			width="200" height="100" viewBox="0 0 200 100"
			preserveAspectRatio="xMinYMin meet"> <polygon stroke="black"
			points="20,-20 0,0 20,20 20,10 100,10 100,20 120,0 100,-20 100,-10 20,-10"
			transform="translate(0,20)" /> </symbol> <symbol id="mssql" width="400"
			height="100" viewBox="-1 -1 400 100"
			preserveAspectRatio="xMinYMin meet"> <% if (targets.length==1) { %>

		<g id="<%=targets[0] %>"> <rect x="0" y="200" width="200"
			height="100" stroke="black" /> <text x="25" y="250" font-size="20px"
			fill="black">MSSQL <tspan x="25" dy="30">(<%=targets[0] %>)</tspan></text>
		<use x="200" y="230" xlink:href="#arrow" /> <g
			transform="matrix(1 0 0 1 450 380) scale(2.5)" visibility="hidden">
		<path fill="#C8D0DB"
			d="M-119.221-34.471h7.284v-7.04h-7.284V-34.471z M-128.111-59.051h7.04c0-1.348,0.393-2.647,1.178-3.898 
									c0.786-1.25,2.119-1.876,3.998-1.876c1.912,0,3.228,0.509,3.949,1.523c0.721,1.015,1.081,2.14,1.081,3.374  
									c0,1.072-0.324,2.055-0.972,2.948c-0.355,0.52-0.826,0.999-1.409,1.437l-1.772,1.389c-1.749,1.364-2.833,2.57-3.254,3.618
									c-0.421,1.047-0.681,2.944-0.777,5.688h6.603c0.016-1.299,0.12-2.257,0.315-2.875c0.309-0.975,0.931-1.827,1.871-2.559                                                 
									l1.725-1.34c1.749-1.363,2.933-2.484,3.548-3.361c1.053-1.445,1.578-3.225,1.578-5.335c0-3.443-1.214-6.041-3.642-7.796  
									c-2.428-1.754-5.477-2.631-9.146-2.631c-2.794,0-5.148,0.617-7.064,1.852C-126.301-66.96-127.917-63.679-128.111-59.051                                                 
									L-128.111-59.051z" />
		</g> </g> <% }  else if (targets.length==2) {%> <g id="<%=targets[0] %>"> <rect
			x="0" y="100" width="200" height="100" stroke="black" /> <text
			x="25" y="150" font-size="20px" fill="black">MSSQL <tspan
			x="25" dy="30">(<%=targets[0] %>)</tspan></text> <use x="230" y="130"
			xlink:href="#arrow" transform="rotate(35,200,130)" /> <g
			transform="matrix(1 0 0 1 450 280) scale(2.5)" visibility="hidden">
		<path fill="#C8D0DB"
			d="M-119.221-34.471h7.284v-7.04h-7.284V-34.471z M-128.111-59.051h7.04c0-1.348,0.393-2.647,1.178-3.898 
									c0.786-1.25,2.119-1.876,3.998-1.876c1.912,0,3.228,0.509,3.949,1.523c0.721,1.015,1.081,2.14,1.081,3.374  
									c0,1.072-0.324,2.055-0.972,2.948c-0.355,0.52-0.826,0.999-1.409,1.437l-1.772,1.389c-1.749,1.364-2.833,2.57-3.254,3.618
									c-0.421,1.047-0.681,2.944-0.777,5.688h6.603c0.016-1.299,0.12-2.257,0.315-2.875c0.309-0.975,0.931-1.827,1.871-2.559                                                 
									l1.725-1.34c1.749-1.363,2.933-2.484,3.548-3.361c1.053-1.445,1.578-3.225,1.578-5.335c0-3.443-1.214-6.041-3.642-7.796  
									c-2.428-1.754-5.477-2.631-9.146-2.631c-2.794,0-5.148,0.617-7.064,1.852C-126.301-66.96-127.917-63.679-128.111-59.051                                                 
									L-128.111-59.051z" />
		</g> </g> <g id="<%=targets[1] %>"> <rect x="0" y="300" width="200"
			height="100" stroke="black" /> <text x="25" y="350" font-size="20px"
			fill="black">MSSQL <tspan x="25" dy="30">(<%=targets[1] %>)</tspan></text>
		<use x="200" y="330" xlink:href="#arrow"
			transform="rotate(-35,200,330)" /> <g
			transform="matrix(1 0 0 1 450 480) scale(2.5)" visibility="hidden">
		<path fill="#C8D0DB"
			d="M-119.221-34.471h7.284v-7.04h-7.284V-34.471z M-128.111-59.051h7.04c0-1.348,0.393-2.647,1.178-3.898 
									c0.786-1.25,2.119-1.876,3.998-1.876c1.912,0,3.228,0.509,3.949,1.523c0.721,1.015,1.081,2.14,1.081,3.374  
									c0,1.072-0.324,2.055-0.972,2.948c-0.355,0.52-0.826,0.999-1.409,1.437l-1.772,1.389c-1.749,1.364-2.833,2.57-3.254,3.618
									c-0.421,1.047-0.681,2.944-0.777,5.688h6.603c0.016-1.299,0.12-2.257,0.315-2.875c0.309-0.975,0.931-1.827,1.871-2.559                                                 
									l1.725-1.34c1.749-1.363,2.933-2.484,3.548-3.361c1.053-1.445,1.578-3.225,1.578-5.335c0-3.443-1.214-6.041-3.642-7.796  
									c-2.428-1.754-5.477-2.631-9.146-2.631c-2.794,0-5.148,0.617-7.064,1.852C-126.301-66.96-127.917-63.679-128.111-59.051                                                 
									L-128.111-59.051z" />
		</g> </g> <% }  else if (targets.length==3) {%> <g id="<%=targets[0] %>"> <rect
			x="0" y="0" width="200" height="100" stroke="black" /> <text x="25"
			y="50" font-size="20px" fill="black">MSSQL <tspan x="25"
			dy="30">(<%=targets[0] %>)</tspan></text> <use x="250" y="30"
			xlink:href="#arrow" transform="rotate(50,200,30)" /> <g
			transform="matrix(1 0 0 1 450 180) scale(2.5)" visibility="hidden">
		<path fill="#C8D0DB"
			d="M-119.221-34.471h7.284v-7.04h-7.284V-34.471z M-128.111-59.051h7.04c0-1.348,0.393-2.647,1.178-3.898 
									c0.786-1.25,2.119-1.876,3.998-1.876c1.912,0,3.228,0.509,3.949,1.523c0.721,1.015,1.081,2.14,1.081,3.374  
									c0,1.072-0.324,2.055-0.972,2.948c-0.355,0.52-0.826,0.999-1.409,1.437l-1.772,1.389c-1.749,1.364-2.833,2.57-3.254,3.618
									c-0.421,1.047-0.681,2.944-0.777,5.688h6.603c0.016-1.299,0.12-2.257,0.315-2.875c0.309-0.975,0.931-1.827,1.871-2.559                                                 
									l1.725-1.34c1.749-1.363,2.933-2.484,3.548-3.361c1.053-1.445,1.578-3.225,1.578-5.335c0-3.443-1.214-6.041-3.642-7.796  
									c-2.428-1.754-5.477-2.631-9.146-2.631c-2.794,0-5.148,0.617-7.064,1.852C-126.301-66.96-127.917-63.679-128.111-59.051                                                 
									L-128.111-59.051z" />
		</g> </g> <g id="<%=targets[1] %>"> <rect x="0" y="200" width="200"
			height="100" stroke="black" /> <text x="25" y="250" font-size="20px"
			fill="black">MSSQL <tspan x="25" dy="30">(<%=targets[1] %>)</tspan></text>
		<use x="200" y="230" xlink:href="#arrow" /> <g
			transform="matrix(1 0 0 1 450 380) scale(2.5)" visibility="hidden">
		<path fill="#C8D0DB"
			d="M-119.221-34.471h7.284v-7.04h-7.284V-34.471z M-128.111-59.051h7.04c0-1.348,0.393-2.647,1.178-3.898 
									c0.786-1.25,2.119-1.876,3.998-1.876c1.912,0,3.228,0.509,3.949,1.523c0.721,1.015,1.081,2.14,1.081,3.374  
									c0,1.072-0.324,2.055-0.972,2.948c-0.355,0.52-0.826,0.999-1.409,1.437l-1.772,1.389c-1.749,1.364-2.833,2.57-3.254,3.618
									c-0.421,1.047-0.681,2.944-0.777,5.688h6.603c0.016-1.299,0.12-2.257,0.315-2.875c0.309-0.975,0.931-1.827,1.871-2.559                                                 
									l1.725-1.34c1.749-1.363,2.933-2.484,3.548-3.361c1.053-1.445,1.578-3.225,1.578-5.335c0-3.443-1.214-6.041-3.642-7.796  
									c-2.428-1.754-5.477-2.631-9.146-2.631c-2.794,0-5.148,0.617-7.064,1.852C-126.301-66.96-127.917-63.679-128.111-59.051                                                 
									L-128.111-59.051z" />
		</g> </g> <g id="<%=targets[2] %>"> <rect x="0" y="400" width="200"
			height="100" stroke="black" /> <text x="25" y="450" font-size="20px"
			fill="black">MSSQL <tspan x="25" dy="30">(<%=targets[2] %>)</tspan></text>
		<use x="200" y="430" xlink:href="#arrow"
			transform="rotate(-50,200,430)" /> <g
			transform="matrix(1 0 0 1 450 580) scale(2.5)" visibility="hidden">
		<path fill="#C8D0DB"
			d="M-119.221-34.471h7.284v-7.04h-7.284V-34.471z M-128.111-59.051h7.04c0-1.348,0.393-2.647,1.178-3.898 
									c0.786-1.25,2.119-1.876,3.998-1.876c1.912,0,3.228,0.509,3.949,1.523c0.721,1.015,1.081,2.14,1.081,3.374  
									c0,1.072-0.324,2.055-0.972,2.948c-0.355,0.52-0.826,0.999-1.409,1.437l-1.772,1.389c-1.749,1.364-2.833,2.57-3.254,3.618
									c-0.421,1.047-0.681,2.944-0.777,5.688h6.603c0.016-1.299,0.12-2.257,0.315-2.875c0.309-0.975,0.931-1.827,1.871-2.559                                                 
									l1.725-1.34c1.749-1.363,2.933-2.484,3.548-3.361c1.053-1.445,1.578-3.225,1.578-5.335c0-3.443-1.214-6.041-3.642-7.796  
									c-2.428-1.754-5.477-2.631-9.146-2.631c-2.794,0-5.148,0.617-7.064,1.852C-126.301-66.96-127.917-63.679-128.111-59.051                                                 
									L-128.111-59.051z" />
		</g> </g> <% } %> </symbol> <symbol id="proxyserver" width="400" height="100"
			viewBox="-1 -1 400 100" preserveAspectRatio="xMinYMin meet">
		<rect x="0" y="0" width="200" height="100" stroke="black" /> <text
			x="45" y="55" font-size="20px" fill="black">Proxyserver</text> <use
			x="200" y="30" xlink:href="#arrow" /> <g
			transform="matrix(1 0 0 1 450 180) scale(2.5)" visibility="hidden">
		<path fill="#C8D0DB"
			d="M-119.221-34.471h7.284v-7.04h-7.284V-34.471z M-128.111-59.051h7.04c0-1.348,0.393-2.647,1.178-3.898 
									c0.786-1.25,2.119-1.876,3.998-1.876c1.912,0,3.228,0.509,3.949,1.523c0.721,1.015,1.081,2.14,1.081,3.374  
									c0,1.072-0.324,2.055-0.972,2.948c-0.355,0.52-0.826,0.999-1.409,1.437l-1.772,1.389c-1.749,1.364-2.833,2.57-3.254,3.618
									c-0.421,1.047-0.681,2.944-0.777,5.688h6.603c0.016-1.299,0.12-2.257,0.315-2.875c0.309-0.975,0.931-1.827,1.871-2.559                                                 
									l1.725-1.34c1.749-1.363,2.933-2.484,3.548-3.361c1.053-1.445,1.578-3.225,1.578-5.335c0-3.443-1.214-6.041-3.642-7.796  
									c-2.428-1.754-5.477-2.631-9.146-2.631c-2.794,0-5.148,0.617-7.064,1.852C-126.301-66.96-127.917-63.679-128.111-59.051                                                 
									L-128.111-59.051z" />
		</g> </symbol> <symbol id="datapower" width="400" height="100"
			viewBox="-1 -1 400 100" preserveAspectRatio="xMinYMin meet">
		<rect x="0" y="0" width="200" height="100" stroke="black" /> <text
			x="50" y="55" font-size="20px" fill="black">Datapower</text> <use
			x="200" y="30" xlink:href="#arrow" /> <g
			transform="matrix(1 0 0 1 450 180) scale(2.5)" visibility="hidden">
		<path fill="#C8D0DB"
			d="M-119.221-34.471h7.284v-7.04h-7.284V-34.471z M-128.111-59.051h7.04c0-1.348,0.393-2.647,1.178-3.898 
									c0.786-1.25,2.119-1.876,3.998-1.876c1.912,0,3.228,0.509,3.949,1.523c0.721,1.015,1.081,2.14,1.081,3.374  
									c0,1.072-0.324,2.055-0.972,2.948c-0.355,0.52-0.826,0.999-1.409,1.437l-1.772,1.389c-1.749,1.364-2.833,2.57-3.254,3.618
									c-0.421,1.047-0.681,2.944-0.777,5.688h6.603c0.016-1.299,0.12-2.257,0.315-2.875c0.309-0.975,0.931-1.827,1.871-2.559                                                 
									l1.725-1.34c1.749-1.363,2.933-2.484,3.548-3.361c1.053-1.445,1.578-3.225,1.578-5.335c0-3.443-1.214-6.041-3.642-7.796  
									c-2.428-1.754-5.477-2.631-9.146-2.631c-2.794,0-5.148,0.617-7.064,1.852C-126.301-66.96-127.917-63.679-128.111-59.051                                                 
									L-128.111-59.051z" />
		</g> </symbol> <symbol id="syncserver" width="400" height="100"
			viewBox="-1 -1 400 100" preserveAspectRatio="xMinYMin meet">
		<rect x="0" y="0" width="200" height="100" stroke="black" /> <text
			x="50" y="55" font-size="20px" fill="black">Syncserver</text> </symbol> <symbol
			id="ldap" width="200" height="200" viewBox="-1 -1 400 100"
			preserveAspectRatio="xMinYMin meet"> <rect x="0" y="120"
			width="140" height="60" stroke="black" /> <text x="45" y="160"
			font-size="20px" fill="black">LDAP</text> <use x="0" y="-90"
			xlink:href="#arrow" transform="rotate(90)" /> <g
			transform="matrix(1 0 0 1 300 235) scale(1.6)" visibility="visible">
		<path fill="#C8D0DB"
			d="M-119.221-34.471h7.284v-7.04h-7.284V-34.471z M-128.111-59.051h7.04c0-1.348,0.393-2.647,1.178-3.898 
									c0.786-1.25,2.119-1.876,3.998-1.876c1.912,0,3.228,0.509,3.949,1.523c0.721,1.015,1.081,2.14,1.081,3.374  
									c0,1.072-0.324,2.055-0.972,2.948c-0.355,0.52-0.826,0.999-1.409,1.437l-1.772,1.389c-1.749,1.364-2.833,2.57-3.254,3.618
									c-0.421,1.047-0.681,2.944-0.777,5.688h6.603c0.016-1.299,0.12-2.257,0.315-2.875c0.309-0.975,0.931-1.827,1.871-2.559                                                 
									l1.725-1.34c1.749-1.363,2.933-2.484,3.548-3.361c1.053-1.445,1.578-3.225,1.578-5.335c0-3.443-1.214-6.041-3.642-7.796  
									c-2.428-1.754-5.477-2.631-9.146-2.631c-2.794,0-5.148,0.617-7.064,1.852C-126.301-66.96-127.917-63.679-128.111-59.051                                                 
									L-128.111-59.051z" />
		</g> </symbol> <symbol id="pns" width="200" height="200" viewBox="-1 -1 400 100"
			preserveAspectRatio="xMinYMin meet"> <rect x="0" y="5"
			width="140" height="60" stroke="black" /> <text x="45" y="40"
			font-size="20px" fill="black">Apple</text> <use x="65" y="-90"
			xlink:href="#arrow" transform="rotate(90)" /> <g
			transform="matrix(1 0 0 1 300 120) scale(1.6)" visibility="visible">
		<path fill="#C8D0DB"
			d="M-119.221-34.471h7.284v-7.04h-7.284V-34.471z M-128.111-59.051h7.04c0-1.348,0.393-2.647,1.178-3.898 
									c0.786-1.25,2.119-1.876,3.998-1.876c1.912,0,3.228,0.509,3.949,1.523c0.721,1.015,1.081,2.14,1.081,3.374  
									c0,1.072-0.324,2.055-0.972,2.948c-0.355,0.52-0.826,0.999-1.409,1.437l-1.772,1.389c-1.749,1.364-2.833,2.57-3.254,3.618
									c-0.421,1.047-0.681,2.944-0.777,5.688h6.603c0.016-1.299,0.12-2.257,0.315-2.875c0.309-0.975,0.931-1.827,1.871-2.559                                                 
									l1.725-1.34c1.749-1.363,2.933-2.484,3.548-3.361c1.053-1.445,1.578-3.225,1.578-5.335c0-3.443-1.214-6.041-3.642-7.796  
									c-2.428-1.754-5.477-2.631-9.146-2.631c-2.794,0-5.148,0.617-7.064,1.852C-126.301-66.96-127.917-63.679-128.111-59.051                                                 
									L-128.111-59.051z" />
		</g> </symbol> <symbol id="all" width="100%" height="400" viewBox="0 0 300 400"
			preserveAspectRatio="xMidYMin meet"> <use fill="white"
			class="all syncserver datapower proxyserver"
			xlink:href="#proxyserver" x="240" y="150" /> <use fill="white"
			class="all syncserver datapower" xlink:href="#datapower" x="480"
			y="150" /> <use fill="white" class="all syncserver"
			xlink:href="#syncserver" x="720" y="150" /> <%if (ldap) {%><use
			fill="white" class="all syncserver ldap" xlink:href="#ldap" x="740"
			y="225" /> <%} %> <%if (pns) {%><use fill="white"
			class="all syncserver pns" xlink:href="#pns" x="740" y="10" /> <%} %>
		<use fill="white" class="all syncserver datapower proxyserver"
			xlink:href="#mssql" x="0" y="0" /> </symbol> <use xlink:href="#all" /> </svg>
		<br /><br />
		<table id="checkers">
		<tr align="center" height="40px"><% for (String name: names) {%>
			<td align="center" width="<%=80/names.size() %>%" id="<%=name %>"><%=name %></td>
			<%} %>
		</tr>
		</table>
		<%} else { %>
		<h1>Не найдены используемые чекеры</h1>
		<%} %>
	</div>
</body>
</html>

