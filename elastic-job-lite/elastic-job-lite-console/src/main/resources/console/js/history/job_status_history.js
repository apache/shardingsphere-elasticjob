$(function() {
    $(".toolbar input").bind("keypress", function(event) {
        if("13" === event.keyCode) {
            $("#job-exec-status-table").bootstrapTable("refresh", {silent: true});
        }
    });
});

function queryParams(params) {
    var sortName = "success" === params.sortName ? "isSuccess" : params.sortName;
    return {
        per_page: params.pageSize, 
        page: params.pageNumber,
        q: params.searchText,
        sort: sortName,
        order: params.sortOrder,
        jobName: $("#job-name").val(),
        state: $("#state").val(),
        startTime: $("#start-time").val(),
        endTime: $("#end-time").val(),
    };
}
