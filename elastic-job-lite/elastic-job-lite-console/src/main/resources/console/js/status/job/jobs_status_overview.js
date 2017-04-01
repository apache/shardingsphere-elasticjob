$(function() {
    renderJobsOverview();
    bindButtons();
});

function renderJobsOverview() {
    $("#jobs-status-overview-tbl").bootstrapTable({
        url: "/api/jobs",
        cache: false,
        columns: 
        [{
            field: "jobName",
            title: "作业名",
            sortable: "true"
        }, {
            field: "shardingItems",
            title: "分片项",
            sortable: "true"
        }, {
            field: "cron",
            title: "cron表达式",
            sortable: "true"
        }, {
            field: "description",
            title: "描述",
            sortable: "true"
        }, {
            field: "status",
            title: "运行状态",
            formatter: "statusFormatter",
            sortable: "true"
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
            return "<span class='label label-success'>正常</span>";
            break;
        case "DISABLED":
            return "<span class='label label-warning'>被禁用</span>";
            break;
        case "PARTIAL_ALIVE":
            return "<span class='label label-warning'>部分可用</span>";
            break;
        case "CRASHED":
            return "<span class='label'>下线</span>";
            break;
    }
}

function generateOperationButtons(val, row) {
    var detailButton = "<button operation='job-detail' class='btn-xs btn-info' job-name='" + row.jobName + "'>分片状态</button>";
    var triggerButton = "<button operation='trigger-job' class='btn-xs btn-success' job-name='" + row.jobName + "'>触发</button>";
    var disableButton = "<button operation='disable-job' class='btn-xs btn-warning' job-name='" + row.jobName + "'>禁用</button>";
    var enableButton = "<button operation='enable-job' class='btn-xs btn-primary' job-name='" + row.jobName + "'>启用</button>";
    var shutdownButton = "<button operation='shutdown-job' class='btn-xs btn-danger' job-name='" + row.jobName + "'>终止</button>";
    var operationTd = detailButton  + "&nbsp;";
    if ("OK" === row.status) {
        operationTd = operationTd + triggerButton + "&nbsp;" + disableButton + "&nbsp;" + shutdownButton;
    }
    if ("DISABLED" === row.status) {
        operationTd = operationTd + enableButton + "&nbsp;" + shutdownButton;
    }
    if ("CRASHED" === row.status) {
        operationTd = "";
    }
    return operationTd;
}

function bindButtons() {
    bindDetailButton();
    bindTriggerButton();
    bindShutdownButton();
    bindDisableButton();
    bindEnableButton();
}

function bindDetailButton() {
    $(document).on("click", "button[operation='job-detail'][data-toggle!='modal']", function(event) {
        var jobName = $(event.currentTarget).attr("job-name");
        $("#index-job-name").text(jobName);
        $("#content").load("html/status/job/job_status_detail.html");
    });
}

function bindTriggerButton() {
    $(document).on("click", "button[operation='trigger-job'][data-toggle!='modal']", function(event) {
        var jobName = $(event.currentTarget).attr("job-name");
        $.ajax({
            url: "/api/jobs/" + jobName + "/trigger",
            type: "POST",
            success: function() {
                showSuccessDialog();
                $("#jobs-status-overview-tbl").bootstrapTable("refresh");
            }
        });
    });
}

function bindDisableButton() {
    $(document).on("click", "button[operation='disable-job']", function(event) {
        var jobName = $(event.currentTarget).attr("job-name");
        $.ajax({
            url: "/api/jobs/" + jobName + "/disable",
            type: "POST",
            success: function() {
                showSuccessDialog();
                $("#jobs-status-overview-tbl").bootstrapTable("refresh");
            }
        });
    });
}

function bindEnableButton() {
    $(document).on("click", "button[operation='enable-job']", function(event) {
        var jobName = $(event.currentTarget).attr("job-name");
        $.ajax({
            url: "/api/jobs/" + jobName + "/disable",
            type: "DELETE",
            success: function() {
                showSuccessDialog();
                $("#jobs-status-overview-tbl").bootstrapTable("refresh");
            }
        });
    });
}

function bindShutdownButton() {
    $(document).on("click", "button[operation='shutdown-job'][data-toggle!='modal']", function(event) {
        var jobName = $(event.currentTarget).attr("job-name");
        $.ajax({
            url: "/api/jobs/" + jobName + "/shutdown",
            type: "POST",
            success: function() {
                showSuccessDialog();
                $("#jobs-status-overview-tbl").bootstrapTable("refresh");
            }
        });
    });
}
