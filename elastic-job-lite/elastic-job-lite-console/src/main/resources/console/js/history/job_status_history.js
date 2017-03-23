$(function() {
    $(".toolbar input").bind("keypress", function(event) {
        if("13" === event.keyCode) {
            $("#job-exec-status-table").bootstrapTable("refresh", {silent: true});
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
        slaveId: $("#slave-id").val(),
        source: $("#source").val(),
        executionType: $("#execution-type").val(),
        state: $("#state").val(),
        startTime: $("#start-time").val(),
        endTime: $("#end-time").val(),
    };
}
