<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>

<%@ page import="java.util.List" %>
<%@ page import="ru.sberbank.syncserver2.gui.util.ButtonHelper" %>
<%@ page import="ru.sberbank.syncserver2.gui.util.JSPHelper" %>
<%@ page import="ru.sberbank.syncserver2.gui.data.MobileLogMode" %>

<% final String PAGE_TITLE = "Сбор логов"; %>
<% final String SECTION_TITLE = "Сбор логов"; %>
<% request.setAttribute("PAGE_TYPE", "MOBILE_LOGS"); %>
<%@ include file="top.jsp" %>
<script type="text/javascript">
$(function(){
   $(".send").on('keyup change',function(){	
      var form = $(this).closest('form').find('input');
      var userEmail = form.val();
      var device = form.next().val();      
      var mode=form.next().next().prop("checked")==true?1:0;      
      var statusMsg;
      $.ajax({
         type: 'POST',
         url: "mode.mobile.gui",
         data: {"userEmail": userEmail, "device": device, "mode": mode, "json":true},
         cache: false,
         success: function(response){
             statusMsg=response;
         },
         error: function(xhr, str, e){
        	 statusMsg="Ошибка при отправке данных";
         },
         complete: function(xhr, str){
             form.parent().find('span').text(statusMsg).show().delay(800).fadeOut(400);
         },
         dataType: "json"
      });
      return true;
                                                               
    });
});
</script>

<table border=0 cellpadding=5 cellspacing=10 width="100%">
<tr class="listHeader">
  <th align="left">Email пользователя</th>
  <th align="left" width="10%">Сбор логов</th>
  <th align="left" width="10%"></th>
</tr>
<tr>
    <td colspan="4"><hr/></td>
    <td align="left"><%=ButtonHelper.showButton("Добавить", "add.mobile.gui", "plus-normal.png", "plus-mouseon.png")%></td>
</tr>
<%
  List<MobileLogMode> modes = (List) pageContext.findAttribute("modes");
  for(int i=0; i<modes.size(); i++){
	MobileLogMode mode = (MobileLogMode) modes.get(i);

%>
<tr class="listData">
  <td><a href='logs.mobile.gui?email=<%=mode.getUserEmail() %>'><%=mode.getUserEmail() %></a></td>
  <td>
      <form action="mode.mobile.gui" method="post">
      	<input type="hidden" name="userEmail" value="<%=mode.getUserEmail()%>">
      	<input type="hidden" name="device" value="<%=mode.getDevice()%>">
      	<input type="checkbox" class="send" name="mode" <%=mode.getMode()==1?"checked":"" %>>
      	<span></span>              
      </form>
  </td>
  <td align="left">
          <%=ButtonHelper.showButton("Удалить","delete.mobile.gui?userEmail="+mode.getUserEmail()+"&device="+mode.getDevice(), "minus-normal.png","minus-mouseon.png")%>
  </td>
</tr>
<%
  }
%>
</table>
<%=JSPHelper.composeActionButton("Назад", "location.href = 'welcome.public.gui'", "goup")%>
<%@ include file="../common/bottom.jsp" %>
