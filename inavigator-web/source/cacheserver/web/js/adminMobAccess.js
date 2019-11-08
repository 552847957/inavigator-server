    function PhoneListCtrl($scope, $http) {
    	
    	/**
    	 * Добавить к относительному URL полный путь
    	 */
    	$scope.createURL = function(url,params) {
    		return encodeURI('/syncserver/gui/mobile-access-admin' + url + "?t=" + Math.random() + "&"  + params); 
    	}

    	/**
    	 * Запустить инициализацию формы при закгрузке всего документа 
    	 */
    	angular.element(document).ready(function () {
		  $scope.init(); 
    	});
    	
    	$scope.showPreloader = function() {
		      $('.loadingDiv').show();
    	}

    	$scope.hidePreloader = function() {
		     $('.loadingDiv').hide();
  	}
    	
    	/**
    	 * Поиск пользователя
    	 */
    	$scope.searchUsers = function() {
    		  $scope.showPreloader();
	    	  $http.get($scope.createURL('/user/list','userName=' + $scope.userSearch.name + '&ip=' + $scope.userSearch.ip + '&navRoles=' + $scope.userSearch.navigatorRoles))
	    	  .success(function(data) {
	    		    $scope.phones = data.data;
     	      		$scope.hidePreloader();
	    	  }).error(function(){
   	      		$scope.hidePreloader();
	    	  });
    	}
    	
    	/**
    	 * Экспорт пользователя 
    	 */
    	$scope.exportUsers = function() {
    		$scope.searchUsers();
    		window.location = $scope.createURL('/user/listCSV','userName=' + $scope.userSearch.name + '&ip=' + $scope.userSearch.ip + '&navRoles=' + $scope.userSearch.navigatorRoles);    		
    	}
    	  
    	  /**
    	   * Сброс формы редактирования для создания нового пользователя
    	   */
    	  $scope.resetForm = function() {
			  $scope.user = {};
			  $scope.currentUserRoles = [];    	  
    	  }

    	 /**
    	  * Метод сохранения пользователя
    	  */
    	 $scope.saveUser = function() {
    		  if (!confirm("Произойдет сохранение данных пользователя в БД. Продолжить?")) {
    			  return;
    		  }
    		 
    		  $scope.user.roles = $scope.currentUserRoles;
 	      	  
    		  $scope.showPreloader();
    		  
    		  $http.post($scope.createURL('/user/info/put',''),$scope.user,{headers: {
   				   'Content-encoding':'UTF-8'
    		  }})
    		  .success(function(data) {
     	      	  $scope.hidePreloader();

     	      	  // формируем строку с ошибками
     	      	  var errors = "";
     	      	  var onlyWarnings = true;
				  for(var i=0;i<data.errors.length;i++) {
					  errors = errors + "- " + data.errors[i].message + "\n";
					  if (data.errors[i].critical)
						  onlyWarnings = false;
				  }
     	      	  
    			  if (onlyWarnings) {
    				  alert("Пользователь успешно сохранен." + ((data.errors.length > 0)?"Внимание! \n" + errors:"") );
        			  // alert('Пользователь успешно сохранен!');
        			  $scope.resetForm();
    			  } else {
					  alert("При сохранении пользователя возникли ошибки: \n" + errors);
    			  }
    				  
    			  $scope.searchUsers();
    			  
    		  })
    		  .error(function(data) {
     	      	  $scope.hidePreloader();
    			  alert('Сохранение пользователя вызвало ошибку. Свяжитесь с администратором системы.');
    		  });
    		  
    	  };
    	  
    	  /**
    	   * Метод для выбора пользователя на форме поиска пользвоателей
    	   */
    	  $scope.changeCurrentUser = function(id,row) {
 	      	  $scope.showPreloader();
    		  $scope.selectedRow = row;
    	      
    		  $scope.currentUserId = id;
        	  $http.get($scope.createURL('/user/info','userId=' +$scope.currentUserId))
        	  .success(function(data) {
        		    $scope.user = data.data[0];
        	  });

        	  $http.get($scope.createURL('/role/listByUser','userId=' +$scope.currentUserId))
        	  .success(function(data) {
        		    var tempArr = [];
        		    for(var i=0;i<data.data.length;i++) {
        		    	tempArr.push(data.data[i].roleId);
        		    }
        		    $scope.currentUserRoles = tempArr;
       	      	    $scope.hidePreloader();
        	  })
    		  .error(function(data) {
     	      	  $scope.hidePreloader();
    			  alert('Сохранение пользователя вызвало ошибку. Свяжитесь с администратором системы.');
    		  });
        	  
    	  };
    	  
    	  
    	  $scope.changeCurrentDb = function() {
 	      	  $scope.showPreloader();
        	  $http.get($scope.createURL('/db/set','db=' +$scope.currentDb))
        	  .success(function(data) {
        		  $scope.init();
        	  })
    		  .error(function(data) {
     	      	  $scope.hidePreloader();
    			  alert('Изменение БД вызвало ошибку. Свяжитесь с администратором.');
    		  });
    	  }
    	  
    	  /**
    	   * Метод сброса выбранного пользователя
    	   */
    	  $scope.cancelCurrentUser = function() {
    		  if (confirm("Данные текущего пользователя будут потеряны. Продолжить?")) {
    			  $scope.resetForm();
    		  }
    	  }
    	  
    	  /**
    	   * Удаление пользователя
    	   */
    	  $scope.deleteCurrentUser = function() {
    		  alert('Функция удаления будет реализована позднее.');
    	  }
    	  
    	  /**
    	   * Метод переключения формы в режим добавления нового пользователя
    	   */
    	  $scope.addNewUser = function() {
    		  if (confirm("Данные текущего пользователя будут потеряны. Продолжить?")) {
    			  $scope.resetForm();
    		  }
    	  }
    	  
    	  /**
    	   * Метод инициализации контроллера 
    	   */
    	  $scope.init = function() {
    		  
    		  // Идентификатор текущего пользователя
        	  $scope.currentUserId = -1;
        	  // Список текущих ролей
        	  $scope.currentUserRoles = [];
        	  // Список полей
        	  $scope.userSearch = {name:"",ip:"",terrbankShortName:"",businessUnitId:"",navigatorRoles:false};
	    	  // далее с сервера подгружаются все справочники
	    	  
        	  // запрашиваем с сервера текущую БД
        	  $http.get($scope.createURL('/db/get',''))
	    	  .success(function(data) {
	    		    $scope.currentDb = data.currentDb;
	    	  });
        	  
        	  $http.get($scope.createURL('/dictionary/terrbanks/list',''))
	    	  .success(function(data) {
	    		    $scope.dictTerrbanks = data.data;
	    	  });
	    	  $http.get($scope.createURL('/dictionary/blocks/list',''))
	    	  .success(function(data) {
	    		    $scope.dictBlocks = data.data;
	    	  });
	    	  $http.get($scope.createURL('/dictionary/units/list',''))
	    	  .success(function(data) {
	    		    $scope.dictUnits = data.data;
	    	  });
	    	  $http.get($scope.createURL('/role/list',''))
	    	  .success(function(data) {
	    		    $scope.navRoles = data.data;
	    	  });
        	  
        	  // Очистить форму и списко ролей
			  $scope.resetForm();
			  
			  // произвести первичный поиск пользователей
	    	  $scope.searchUsers();
    	  }
    }