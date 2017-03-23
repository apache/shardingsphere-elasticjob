$(function() {
    renderJobsOverview();
    bindStatusButtons();
});

function renderJobsOverview() {
    $("#jobs-status-overview").bootstrapTable({
        url: "/api/jobs",
        cache: false,
        columns: 
        [{
            field: "jobName",
            title: "作业名",
            sortable: "true"
        }, {
            field: "status",
            title: "运行状态",
            formatter: "statusFormatter",
            sortable: "true"
        }, {
            field: "description",
            title: "描述"
        }, {
            field: "operation",
            title: "操作",
            formatter: "generateOperationButtons"
        }]
    });
}

function statusFormatter(value) {
    switch(value) {
        case "OK":
            return "<span class='label label-success'>全部可用</span>";
            break;
        case "MANUALLY_DISABLED":
            return "<span class='label label-warning'>被禁用</span>";
            break;
        case "PARTIAL_ALIVE":
            return "<span class='label label-warning'>部分可用</span>";
            break;
        case "ALL_CRASHED":
            return "<span class='label label-danger'>全部宕机</span>";
            break;
    }
}

function generateOperationButtons(val, row) {
    return "<button operation='job-status' class='btn-xs btn-info' jobName='" + row.jobName + "'>详情</button>";
}

function bindStatusButtons() {
    $(document).on("click", "button[operation='job-status'][data-toggle!='modal']", function(event) {
        var jobName = $(event.currentTarget).attr("jobName");
        window.location = "index.html?jobName=" + jobName + "&statusPage=show";
    });
}
