$(function() {
    $("[data-toggle='tooltip']").tooltip();
});

function showSuccessDialog() {
    $("#success-dialog").modal("show");
    setTimeout('$("#success-dialog").modal("hide")', 2000);
}

function showFailureDialog(info) {
    $("#failure-dialog-info").text(info);
    $("#failure-dialog").modal("show");
    setTimeout("$('#failure-dialog').modal('hide')", 4000);
}

function authorityControl() {
    $.ajax({
        type: "HEAD",
        url : "/",
        complete: function(xhr, data) {
            if ("guest" === xhr.getResponseHeader("identify")) {
                $("table").on("all.bs.table", function() {
                    $(".index-content .btn-xs").attr("disabled", true);
                    $(".btn-info").attr("disabled", false);
                });
            }
            $("#authority").text(xhr.getResponseHeader("identify"));
        }
    });
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

function showTestConnectionSuccessDialog(info) {
    $("#success-dialog-info").text(info);
    showSuccessDialog();
}
