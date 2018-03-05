var clickFlag = true;

// domainName+':'+port+'/'+webContext+'/'+uri == url
function parseURL(url) {
    var domain;
    if (url.indexOf('/') >= 0) {
        domain = url.substr(0, url.indexOf('/'));
    } else {
        domain = url;
    }

    var domainName;
    var port;
    var idx = domain.indexOf(':');

    if (idx > 0) {
        domainName = domain.substr(0, idx);
        port = domain.substr(idx + 1);
    } else {
        domainName = domain;
    }

    var shortName = domainName.substr(url.indexOf('.') + 1);
    if (shortName.indexOf('.') < 0) {
        shortName = domainName;
    }

    var tmp2 = url.substr(url.indexOf('/') + 1);
    var webContext = tmp2.substr(0, tmp2.indexOf('/'));

    var uri = tmp2.substr(tmp2.indexOf('/'));

    return {
        domainName: domainName,
        shortName: shortName,
        port: port,
        webContext: webContext,
        uri: uri
    };
}

var custom_switch = false;

// 只保存自动翻译网站列表
function save_options() {
    custom_sites = [];

    $('.site').each(function () {
        /* globals bridge */
        /* eslint-disable fecs-camelcase */
        if ($(this).text() !== '' && custom_sites.indexOf($(this).text()) === -1) {
            custom_sites.push($(this).text());
        }
    });

    chrome.storage.local.set({
        /* globals bridge */
        custom_sites: custom_sites
    }, function () {
        location.reload();
    });
}

/*
    ban_detect:自动检测
    huaci_switch:划词翻译
    huaci_button:是否“显示图标，点击即可弹出翻译。”
    online_api:线上OR线下
    custom_switch:自动翻译网站开关
    custom_sites:数组，每个元素为用户添加的一条网址
*/
function restore_options() {
    /* globals bridge */
    chrome.storage.local.get(null, function (items) {
        // console.log('items', items);
        account_info_name = items['account_info_name'];
        account_info_password = items['account_info_password'];

        if (account_info_name) {
            $('#account-info-name').attr("value", account_info_name);
        }

        if (account_info_password) {
            $('#account-info-password').attr("value", account_info_password);
        }
    });
}

restore_options();

$(document).ready(function () {

    $('#account-info-name').keypress(function () {
        setTimeout(function() {
            chrome.storage.local.set({
                account_info_name: $('#account-info-name').val()
            });
        }, 200);
    });

    $('#account-info-password').keypress(function () {
        setTimeout(function() {
            chrome.storage.local.set({
                account_info_password: $('#account-info-password').val()
            });
        }, 200);
    });

    $('#save').click(function () {
        save_options();
    });
});