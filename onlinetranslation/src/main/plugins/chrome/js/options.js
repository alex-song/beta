function save_options() {
    //
}

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
        //
    });
});