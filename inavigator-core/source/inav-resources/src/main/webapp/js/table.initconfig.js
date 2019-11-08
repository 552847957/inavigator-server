function initTable(params) {
	var butImport = {
			"sExtends": "text",
			"sButtonText": "Экспорт в Excel",
			"fnClick": function( button, config ) {
		    	var oParams = this.s.dt.oApi._fnAjaxParameters( this.s.dt );
		    	params.addParFn(oParams);
		    	oParams.getFile=true;
		    	window.location.href=params.url+"?"+$.param(oParams);
		    
		    }
		}

	var butSearch = {
			"sExtends": "text",
			"sButtonText": "Поиск",
			"fnClick": function( button, config ) {
				table.ajax.reload(function (){
					table.columns.adjust();
				}, true);
		    }
		}

	var butReset = {
			"sExtends": "text",
			"sButtonText": "Сбросить",
			"fnClick": function( button, config ) {
		    	params.clearFn();
		    	table.ajax.reload(function (){
					table.columns.adjust();
				}, true);
		    }
		}
	var buttons = [];
	if (params.butSearch!=false) {
		buttons.push(butSearch);
	}
	if (params.butReset!=false) {
		buttons.push(butReset);
	}
	if (params.butImport==true) {
		buttons.push(butImport);
	}
	var processing = params.processing!=null ? params.processing : true;
	var order = params.order!=null ? params.order : [[ 0, "desc" ]];
	var table = params.init.DataTable({        	
        "processing": processing,
        "serverSide": true,
        "bFilter": true,
        "order": order,
        "ajax": {
        	"url": params.url,
        	"type": "GET",
        	"dataType": "json",
        	"data": function ( d ) {
        		params.addParFn(d);
        		}
    	},    
        "sDom": 'Tlrtip',
        "pagingType": "full_numbers",
        "oTableTools": {
            "aButtons": buttons
        },
        "columns": params.columns,
        "language": {
            "emptyTable":     "Нет записей",
            "info":           "Показано с _START_ по _END_ из _TOTAL_ ",
            "infoEmpty":      "Показано с 0 по 0 из 0 записей",
            "infoFiltered":   "(найдено из _MAX_)",
            "infoPostFix":    "",
            "thousands":      ",",
            "lengthMenu":     "Показывать по _MENU_ записей",
            "loadingRecords": "Loading...",
            "processing":     "Processing...",
            "search":         "Search:",
            "zeroRecords":    "Не найдено",
            "paginate": {
                "first":      "Первая",
                "last":       "Последняя",
                "next":       "Следующая",
                "previous":   "Предыдущая"
            }
        },
        "createdRow": (params.rowCallback?params.rowCallback:""),
        "paging": (params.paging!=null?params.paging:true)
    });
    params.inputs.find('input').each(function(idx) {
    	$(this).on( 'keyup change', function () {
            table.column( idx ).search( this.value );
        });
    });
    return table;
}