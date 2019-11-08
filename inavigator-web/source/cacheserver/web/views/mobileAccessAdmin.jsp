<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List" %>

 <script src="/syncserver/js/angular.min.js"></script>
 <script src="/syncserver/js/adminMobAccess.js"></script>

<% final String PAGE_TITLE = "Администрирование мобильных приложений"; %>
<% final String SECTION_TITLE = "Администрирование MIS пользователей"; %>
<% request.setAttribute("PAGE_TYPE", "MIS_ACCESS_ADMIN"); %>

<%@ include file="top.jsp" %>

<style>
select {
	color:black;
    font-size: 16px;
    padding: 2px 2px;
    width: 378px;
    *background: #58B14C;
}

.active{
    background:silver;
    border:1px solid;
}

  .css-form input.ng-invalid.ng-touched {
    background-color: #FA787E;
  }
  
  
  .loading-image {
  position: absolute;
  top: 50%;
  left: 50%;
  z-index: 10;
 }

 .loadingDiv
 {
    display: none;
    width:200px;
    height: 200px;
    position: fixed;
    top: 50%;
    left: 50%;
    text-align:center;
    margin-left: -50px;
    margin-top: -100px;
    z-index:2;
    overflow: auto;
 }  

</style>


<div ng-app>
<div ng-controller="PhoneListCtrl">

<div class="loadingDiv"><img class="loading-image" src="/syncserver/images/preloader.gif"></img> </div>
<table width="100%" height="100%" >
	<tr valign="top">
		<td>
		<div style="width:100%;height:700px;border: 1px;border-style: solid;">
			<table style="border:1px;border-style: solid;width:100%;height:70px;">
				<tr>
					<td>БД:</td>
					<td>
						<select style="width:70px" ng-model="currentDb" ng-change="changeCurrentDb()">
							<option value="finik1-new">finik1</option>
							<option value="finik2-new">finik2</option>
						</select>
					</td>
					<td>ФИО:</td>
					<td><input type="search" ng-model="userSearch.name" size="30%"  /></td>
					<td>IP:</td>
					<td ><input type="search" ng-model="userSearch.ip" size="30%" /></td>					
					<td ><input type="button"  ng-click="searchUsers()" value="Поиск"/></</td>
				</tr>
				<tr>
					<td colspan="5">Пользователи АС "Навигатор" <input type="checkbox" ng-model="userSearch.navigatorRoles" /></td>
					<td ><input type="button"  ng-click="exportUsers()" value="Экспорт"/></</td>
				</tr>
			</table>		
			<div style="width:100%;height:630px;overflow-y:auto;">
				<table border="1" style="border:1px;width:100%;border-style: solid;">
					<tr ng-class="{active:$index==selectedRow}" ng-repeat="phone in phones" value="{{phone.userId}}" ng-click="changeCurrentUser(phone.userId,$index)" ng-model="currentUserId">
						<td width="650px">{{phone.userName}}</td>
						<td width="50px">{{phone.ip}}</td>
						<td width="50px">{{phone.terrbankShortName}}</td>
						<td width="50px">{{phone.businessUnitId}}</td>
					</tr>
				</table>
			</div>
		</div>
		</td>
		<td valign="top">
		<div style="width:100%;height:700px;border: 1px;border-style: solid;">
			<form ng-submit="saveUser()" >
				<table style="border:1px;width:100%;">
					<tr>
						<td colspan="2">
							<p style="text-decoration: underline;">Данные пользователя:</p>
						</td>
					</tr>
					<tr>	
						<td colspan="2" align="right"><a href="" ng-click="addNewUser()">Добавить нового пользователя</a></td>
					</tr>
					<tr>
						<td>ID пользователя:</td>
						<td><input readonly="readonly" ng-model="user.userId" size="60"/></td>
					</tr>
					<tr>
						<td>Имя пользователя:</td>
						<td><input ng-model="user.userName" size="60" required/>
						</td>
					</tr>
					<tr>
						<td>IP пользователя:</td>
						<td><input ng-model="user.ip" size="60" required/></td>
					</tr>

					<tr>
						<td>Terrbank:</td>
						<td>
						<select ng-model="user.terrbankId"  ng-options="item.id as item.name for item in dictTerrbanks" required ></select>
						</td>
					</tr>

					<tr>
						<td>Block:</td>
						<td><select ng-model="user.businessBlockId" ng-options="item.id as item.name for item in dictBlocks" ></select></td>
					</tr>

					<tr>
						<td>Unit:</td>
						<td><select ng-model="user.businessUnitId"  ng-options="item.id as (item.id  +' | ' + item.name) for item in dictUnits" ></select></td>
					</tr>

					<tr>
						<td>Email Sigma:</td>
						<td><input ng-model="user.email" required size="60"/></td>
					</tr>

					<tr>
						<td>Должность:</td>
						<td><input ng-model="user.position" size="60"/></td>
					</tr>

					<tr>
						<td>Комментарий:</td>
						<td><input ng-model="user.comment" size="60"/></td>
					</tr>

					<tr>
						<td>Email Alpha:</td>
						<td><input ng-model="user.emailAD" size="60" required/></td>
					</tr>

					<tr>
						<td>Права в MDM:</td>
						<td><input type="checkbox" ng-model="user.hasMdmRights" required/></td>
					</tr>
					
					<tr>
						<td colspan="2">
							<p style="text-decoration: underline;">Роли пользователя:</p>
							<select style="width:100%" ng-model="currentUserRoles" multiple="multiple"  ng-options="item.roleId as (item.roleName + ' (' + item.roleDescription + ')') for item in navRoles"  size="13"/>
						</td>
					</tr>

					<tr>
						<td align=leftt">
							<input type="button" ng-show="user.userId" value="Удалить" ng-click="deleteCurrentUser()"></input>
						</td>
						<td align="right">
							<input type="button" value="Отмена" ng-click="cancelCurrentUser()"></input>
							<input type="submit" value="Сохранить"></input>
						</td>
					</tr>
				</table>
			</form>
		</div>
		</td>
		
	</tr>
</table>


</div>
</div>

<%@ include file="../common/bottom.jsp" %>
