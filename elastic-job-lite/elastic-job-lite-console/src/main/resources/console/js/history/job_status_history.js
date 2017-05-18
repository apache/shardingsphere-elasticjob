$(function() {
    $(".toolbar input").bind("keypress", function(event) {
        if("13" == event.keyCode) {
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
        endTime: $("#end-time").val()
    };
}

function splitRemarkFormatter(value, row) {
    var maxLength = 50;
    var replacement = "...";
    if(null != value && value.length > maxLength) {
        var valueDetail = value.substring(0 , maxLength - replacement.length) + replacement;
        value = value.replace(/\n/g,"<br/>").replace(/\'/g, "\\'");
        var remarkHtml;
        if ("TASK_FAILED" === row.state || "TASK_ERROR" === row.state) {
            remarkHtml = '<a href="javascript: void(0);" style="color:#FF0000;" onClick="showHistoryMessage(\'' + value + '\')">' + valueDetail + '</a>';
        } else {
            remarkHtml = '<a href="javascript: void(0);" style="color:black;" onClick="showHistoryMessage(\'' + value + '\')">' + valueDetail + '</a>';
        }
        return remarkHtml;
    }
    return value;
}

function stateFormatter(value) {
    switch(value)
    {
        case "TASK_STAGING":
            return "<span class='label label-default'>等待运行</span>";
        case "TASK_FAILED":
            return "<span class='label label-danger'>运行失败</span>";
        case "TASK_FINISHED":
            return "<span class='label label-success'>已完成</span>";
        case "TASK_RUNNING":
            return "<span class='label label-primary'>运行中</span>";
        case "TASK_ERROR":
            return "<span class='label label-danger'>启动失败</span>";
        case "TASK_KILLED":
            return "<span class='label label-warning'>主动终止</span>";
        default:
            return "-";
    }
}