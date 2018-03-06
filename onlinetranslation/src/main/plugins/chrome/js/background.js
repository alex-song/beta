/* globals chrome */
// extension.onRequest已废弃，换用runtime.onMessage https://developer.chrome.com/extensions/extension#event-onRequest
var timer = null;

chrome.extension.onMessage.addListener(function (request, sender, sendResponse) {
    // console.log('request', request);
    // console.log('sender', sender);
    if (request.action === 'translate') {
        console.info("uuid", request.uuid);
        if (timer) {
            clearTimeout(timer);
        }
        doTranslation(0, request.uuid, sendResponse);
    }
    //异步执行
    return true;
});

function doTranslation(count, uuid, sendResponse) {
    if (count++ > 10) {
        //服务器没有响应，请稍后再试。
        sendResponse({
            errorCode: 'TIMEOUT',
            message: '\u670d\u52a1\u5668\u6ca1\u6709\u54cd\u5e94\uff0c\u8bf7\u7a0d\u540e\u518d\u8bd5\u3002'
        });
    } else {
        $.ajax({
            url: 'http://songlp.ddns.net:7070/onlinetranslation/translate/' + uuid,
            method: 'GET',
            async: true
        }).done(function (data) {
            //console.info("status", data.status);
            if (data.status === 'SUBMITTED' || data.status === 'PROCESSING') {
                timer = setTimeout(function () {
                    doTranslation(count, uuid, sendResponse);
                }, 1000);
            } else {
                if (timer) {
                    clearTimeout(timer);
                }
                console.info(data);
                sendResponse(data);
            }
        });
    }
};
