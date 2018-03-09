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
        doTranslation(0, request.uuid, request.hostAddress, request.authorization, sendResponse);
    }
    //异步执行
    return true;
});

function doTranslation(count, uuid, hostAddress, authorization, sendResponse) {
    if (count++ > 10) {
        //服务器没有响应，请稍后再试。
        sendResponse({
            errorCode: 'TIMEOUT',
            message: '\u670d\u52a1\u5668\u6ca1\u6709\u54cd\u5e94\uff0c\u8bf7\u7a0d\u540e\u518d\u8bd5\u3002'
        });
    } else {
        $.ajax({
            url: hostAddress + '/onlinetranslation/translate/' + uuid,
            method: 'GET',
            async: true,
            beforeSend : function(req) {
                req.setRequestHeader('Authorization', authorization);
            }
        }).done(function (data) {
            //console.info("status", data.status);
            if (data.status === 'SUBMITTED' || data.status === 'PROCESSING') {
                timer = setTimeout(function () {
                    doTranslation(count, uuid, hostAddress, authorization, sendResponse);
                    clearTimeout(timer);
                }, 1000);
            } else {
                if (timer) {
                    clearTimeout(timer);
                }
                //console.info(data);
                sendResponse(data);
            }
        }).fail(function (jqXHR, textStatus, errorThrown) {
            //console.log(jqXHR);
            //用户账号无效。
            //翻译请求参数错误。
            //没有找到对应的翻译结果。
            //翻译服务内部错误。
            //翻译请求超时了，请稍后再试。
            if (jqXHR.status == 401) {
                sendResponse({
                    errorCode: 'NOT_AUTHORIZED',
                    message: '\u7528\u6237\u8d26\u53f7\u65e0\u6548\u3002'
                });
            } else if (jqXHR.status == 400) {
                sendResponse({
                    errorCode: 'ERROR',
                    message: '\u7ffb\u8bd1\u8bf7\u6c42\u53c2\u6570\u9519\u8bef\u3002'
                });
            } else if (jqXHR.status == 404) {
                sendResponse({
                    errorCode: 'ERROR',
                    message: '\u6ca1\u6709\u627e\u5230\u5bf9\u5e94\u7684\u7ffb\u8bd1\u7ed3\u679c\u3002'
                });
            } else {
                sendResponse({
                    errorCode: 'ERROR',
                    message: '\u7ffb\u8bd1\u670d\u52a1\u5185\u90e8\u9519\u8bef\u3002'
                });
            }
        }).always(function (data) {
            if (data == null) {
                sendResponse({
                    errorCode: 'TIMEOUT',
                    message: '\u7ffb\u8bd1\u8bf7\u6c42\u8d85\u65f6\u4e86\uff0c\u8bf7\u7a0d\u540e\u518d\u8bd5\u3002'
                });
            }
        });;
    }
};
