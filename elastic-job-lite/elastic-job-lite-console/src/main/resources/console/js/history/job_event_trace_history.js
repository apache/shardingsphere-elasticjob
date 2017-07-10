$(function() {
    $("[data-mask]").inputmask();
    $(".toolbar input").bind("keypress", function(event) {
        if("13" == event.keyCode) {
            $("#job-exec-details-table").bootstrapTable("refresh", {silent: true});
        }
    });
    $("#job-exec-details-table").on("all.bs.table", function() {
        doLocale();
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
        return "<span class='label label-success' data-lang='execute-result-success'></span>";
      case false:
          return "<span class='label label-danger' data-lang='execute-result-failure'></span>";
      default:
        return "<span class='label label-danger' data-lang='execute-result-null'></span>";
    }
}

function splitFormatter(value) {
    var maxLength = 50;
    var replacement = "...";
    if(null != value && value.length > maxLength) {
        var vauleDetail = value.substring(0 , maxLength - replacement.length) + replacement;
        value = value.replace(/\r\n/g,"<br/>").replace(/\n/g,"<br/>").replace(/\'/g, "\\'");
        return '<a href="javascript: void(0);" style="color:#FF0000;" onClick="showHistoryMessage(\'' + value + '\')">' + vauleDetail + '</a>';
    }
    return value;
}
