$(function() {
    $("[data-toggle='tooltip']").tooltip();
    $("table").on("all.bs.table", function() {
        authorityControl();
    });
});

function showSuccessDialog() {
    $("#success-dialog").modal("show");
    setTimeout('$("#success-dialog").modal("hide")', 2000);
}

function showFailureDialog(id, info) {
    $("#failure-dialog-info").text(info);
    $("#" + id).modal("show");
    setTimeout("$('#" + id + "').modal('hide')", 4000);
}

function authorityControl() {
    if (-1 !== document.cookie.indexOf("guest")) {
        $(".index-content .btn-xs").attr("disabled", true);
        $(".btn-info").attr("disabled", false);
    }
}

function showDeleteConfirmModal() {
    $("#confirm-info").text("确认要删除吗？");
}

function showShutdownConfirmModal() {
    $("#confirm-info").text("确认要关闭吗？");
}

function showUpdateConfirmModal() {
    $("#confirm-info").text("请慎重更新！");
}
