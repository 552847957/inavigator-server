$(function(){
   $(".json").click(function(){	
      var form = $(this);
      $.ajax({
         type: 'GET',
         url: form.attr('href'),
         cache: false,
         data: {json:true},
         success: function(response){
        	 form = form.parent().parent().find('span');
        	 form.html(response[1]);        	 
        	 form.last().html(response[2]);
        	 addNotice(response[0]);
         },
         error: function(xhr, str, e){
        	 addNotice("Ошибка при отправке данных");
         },
         dataType: "json"
      });
      return false;                                                               
    });
});

function addNotice(notice) {
	$('<div class="notice"></div>')
		.append('<div class="skin"></div>')
		.append($('<div class="content"></div>').html(notice))
		.appendTo('#growl')
		.show().delay(1500).fadeOut(800);
}