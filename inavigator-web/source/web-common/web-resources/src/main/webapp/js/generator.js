/*
 * Функция обновления данных из логов
 *  
 * @param lastLogId - идентификатор последнего загруженного на клиент лога 
 * @param dataFileName - имя файла с БД
 * @param debug - true/false. полный или сокращенный режим
 */
function callRequestTaskLogs(lastLogId,dataFileName,debug,seconds) {
	$.ajax({ 
		url: "/generator/gui/logs.generator.gui?dataFileName=" + dataFileName + "&debug=" + debug +"&json=true&lastLogId="+lastLogId
		,success: function(data){
			// добавить новые логи в таблицу при необходимости
			addLogsToTable(data);
			// планируем новый ВЫЗОВ
			setTimeout(function() {callRequestTaskLogs(data.lastLogId,dataFileName,debug,seconds)}, seconds * 1000);
		}
		,error: function(a,b,c) {
			// даже если произошла ошибка, все равно планируем очередной запрос логов
			setTimeout(function() {callRequestTaskLogs(data.lastLogId,dataFileName,debug,seconds)}, seconds * 1000);
		}
		// указываем что с сервера придут json данные
		,dataType: "json"
	});
}
    
/*
 * Добавить разделитель логов в таблицу  
 * @param table - UI таблица
 */
function addSeparator(table) {
	var row = table.insertRow(table.rows.length-1);
	var cell = row.insertCell(0);
	cell.colSpan = 2;
	cell.innerHTML = "<hr/>";
}

/*
 * Очистить все логи в таблице (удалить все строки  с логами) 
 * @param table - UI таблица
 */
function clearTable(table) {
	var countOfOldLogs = table.rows.length - 1;
	for(var y=1;y < countOfOldLogs;y++) {
		table.deleteRow(0);        		
	}
}

/*
 * Метод добавляет новые логи в таблицу с логами
 * @param data
 * @returns {String}
 */
function addLogsToTable(data) {
	// если при очередном запросе новых данных нет, то пропускаем вызов
	if (data.logs.length <= 0)
		return "";
	//  Считаем сдвиг текушщего скрола относительно нижней кромки окна
	var scrollTop = $(document).height() - $(window).height() - $(window).scrollTop();
	// ищем элемент таблицу
	var table = document.getElementById("logTable");
	//Флаг определяет началась ли новая генерация
	var isBeginNewGen = false;
	var DEFAULT_CLASS_NAME = "listData";
	var NEWMESS_CLASS_NAME = "new-log-message";
	var ERROR_CLASS_NAME = "formerror";
	// пробегаем по логам чтобы определить не началась ли новая генерация ( в случае 
	// если в логах пришло сообщение с типом GEN_DB_CONNECTION_START
	for(var i=0;i<data.logs.length;i++) {
		if (data.logs[i].eventType == "GEN_DB_CONNECTION_START")
			isBeginNewGen = true;
	}
	// если это новая генерация, то удаляем все старые логи
	if (isBeginNewGen)
		clearTable(table);
	// Сбрасываем у всех сообщений css класс, чтобы перед появлением новых сообщений сбросить отметку со старых 
	for(var y=0;y<table.rows.length;y++) {
		table.rows[y].className = table.rows[y].className.replace(NEWMESS_CLASS_NAME,"");        		
	}
	// Пробегаем по всем новым собщениям в логах и добавляем их в HTML таблицу
	for(var i=0;i<data.logs.length;i++) {
		try {
			// Создаем новую строку
			var row = table.insertRow(table.rows.length-1);
			// прописываем у строки класс - новое сообщение
			row.className=DEFAULT_CLASS_NAME + ((data.logs[i].eventType == 'ERROR')?(" " + ERROR_CLASS_NAME):"") + " " + NEWMESS_CLASS_NAME;
			// добавляем первую ячейку - со временем события
			var cell1 = row.insertCell(0);
			cell1.innerHTML = data.logs[i].eventTime;
			// добавляем вторую ячейку с информацией о событии
			var cell2 = row.insertCell(1);
			if (data.logs[i].eventType == 'ERROR')
				cell2.innerHTML = "<pre>" + data.logs[i].eventDesc + "</pre>";
			else
				cell2.innerHTML = data.logs[i].eventDesc;
			// добавляем разделитель
			addSeparator(table);
		} catch (e) {}
    }
	// ВЫравниваем скроллна позицию в которой он был до того , как HTML таблица изменилась
	$(window).scrollTop($(document).height() - $(window).height() - scrollTop);
}


