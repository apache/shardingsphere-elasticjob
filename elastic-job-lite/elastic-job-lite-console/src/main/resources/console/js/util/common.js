$(function() {
    $("[data-toggle='tooltip']").tooltip();
});

function showDialog(msg, timeout) {
    $("#message-info").text(msg);
    $("#message-dialog").modal("show");
    if(null !== timeout) {
        setTimeout('$("#message-dialog").modal("hide")', timeout);
    }
}

function showSuccessDialog() {
    if ($("#content").hasClass("lang-en")) {
        showInfoDialog("Operation complete successfully");
    } else {
        showInfoDialog("操作已成功完成");
    }
}

function showInfoDialog(msg) {
    showDialog(msg, 2000);
}

function showFailureDialog(msg) {
    showDialog(msg, null);
}

function authorityControl() {
    $.ajax({
        type: "HEAD",
        url : "/",
        complete: function(xhr, data) {
            if ("guest" === xhr.getResponseHeader("identify")) {
                $("table").on("all.bs.table", function() {
                    $(".index-content .btn-xs").not(".btn-info").attr("disabled", true);
                    $(".index-content .btn-xs").not(".btn-info").removeClass().addClass("btn-xs");
                });
            }
            if ("" === $("#authority").text()) {
                $("#authority").text(xhr.getResponseHeader("identify"));
            }
        }
    });
}

function showDeleteConfirmModal() {
    if ($("#content").hasClass("lang-en")) {
        $("#confirm-info").text("Are you sure to delete it？");
    } else {
        $("#confirm-info").text("确认要删除吗？");
    }
    $("#confirm-dialog").modal({backdrop: 'static', keyboard: true});
}

function showShutdownConfirmModal() {
     if ($("#content").hasClass("lang-en")) {
         $("#confirm-info").text("Are you sure to close it？");
     } else {
         $("#confirm-info").text("确认要关闭吗？");
     }
    $("#confirm-dialog").modal({backdrop: 'static', keyboard: true});
}

function showUpdateConfirmModal() {
     if ($("#content").hasClass("lang-en")) {
         $("#confirm-info").text("It will affect the running job by updating these fields are Monitor Execution、Failover and Misfire，please be careful!");
     } else {
         $("#confirm-info").text("更新监控作业执行时状态、支持自动失效转移、支持misfire会对运行中的作业造成影响，请慎重操作！");
     }
    $("#confirm-dialog").modal({backdrop: 'static', keyboard: true});
}

function showDataSourceFailureDialog() {
    if ($("#content").hasClass("lang-en")) {
        showFailureDialog("The operation is unsuccessful, for the reason: the connection failed, please check the event trace data source configuration");
    } else {
        showFailureDialog("操作未成功，原因：连接失败，请检查事件追踪数据源配置");
    }
}

function showRegCenterFailureDialog() {
    if ($("#content").hasClass("lang-en")) {
        showFailureDialog("The operation is unsuccessful, for the reason: the connection failed, please check the registry center configuration");
    } else {
        showFailureDialog("操作未成功，原因：连接失败，请检查注册中心配置");
    }
}

function showDataSourceTestConnectionSuccessDialog() {
    if ($("#content").hasClass("lang-en")) {
        showInfoDialog("Event trace data source test connection success!");
    } else {
        showInfoDialog("事件追踪数据源测试连接成功!");
    }
}

function showDataSourceTestConnectionFailureDialog() {
    if ($("#content").hasClass("lang-en")) {
        showInfoDialog("Event trace data source test connection failure!");
    } else {
        showInfoDialog("事件追踪数据源测试连接失败!");
    }
}

function i18n(lang) {
    jQuery.i18n.properties({
        name : 'message',
        path : '/i18n/',
        mode : 'map',
        language : lang,
        cache: true,
        encoding: 'UTF-8',
        callback : function() {
            for (var i in $.i18n.map) {
                $('[data-lang="'+i+'"]').html($.i18n.prop(i));
            }
        }
    });
}

function doLocale() {
    if ($("#content").hasClass("lang-en")) {
        i18n("en");
        $.extend($.fn.bootstrapTable.defaults, $.fn.bootstrapTable.locales['en-US']);
    } else {
        i18n("zh");
        $.extend($.fn.bootstrapTable.defaults, $.fn.bootstrapTable.locales['zh-CN']);
    }
}

function changeLang() {
    $("#lang-zh").click(function() {
        $("#content").removeClass("lang-en").addClass("lang-zh");
        doLocale();
    });
    $("#lang-en").click(function() {
        $("#content").removeClass("lang-zh").addClass("lang-en");
        doLocale();
    });
}
