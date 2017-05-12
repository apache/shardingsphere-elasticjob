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
    showInfoDialog("操作已成功完成");
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
    $("#confirm-info").text("确认要删除吗？");
    $("#confirm-dialog").modal({backdrop: 'static', keyboard: true});
}

function showShutdownConfirmModal() {
    $("#confirm-info").text("确认要关闭吗？");
    $("#confirm-dialog").modal({backdrop: 'static', keyboard: true});
}

function showUpdateConfirmModal() {
    $("#confirm-info").text("更新监控作业执行时状态、支持自动失效转移、支持misfire会对运行中的作业造成影响，请慎重操作！");
    $("#confirm-dialog").modal({backdrop: 'static', keyboard: true});
}
