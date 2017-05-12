$(function() {
    $("[data-mask]").inputmask();
    $(".toolbar input").bind("keypress", function(event) {
        if("13" == event.keyCode) {
            $("#job-exec-details-table").bootstrapTable("refresh", {silent: true});
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
        return "<span class='label label-success'>成功</span>";
      case false:
          return "<span class='label label-danger'>失败</span>";
      default:
        return "空";
    }
}

function splitFormatter(value) {
    var maxLength = 50;
    var replacement = "...";
    if(null != value && value.length > maxLength) {
        var vauleDetail = value.substring(0 , maxLength - replacement.length) + replacement;
        return '<a href="javascript: void(0);" style="color:#FF0000;" onClick="showHistoryMessage(\'' + value.replace(/\n/g,"<br/>") + '\')">' + vauleDetail + '</a>';
    }
    return value;
}
