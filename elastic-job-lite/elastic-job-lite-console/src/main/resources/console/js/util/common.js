$(function() {
    $("[data-toggle='tooltip']").tooltip()
});

function showSuccessDialog() {
    $("#success-dialog").modal("show");
    setTimeout('$("#success-dialog").modal("hide")', 2000);
}

function showFailureDialog(id) {
    $("#" + id).modal("show");
    setTimeout("$('#" + id + "').modal('hide')", 4000);
}
