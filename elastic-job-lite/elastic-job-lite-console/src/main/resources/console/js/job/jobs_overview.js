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
        url: "/api/jobs",
        cache: false,
        columns: 
        [{
            field: "jobName",
            title: "作业名",
            sortable: true
        }, {
            field: "cron",
            title: "cron表达式"
        }, {
            field: "description",
            title: "描述"
        }, {
            field: "status",
            title: "状态",
            sortable: true,
            formatter: "statusFormatter"
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
            url: "/api/jobs/settings/" + jobName,
            success: function(data) {
                if (null !== data) {
                    $(".box-body").remove();
                    $('#update-job-body').load('html/job/job_config.html', null, function() {
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

function bindTriggerButtons() {
    $(document).on("click", "button[operation='triggerJob'][data-toggle!='modal']", function(event) {
        var jobName = $(event.currentTarget).attr("jobName");
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

function bindPauseButtons() {
    $(document).on("click", "button[operation='pauseJob'][data-toggle!='modal']", function(event) {
        var jobName = $(event.currentTarget).attr("jobName");
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

function bindResumeButtons() {
    $(document).on("click", "button[operation='resumeJob'][data-toggle!='modal']", function(event) {
        var jobName = $(event.currentTarget).attr("jobName");
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
