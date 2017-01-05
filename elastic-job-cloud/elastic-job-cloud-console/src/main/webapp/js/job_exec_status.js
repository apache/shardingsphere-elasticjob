$(function () {
    $('.toolbar input').bind('keypress',function(event){  
        if(event.keyCode == "13") {
            $('#jobExecStatusTable').bootstrapTable('refresh', {silent: true});
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
        jobName: $("#jobName").val(),
        taskId: $("#taskId").val(),
        slaveId: $("#slaveId").val(),
        source: $("#source").val(),
        executionType: $("#executionType").val(),
        state: $("#state").val(),
        startTime: $("#startTime").val(),
        endTime: $("#endTime").val(),
    };
}
