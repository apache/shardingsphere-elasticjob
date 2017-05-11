$(function() {
    $("[data-toggle='tooltip']").tooltip();
});

function showDialog(msg, style, timeout) {
    var modalHtml = "<" + style + " id='message-dialog-msg'></" + style + ">";
    $("#message-dialog .modal-body").empty();
    $("#message-dialog .modal-body").append(modalHtml);
    $("#message-dialog-msg").text(msg);
    $("#message-dialog").modal("show");
    if(null !== timeout) {
        setTimeout('$("#message-dialog").modal("hide")', timeout);
    }
}

function showInfoDialog(msg) {
    showDialog(msg, "h4", 2000);
}

function showErrorDialog(msg) {
    showErrorDialogByStyle(msg, "h4");
}

function showErrorDialogByStyle(msg, style) {
    showDialog(msg, style, null);
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
    $("#confirm-info").text("该更新会对运行中的作业造成影响，请慎重操作！");
}