function updateImageProperties(img,actionGenState) {
	if (img.src != actionGenState.colorFileName)
		img.src = actionGenState.colorFileName;
	if (img.title != actionGenState.title)
		img.title = actionGenState.title;
}

/*
 * Обновить статус всех заданий  
 * @param data
 */
function updateStatusTableToTable(data) {
	// пробегаемся по статусам всех задач гененерации
	for(var i=0;i < data.actionInfos.length;i++) {
		try {
			// текущая таска нга генерацию 
			var currentActionInfo = data.actionInfos[i];
			// Определяем текущую строку
			var row = document.getElementById(currentActionInfo.application + "_" + currentActionInfo.dataFileName);
			
			if (row != null) {
				
				// ОБновляем статус задачи
				row.cells[2].innerHTML = currentActionInfo.status;
				
				// обновляем режим генерации
				if(currentActionInfo.isGenerationModeDraft == "true") {
					$(row.cells[3]).find(".modeLink").attr("onclick","return checkShutDownDraftMode(" + currentActionInfo.canPublish + ");" );
				}
				else {
					$(row.cells[3]).find(".modeLink").attr("onclick","" );
				}
				
				row.cells[4].innerHTML = currentActionInfo.publishStatus;

				// если с сервера пришла информация о фазах и статусах, то обновляем данные по альфа-статусам
				if ((currentActionInfo.actionGenStates != null) && (currentActionInfo.actionGenStates.length > 0)) {
					updateImageProperties(row.cells[5].children[0],currentActionInfo.actionGenStates[0]);
					updateImageProperties(row.cells[6].children[0],currentActionInfo.actionGenStates[1]);
					updateImageProperties(row.cells[7].children[0],currentActionInfo.actionGenStates[2]);
				}
				
				// Если с сервера пришла информация о загрузки файлов на сигме - обновляем сигма-статусы
				if ((currentActionInfo.actionLoadStates != null) && (currentActionInfo.actionLoadStates.length > 0)) {
					var imgs = row.cells[8].getElementsByTagName("img");
					for(var i1 = 0; i1<imgs.length; i1++) {
						updateImageProperties(imgs[i1],currentActionInfo.actionLoadStates[i1]);
					}
				}
				
				// Обновляем активность кнопок
				if (row.cells[9] != null) {
					$(row.cells[9]).find(".stopButtonClass").attr("style"  , (currentActionInfo.canStop == "true")?  "":"display:none;");
					$(row.cells[9]).find(".startButtonClass").attr("style" , (currentActionInfo.canStart == "true")? "":"display:none;");
					$(row.cells[9]).find(".copyButtonClass").attr("style"  , (currentActionInfo.canCopy == "true")?  "":"display:none;");
					$(row.cells[9]).find(".publishButtonClass").attr("style"  , (currentActionInfo.canPublish == "true")?  "":"display:none;");
					$(row.cells[9]).find(".deleteDraftButtonClass").attr("style"  , (currentActionInfo.canDeleteDraft == "true")?  "":"display:none;");
					$(row.cells[9]).find(".forsePublisButtonClass").attr("style"  , (currentActionInfo.canForcePublish == "true")?  "":"display:none;");
					
				}	
			}	
		} 
		catch (e) {}
	}	
} 

/*
 * Функция обновления данных из сигналов
 *  
 * @param lastLogId - идентификатор последнего загруженного на клиент лога 
 * @param dataFileName - имя файла с БД
 * @param debug - true/false. полный или сокращенный режим
 */
function callRequestTaskInfo(seconds) {
	$.ajax({ 
		url: "/generator/gui/tasks.generator.gui?json=true"
		,success: function(data){
			// добавить новые логи в таблицу при необходимости
			updateStatusTableToTable(data);
			// планируем новый ВЫЗОВ
			setTimeout(function() {callRequestTaskInfo(seconds)}, seconds * 1000);
		}
		,error: function(a,b,c) {
			// даже если произошла ошибка, все равно планируем очередной запрос логов
			setTimeout(function() {callRequestTaskInfo(seconds)}, seconds * 1000);
		}
		// указываем что с сервера придут json данные
		,dataType: "json"
	});
}

