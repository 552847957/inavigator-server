(function($) {
    $.fn.jCombo = function(url, user_options) {
        var default_options = {
                parent:          "",
                selected_value : "0",
                parent_value :   "",
                initial_text:    "-- Выберите значение --"
        };                
        var user_options = $.extend( default_options, user_options) ;
        var obj = $(this);
        if(url=="refresh") { 
            $(this).trigger("change"); 
        }
        
        if(user_options.parent!="") {
            var $parent = $(user_options.parent);            
            $parent.removeAttr("disabled","disabled");
            $parent.bind('change', function(e) {
                obj.attr("disabled", "disabled");
                if($(this).val() != "0" && $(this).val() != "") {
                    obj.removeAttr("disabled");
                }
                __fill(obj,
                       url,
                       $(this).val(),
                       user_options.initial_text,
                       user_options.selected_value);
            });
        } 
        __fill(obj,url,user_options.parent_value,user_options.initial_text,user_options.selected_value);
        
        function __fill($obj, $url, $id, $initext, $inival) {
            $obj.html('<option value="0">Загрузка ...</option>');
            $.ajax({
                type: "GET",
                url: $url + $id,
                dataType: "json",
                context: document.body,
                async: true,
                error: function() {
                    
                },
                success: function(data){
                    var choices = '';
                    if (data.length == 0) {
                        choices += '<option value="0"></option>';
                        $obj.html(choices);
                    } else {
                        if($initext != "" && $initext != null) { 
                            choices += '<option value="0">' + $initext + '</option>';
                        }
                        $(data).each(
                            function () {
                                var optionId = $(this).attr('id');
                                choices += '<option value="' + optionId + '"' + (optionId == $inival? ' selected ' : '') + '>' + $(this).attr('caption') + '</option>';
                            }
                        );
                        $obj.html(choices);
                    }                                                   
                    $obj.trigger("change");
                }
            });                    
        }
    }
})(jQuery);