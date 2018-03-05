/*封装executeScript，可以向当前网页批量注入js脚本*/
function executeScripts(tabId, injectDetailsArray) {
    function createCallback(tabId, injectDetails, innerCallback) {
        return function () {
			/* globals chrome */
            chrome.tabs.executeScript(tabId, injectDetails, innerCallback);
        };
    }

    var callback = null;

    for (var i = injectDetailsArray.length - 1; i >= 0; --i) {
        callback = createCallback(tabId, injectDetailsArray[i], callback);
    }
    if (callback !== null) {
        callback();
    }
}