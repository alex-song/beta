function restore_options() {
    /* globals bridge */
    chrome.storage.local.get(null, function (items) {
        // console.log('items', items);
        account_info_name = items['account_info_name'];
        account_info_password = items['account_info_password'];
        account_info_host = items['account_info_host'];

        if (account_info_name) {
            $('#account-info-path').attr("value", account_info_name);
        }

        if (account_info_password) {
            $('#account-info-password').attr("value", account_info_password);
        }

        if (account_info_host) {
            $('#account-info-host').attr("value", account_info_host);
        }
    });
}

restore_options();

$(document).ready(function () {
    var timer = null;

    $('#account-info-path').keyup(function () {
        timer = setTimeout(function() {
            chrome.storage.local.set({
                account_info_name: $('#account-info-path').val()
            });
            clearTimeout(timer);
        }, 200);
    });

    $('#account-info-password').keyup(function () {
        timer = setTimeout(function() {
            chrome.storage.local.set({
                account_info_password: $('#account-info-password').val()
            });
            clearTimeout(timer);
        }, 200);
    });

    $('#account-info-host').keyup(function () {
        timer = setTimeout(function() {
            chrome.storage.local.set({
                account_info_host: $('#account-info-host').val()
            });
            clearTimeout(timer);
        }, 200);
    });

    $('#save').click(function () {
        //
    });
});