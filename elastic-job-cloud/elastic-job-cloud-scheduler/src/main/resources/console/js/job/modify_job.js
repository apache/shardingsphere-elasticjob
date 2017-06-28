$(function() {
    validate();
    dataControl();
    submitConfirm("put", "/api/job/update", $("#data-update-job"));
});
