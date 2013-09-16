// See http://stackoverflow.com/questions/901115/how-can-i-get-query-string-values
function getParameterByName(name) {
    name = name.replace(/[\[]/, "\\\[").replace(/[\]]/, "\\\]");
    var regex = new RegExp("[\\?&]" + name + "=([^&#]*)"),
        results = regex.exec(location.search);
    return results == null ? "" : decodeURIComponent(results[1].replace(/\+/g, " "));
}

$(document).ready(function(){
/*    var code = getParameterByName('code');
    if (code) {
        $('#codeField').val(code);
        $('#newProductForm').removeClass('hide');
    }
    var path = document.location.href.split('?')[0];
    var scanPath = "zxing://scan/?ret=" + encodeURIComponent(path + "?code={CODE}");
    $('#scan-new-button').attr('href', scanPath);
    $('#scan-new-button-repeat').attr('href', scanPath + "&repeat");
*/
});