$(document).ready(function() {
    function isAppleDevice() {
        return (
            (navigator.platform.indexOf("iPhone") != -1) ||
            (navigator.platform.indexOf("iPod") != -1) ||
            (navigator.platform.indexOf("iPad") != -1)
        )
    }

    function isAndroidDevice() {
        return navigator.userAgent.indexOf("Android") != -1
    }

    $('#scanBtn').click(function() {
        var loc = window.location
        var replyUrl = loc.protocol + '//' + loc.host + '/scan/products/{CODE}'
        if (isAppleDevice() || isAndroidDevice()) {
            window.location = (isAppleDevice() ? "zxing://scan/?ret=" : "http://zxing.appspot.com/scan?ret=") + replyUrl
        } else
            alert("An Android or iP{hone|pad|pod} device needed to scan")
    })
})