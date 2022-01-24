var langmap = {
    zh: '中文',
    en: '英语',
    jp: '日语',
    ara: '阿拉伯语',
    est: '爱沙尼亚语',
    bul: '保加利亚语',
    pl: '波兰语',
    dan: '丹麦语',
    de: '德语',
    ru: '俄语',
    fra: '法语',
    fin: '芬兰语',
    kor: '韩语',
    nl: '荷兰语',
    cs: '捷克语',
    rom: '罗马尼亚语',
    pt: '葡萄牙语',
    swe: '瑞典语',
    slo: '斯洛文尼亚',
    th: '泰语',
    wyw: '文言文',
    spa: '西班牙语',
    el: '希腊语',
    hu: '匈牙利语',
    it: '意大利语',
    yue: '粤语',
    cht: '中文繁体'
};
var timer = null;
var auth = null;
var host = null;

$(document).ready(function () {
    chrome.tabs.query({
        active: true,
        lastFocusedWindow: true,
        currentWindow: true
    }, function (tabs) {
        chrome.storage.local.get(null, function (items) {
            //console.log('items', items);
            account_info_name = items['account_info_name'];
            account_info_password = items['account_info_password'];
            account_info_host = items['account_info_host'];

            // console.log("account_info_name", account_info_name);
            // console.log("account_info_password", account_info_password);
            // console.log("account_info_host", account_info_host);

            if (account_info_name && account_info_password) {
                auth = "Basic " + Base64.encode(account_info_name + ':' + account_info_password);
                //console.log("auth", auth);
            } else {
                $('#result').text('\u8bf7\u5148\u8bbe\u7f6e\u7528\u6237\u540d\u548c\u5bc6\u7801');
                $('#translate-text').css('visibility', 'hidden');
            }
            if (account_info_host) {
                host = account_info_host;
            } else {
                $('#result').text('\u8bf7\u5148\u8bbe\u7f6e\u670d\u52a1\u5668\u5730\u5740');
                $('#translate-text').css('visibility', 'hidden');
            }
        });
    });

    $('#translate-text').click(function () {
        var query = $.trim($('#query').val());
        // query为空提示
        if (query.length <= 0) {
            $('.translate-placeholder').hide();
            $('.translate-error').show();
            var timer = setTimeout(function () {
                $('.translate-error').hide();
                $('.translate-placeholder').show();
                clearTimeout(timer);
            }, 1000);
            return;
        }

        function fanyiTrans() {
            $.ajax({
                url: host + '/onlinetranslation/translate?toLanguage=' + $('.translate-to .selected-l-text').attr('value'),
                method: 'POST',
                contentType: 'application/json',
                dataType: 'json',
                data: $('#query').val(),
                async: true,
                beforeSend : function(req) {
                    req.setRequestHeader('Authorization', auth);
                }
            }).done(function (data) {
                console.log("Submit translation request", data);
                if (data['errorCode']) {
                    $('#result').text(data['message']);
                } else {
                    $('#result').text('翻译中......');
                    doTranslation(data['uuid']);
                }
            }).fail(function (jqXHR, textStatus, errorThrown) {
                //console.log(jqXHR);
                if (jqXHR.status == 401) {
                    $('#result').text('用户账号无效。');
                } else if (jqXHR.status == 400) {
                    $('#result').text('翻译请求参数错误。');
                } else if (jqXHR.status == 404) {
                    $('#result').text('翻译服务地址错误。');
                } else {
                    $('#result').text('翻译服务内部错误。');
                }
            }).always(function (data) {
                if (data == null) {
                    $('#result').text('翻译请求超时了，请稍后再试。');
                }
            });
        }

        fanyiTrans();
    });

    $('#translate-text-clear').click(function () {
        $('#result').html('').css('padding', 0);
        $('#query').val('');
        $('#translate-text-clear').css('visibility', 'hidden');
    });

    $('#icon_options_setting').click(function () {
        window.open('options.html');
    });

    $('#icon_options_setting').hover(function () {
        $('#icon_options_setting_img').attr('src', 'imgs/map/setuphover.png');
    }, function () {
        $('#icon_options_setting_img').attr('src', 'imgs/map/setup.png');
    });

    $('#icon_options_help').click(function () {

        window.open('mailto:song_liping@hotmail.com');
    });
    $('#icon_options_help').hover(function () {
        $('#icon_options_help_img').attr('src', 'imgs/map/help_hover.png');
    }, function () {
        $('#icon_options_help_img').attr('src', 'imgs/map/help.png');
    });

    $('.select-l').on('click', selectEvent);

    $('.select-inner span').on('click', langBtnEvent);

    function selectEvent(e) {
        var target = $(e.target).hasClass('select-l') ? $(e.target) : $(e.target).parents('.select-l');
        e.stopPropagation();
        initSelectBtn();
        if (target.attr('inlineimage-click') === 'no-click') {
            target.find('.selected-l').css('justify-content', 'flex-start').css('margin-left', '15px');
            if (target.hasClass('translate-from')) {
                target.addClass('from-click');
            } else {
                target.addClass('to-click');
            }
            target.children('.select-inner').slideDown(100);
            target.attr('inlineimage-click', 'click');
        } else {
            target.attr('inlineimage-click', 'no-click');
            target.children('.select-inner').slideUp(100);
        }
    }

    function langBtnEvent(e) {
        e.stopPropagation();

        var target = $(e.target);
        target.siblings().removeClass('span-hover');
        target.addClass('span-hover');

        target.parents('.select-l').attr('inlineimage-click', 'no-click').find('.selected-l-text').html(target.html()).attr('value', target.attr('value'));
        $('.select-inner').slideUp(100);
        initSelectBtn();
    }

    function initSelectBtn() {
        $('.select-l').removeClass('from-click').removeClass('to-click');
        $('.select-inner').slideUp(100);
        $('.selected-l').css('justify-content', 'center').css('margin-left', '0');
    }

    $(document).on('click', function (e) {
        initSelectBtn();
    });
    var flag = true;
    $('#query').hover(function () {
        if (flag) {
            $(this).parent('.row').css('border-color', '#bbb');
        }
    }, function () {
        if (flag) {
            $(this).parent('.row').css('border-color', '#dedede');
        }
    }).on('blur', function () {
        if ($(this).val() === '') {
            $('.translate-placeholder').show();
        }
        $(this).parent('.row').css('border-color', '#dedede');
        flag = true;
    }).on('focus', function () {
        if ($(this).val() === '') {
            $('.translate-placeholder').hide();
        }
        $(this).parent('.row').css('border-color', '#4395FF');
        flag = false;
    }).on('keydown keyup', function () {
        if ($('#query').val().length > 0) {
            $('.translate-placeholder').hide();
            $('#translate-text-clear').css('visibility', 'visible');
        } else {
            $('#result').css('padding', '0px');
            $('.translate-placeholder').show();
            $('#translate-text-clear').css('visibility', 'hidden');
            $('#result').html('');
        }

        if ($('.form-control').get(0).scrollHeight && $('.form-control').get(0).scrollHeight < 130) {
            $('.form-control').height(this.scrollHeight);
        } else {
            $('.form-control').css('overflow-y', 'scroll');
        }

        if ($('.form-control').scrollTop() === 0 && $('.form-control').height() > 60) {
            $('.form-control').height('60px').height(this.scrollHeight);
        }
    }).on('keyup', function () {
        if (timer) {
            clearTimeout(timer);
        }
        if ($.trim($('#query').val()).length > 0 && host && auth) {
            timer = setTimeout(function () {
                $('#translate-text').click();
                clearTimeout(timer);
            }, 1500);
        }
    });
});

