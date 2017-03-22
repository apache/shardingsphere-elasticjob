$(function() {
    renderJobsOverview();
    bindModifyButtons();
    bindTriggerButtons();
    bindPauseButtons();
    bindResumeButtons();
    bindRemoveButtons();
});

function renderJobsOverview() {
    $("#jobs-overview-tbl").bootstrapTable({
        url: "/api/job/jobs",
        method: "get",
        cache: false,
        rowStyle: function (row, index) {
            var strclass = "";
            if ("OK" === row.status) {
                strclass = "success";
            } else if ("MANUALLY_DISABLED" === row.status) {
                strclass = "info";
            } else if ("PARTIAL_ALIVE" === row.status) {
                strclass = "warning";
            } else if ("ALL_CRASHED" === row.status) {
                strclass = "danger";
            } else {
                return {};
            }
            return { classes: strclass }
        },
        columns: 
        [{
            field: "jobName",
            title: "作业名"
        }, {
            field: "cron",
            title: "cron表达式"
        }, {
            field: "description",
            title: "描述"
        }, {
            fidle: "operation",
            title: "操作",
            formatter: "generateOperationButtons"
        }]
    });
}

function generateOperationButtons(val, row) {
    var modifyButton = "<button operation='modifyJob' class='btn-xs btn-warning' jobName='" + row.jobName + "'>修改</button>";
    var removeButton = "<button operation='removeJob' class='btn-xs btn-danger' jobName='" + row.jobName + "'>删除</button>";
    var pauseButton = "<button operation='pauseJob' class='btn-xs btn-warning' jobName='" + row.jobName + "'>暂停</button>";
    var resumeButton = "<button operation='resumeJob' class='btn-xs btn-info' jobName='" + row.jobName + "'>恢复</button>";
    var triggerButton = "<button operation='triggerJob' class='btn-xs btn-success' jobName='" + row.jobName + "'>触发</button>";
    var operationTd = modifyButton  + "&nbsp;" + removeButton + "&nbsp;" + triggerButton + "&nbsp;";
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

function bindModifyButtons() {
    $(document).on("click", "button[operation='modifyJob'][data-toggle!='modal']", function(event) {
        var jobName = $(event.currentTarget).attr("jobName");
        $.ajax({
            url: "/api/job/settings/" + jobName,
            success: function(data) {
                if (null !== data) {
                    $(".box-body").remove();
                    $('#update-job-body').load('html/job/job_detail.html', null, function() {
                        $('#data-update-job').modal({backdrop : 'static', keyboard : true});
                        renderJob(data);
                    });
                }
            }
        });
    });
}

function bindRemoveButtons() {
    $(document).on("click", "button[operation='removeJob'][data-toggle!='modal']", function(event) {
        var jobName = $(event.currentTarget).attr("jobName");
        $.ajax({
            url: "/api/job/remove",
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
            }
        });
    });
}

function bindTriggerButtons() {
    $(document).on("click", "button[operation='triggerJob'][data-toggle!='modal']", function(event) {
        var jobName = $(event.currentTarget).attr("jobName");
        $.ajax({
            url: "/api/job/triggerAll/name",
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

function bindPauseButtons() {
    $(document).on("click", "button[operation='pauseJob'][data-toggle!='modal']", function(event) {
        var jobName = $(event.currentTarget).attr("jobName");
        $.ajax({
            url: "/api/job/pauseAll/name",
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

function bindResumeButtons() {
    $(document).on("click", "button[operation='resumeJob'][data-toggle!='modal']", function(event) {
        var jobName = $(event.currentTarget).attr("jobName");
        $.ajax({
            url: "/api/job/resumeAll/name",
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

function renderJob(data) {
    $("#job-name").attr("value", data.jobName);
    $("#job-type").attr("value", data.jobType);
    $("#job-class").attr("value", data.jobClass);
    $("#sharding-total-count").attr("value", data.shardingTotalCount);
    $("#cron").attr("value", data.cron);
    $("#sharding-item-parameters").text(data.shardingItemParameters);
    $("#job-parameter").attr("value", data.jobParameter);
    $("#monitor-execution").attr("checked", data.monitorExecution);
    $("#failover").attr("checked", data.failover);
    $("#misfire").attr("checked", data.misfire);
    $("#streaming-process").attr("checked", data.streamingProcess);
    $("#max-time-diff-seconds").attr("value", data.maxTimeDiffSeconds);
    $("#monitor-port").attr("value", data.monitorPort);
    $("#job-sharding-strategy-class").attr("value", data.jobShardingStrategyClass);
    $("#executor-service-handler").attr("value", data.jobProperties["executor_service_handler"]);
    $("#job-exception-handler").attr("value", data.jobProperties["job_exception_handler"]);
    $("#reconcile-cycle-time").attr("value", data.reconcileCycleTime);
    $("#description").text(data.description);
    if (!data.monitorExecution) {
        $("#execution-info-tab").addClass("disabled");
    }
    $("#script-command-line").attr("value", data.scriptCommandLine);
    if ("DATAFLOW" === $("#job-type").val()) {
        $("#streaming-process-group").show();
    }
    if ("SCRIPT" === $("#job-type").val()) {
        $("#script-commandLine-group").show();
    }
}
