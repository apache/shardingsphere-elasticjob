$(function () {
    $("[data-mask]").inputmask();
    $('.toolbar input').bind('keypress',function(event){  
        if(event.keyCode == "13") {
            $('#jobExecDetailTable').bootstrapTable('refresh', {silent: true});
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
        startTime: $("#startTime").val(),
        endTime: $("#endTime").val(),
        ip: $("#ip").val(),
        isSuccess: $('input[name="isSuccess"]:checked ').val()
    };
}
    
function successFormatter(value) {
    switch(value)
    {
    case true:
      return 'Y';
    case false:
        return 'N';
    default:
      return 'N/A';
    }
}