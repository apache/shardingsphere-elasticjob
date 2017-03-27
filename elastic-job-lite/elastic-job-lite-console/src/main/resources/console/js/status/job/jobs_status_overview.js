$(function() {
    renderJobsOverview();
    bindButtons();
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
            title: "描述",
            sortable: true
        }, {
            field: "operation",
            title: "操作",
            formatter: "generateOperationButtons"
        }]
    });
}

function statusFormatter(value, row) {
    switch(value) {
        case "OK":
            return "<span class='label label-success'>全部可用</span>";
            break;
        case "DISABLED":
            return "<span class='label label-info'>被禁用</span>";
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
    var detailButton = "<button operation='job-status-detail' class='btn-xs btn-info' job-name='" + row.jobName + "'>详情</button>";
    var removeButton = "<button operation='remove-job' class='btn-xs btn-danger' job-name='" + row.jobName + "'>删除</button>";
    var pauseButton = "<button operation='pause-job' class='btn-xs btn-warning' job-name='" + row.jobName + "'>暂停</button>";
    var resumeButton = "<button operation='resume-job' class='btn-xs btn-info' job-name='" + row.jobName + "'>恢复</button>";
    var triggerButton = "<button operation='trigger-job' class='btn-xs btn-success' job-name='" + row.jobName + "'>触发</button>";
    var operationTd = detailButton  + "&nbsp;" + removeButton + "&nbsp;" + triggerButton + "&nbsp;";
    if ("PAUSED" === row.status) {
        operationTd = operationTd + resumeButton + "&nbsp;";
    } else if ("DISABLED" !== row.status && "CRASHED" !== row.status && "SHUTDOWN" !== row.status) {
        operationTd = operationTd + pauseButton + "&nbsp;";
    }
    if ("SHUTDOWN" === row.status || "CRASHED" === row.status) {
        operationTd = removeButton + "&nbsp;";
    }
    return operationTd;
}

function bindButtons() {
    bindStatusDetailButton();
    bindTriggerButton();
    bindPauseButton();
    bindResumeButton();
    bindRemoveButton();
}

function bindStatusDetailButton() {
    $(document).on("click", "button[operation='job-status-detail'][data-toggle!='modal']", function(event) {
        var jobName = $(event.currentTarget).attr("job-name");
        window.location = "index.html?job-name=" + jobName + "&status-page=show";
    });
}

function bindRemoveButton() {
    $(document).on("click", "button[operation='remove-job'][data-toggle!='modal']", function(event) {
        var jobName = $(event.currentTarget).attr("job-name");
        $.ajax({
            url: "/api/jobs/remove",
            type: "POST",
            data: JSON.stringify({jobName : jobName}),
            contentType: "application/json",
            dataType: "json",
            success: function(data) {
                if (data.length > 0) {
                    showFailureDialog("remove-job-failure-dialog");
                } else {
                    showSuccessDialog();
                }
                $("#jobs-overview-tbl").bootstrapTable("refresh");
                getJobNavTag();
            }
        });
    });
}

function bindTriggerButton() {
    $(document).on("click", "button[operation='trigger-job'][data-toggle!='modal']", function(event) {
        var jobName = $(event.currentTarget).attr("job-name");
        $.ajax({
            url: "/api/jobs/triggerAll/name",
            type: "POST",
            data: JSON.stringify({jobName : jobName}),
            contentType: "application/json",
            dataType: "json",
            success: function() {
                $("#jobs-overview-tbl").bootstrapTable("refresh");
                showSuccessDialog();
            }
        });
    });
}

function bindPauseButton() {
    $(document).on("click", "button[operation='pause-job'][data-toggle!='modal']", function(event) {
        var jobName = $(event.currentTarget).attr("job-name");
        $.ajax({
            url: "/api/jobs/pauseAll/name",
            type: "POST",
            data: JSON.stringify({jobName : jobName}),
            contentType: "application/json",
            dataType: "json",
            success: function() {
                $("#jobs-overview-tbl").bootstrapTable("refresh");
                showSuccessDialog();
            }
        });
    });
}

function bindResumeButton() {
    $(document).on("click", "button[operation='resume-job'][data-toggle!='modal']", function(event) {
        var jobName = $(event.currentTarget).attr("job-name");
        $.ajax({
            url: "/api/jobs/resumeAll/name",
            type: "POST",
            data: JSON.stringify({jobName : jobName}),
            contentType: "application/json",
            dataType: "json",
            success: function() {
                $("#jobs-overview-tbl").bootstrapTable("refresh");
                showSuccessDialog();
            }
        });
    });
}

