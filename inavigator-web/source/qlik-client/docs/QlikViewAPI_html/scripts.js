function methodsList() {
    var text = "";
    var headers = document.getElementsByTagName("h3");
    for (var i = 0; i < headers.length; i++) {
        var el = headers[i];
        var label = el.innerHTML;
        el.innerHTML = label + "<a id='" + label + "'/>";
        if (i > 0) {
            text += ', ';
        }
        text += "<a href='#" + label + "'>" + label + "</a>";
    }

    document.getElementById("navigation").innerHTML = text;
}