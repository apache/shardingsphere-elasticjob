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

function getCurrentUrl(param) {
    var reg = new RegExp(param + "=([^&]*)");
    var result = window.location.search.substr(1).match(reg);
    return null === result ? result : result[1];
}
