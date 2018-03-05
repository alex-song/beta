/* globals chrome */
// extension.onRequest已废弃，换用runtime.onMessage https://developer.chrome.com/extensions/extension#event-onRequest
chrome.extension.onMessage.addListener(function (request, sender, sendResponse) {
    // console.log('request', request);
    // console.log('sender', sender);
    if (request.action === 'translate') {
        console.info("uuid", request.uuid);
        $.ajax({
            url: 'http://songlp.ddns.net:7070/onlinetranslation/translate/' + request.uuid,
            method: 'GET',
            async: false
        }).done(function (data) {
            // console.log('data', data);
            sendResponse(data);
        });
    }
});