function doTranslation(uuid) {
    chrome.runtime.sendMessage({
        action: 'translate',
        uuid: uuid,
        hostAddress: host,
        authorization: auth
    }, function (data) {
        console.info("server response", data);
        if (data['errorCode']) {
            $('#result').text(data.message);
        } else if (data.status === 'ERROR' || data.status === 'WARNING' || data.status === 'NOT_AUTHORIZED' || data.status === 'TIMEOUT') {
            $('#result').text(data.message);
        } else {
            if ($('.translate-from .selected-l-text').attr('value') === 'auto') {
                if (data.fromLanguage !== 'auto') {
                    $('.translate-from .select-inner span').removeClass('span-hover').each(function (index, el) {
                        if ($(el).attr('value') === data.fromLanguage) {
                            $(this).addClass('span-hover');
                        }
                    });
                    $('.translate-from .selected-l-text').html('检测到' + langmap[data.fromLanguage]);
                } else {
                    $('.translate-from .selected-l-text').html('自动检测');
                }
            }
            if (data['toLanguage'] === 'en') {
                $('.translate-to .selected-l-text').html('英文');
            }

            var dst_text = '';
            for (var i in data['translations']) {
                dst_text += (data['translations'][i]['dst'] + '<br>');
            }
            if (dst_text.length > 0) {
                $('#result').css('padding', '12px');
            } else {
                $('#result').css('padding', '0px');
            }
            dst_text += '<p style="float:right;"><a style="text-decoration:none;" href="javascript:;" id="moreMean">更多释义 \></a></p>';
            $('#result').html(dst_text);
            var TransSrc = '';
            for (var i = 0; i < data['translations'].length; i++) {
                TransSrc += data['translations'][i]['src'];
            }
            // console.log("TransSrc", TransSrc);
            $('#moreMean').click(function () {
                window.open('http://fanyi.baidu.com/#' + data['from'] + '/' + data['to'] + '/' + TransSrc);
            });
        }
    });
}