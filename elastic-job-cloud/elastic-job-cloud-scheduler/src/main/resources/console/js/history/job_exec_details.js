$(function() {
    $("[data-mask]").inputmask();
    $(".toolbar input").bind("keypress", function(event) {
        if("13" === event.keyCode) {
            $("#job-exec-details-table").bootstrapTable("refresh", {silent: true});
        }
    });
});

function queryParams(params) {
    return {
        per_page: params.pageSize, 
        page: params.pageNumber,
        q: params.searchText,
        sort: params.sortName,
        order: params.sortOrder,
        jobName: $("#job-name").val(),
        taskId: $("#task-id").val(),
        startTime: $("#start-time").val(),
        endTime: $("#end-time").val(),
        ip: $("#ip").val(),
        isSuccess: $('input[name = "isSuccess"]:checked ').val()
    };
}

function successFormatter(value) {
    switch(value)
    {
    case true:
      return "Y";
    case false:
        return "N";
    default:
      return "N/A";
    }
}
