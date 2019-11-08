$(function(){
	$('#delete').on('click', function () {
		var tables = $.fn.dataTable.tables(true);
		$( tables ).DataTable().draw();
	    var oParams = $( tables ).DataTable().ajax.params();
		oParams.password=$(this).parent().parent().parent().find('input').val();
		$.arcticmodal('close');
		$.arcticmodal({
			type: 'ajax',
		    url: 'delete.audit.gui',
		    ajax: {
		    	type: 'POST',
		        data: oParams,  
		        cache: false
		    },
	        openEffect: {type: "none"},
	        closeEffect: {type: "none"},
	        afterClose: function(data, el) {
	        	$( tables ).DataTable().draw();
	        }
		});		
	});
	$('#clearAudit').on('click', function() {
		var inputs = $('#insinp').find('input');
		$('#modWindow').find('span').each(function (index) {
			if (index==0) $(this).html($('#datetimestart').val());
			if (index==1) $(this).html($('#datetimeend').val());
			if (index>1) $(this).html($(inputs[index-1]).val());
		});		
		$('#modWindow').find('input:first').val('');
		$('#modWindow').arcticmodal({
	        openEffect: {type: "none"},
	        closeEffect: {type: "none"}
	    });
	});		
	$('#datetimestart').datetimepicker({
		format:'d.m.Y H:i',
		timepicker:true,
		step: 5
	});
	$('#datetimeend').datetimepicker({
		format:'d.m.Y H:i',
		timepicker:true,
		step: 5
	});	
});