$(function() {
    tooltipLocale();
    validate();
    dataControl();
    submitConfirm("post", "/api/job/register", $("#data-add-job"));
});
