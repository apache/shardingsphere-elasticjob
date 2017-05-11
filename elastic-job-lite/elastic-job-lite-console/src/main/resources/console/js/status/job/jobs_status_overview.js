$(function() {
    authorityControl();
    renderJobsOverview();
    bindButtons();
});

function renderJobsOverview() {
    var jsonData = {
        cache: false,
        columns:
            [{
                field: "jobName",
                title: "作业名",
                sortable: "true"
            }, {
                field: "shardingTotalCount",
                title: "分片总数",
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
    };
    var activated = false;
    $.ajax({
        url: "/api/registry-center/activated",
        async: false,
        success: function(data) {
            activated = data;
        }
    });
    if (activated) {
        jsonData.url = "/api/jobs";
    }
    $("#jobs-status-overview-tbl").bootstrapTable({
        columns: jsonData.columns,
        url: jsonData.url,
        cache: jsonData.cache
    });
}

function statusFormatter(value, row) {
    switch(value) {
        case "OK":
            return "<span class='label label-success'>正常</span>";
            break;
        case "DISABLED":
            return "<span class='label label-warning'>已禁用</span>";
            break;
        case "SHARDING_ERROR":
            return "<span class='label label-info'>分片调整中</span>";
            break;
        case "CRASHED":
            return "<span class='label label-default'>已下线</span>";
            break;
    }
}

function generateOperationButtons(val, row) {
    var modifyButton = "<button operation='modify-job' class='btn-xs btn-primary' job-name='" + row.jobName + "'>修改</button>";
    var shardingStatusButton = "<button operation='job-detail' class='btn-xs btn-info' job-name='" + row.jobName + "'>分片状态</button>";
    var triggerButton = "<button operation='trigger-job' class='btn-xs btn-success' job-name='" + row.jobName + "'>触发</button>";
    var disableButton = "<button operation='disable-job' class='btn-xs btn-warning' job-name='" + row.jobName + "'>禁用</button>";
    var enableButton = "<button operation='enable-job' class='btn-xs btn-success' job-name='" + row.jobName + "'>启用</button>";
    var shutdownButton = "<button operation='shutdown-job' class='btn-xs btn-danger' job-name='" + row.jobName + "'>终止</button>";
    var removeButton = "<button operation='remove-job' class='btn-xs btn-danger' job-name='" + row.jobName + "'>删除</button>";
    var operationTd = modifyButton + "&nbsp;" + shardingStatusButton  + "&nbsp;";
    if ("OK" === row.status) {
        operationTd = operationTd + triggerButton + "&nbsp;" + disableButton + "&nbsp;" + shutdownButton;
    }
    if ("DISABLED" === row.status) {
        operationTd = operationTd + enableButton + "&nbsp;" + shutdownButton;
    }
    if ("SHARDING_ERROR" === row.status) {
        operationTd = operationTd + "&nbsp;" + shutdownButton;
    }
    if ("CRASHED" === row.status) {
        operationTd = modifyButton + "&nbsp;" + removeButton;
    }
    return operationTd;
}

function bindButtons() {
    bindModifyButton();
    bindShardingStatusButton();
    bindTriggerButton();
    bindShutdownButton();
    bindDisableButton();
    bindEnableButton();
    bindRemoveButton();
}

function bindModifyButton() {
    $(document).off("click", "button[operation='modify-job'][data-toggle!='modal']");
    $(document).on("click", "button[operation='modify-job'][data-toggle!='modal']", function(event) {
        var jobName = $(event.currentTarget).attr("job-name");
        $.ajax({
            url: "/api/jobs/config/" + jobName,
            success: function(data) {
                if (null !== data) {
                    $(".box-body").remove();
                    $('#update-job-body').load('html/status/job/job_config.html', null, function() {
                        $('#data-update-job').modal({backdrop : 'static', keyboard : true});
                        renderJob(data);
                        $("#job-overviews-name").text(jobName);
                    });
                }
            }
        });
    });
}

function bindShardingStatusButton() {
    $(document).off("click", "button[operation='job-detail'][data-toggle!='modal']");
    $(document).on("click", "button[operation='job-detail'][data-toggle!='modal']", function(event) {
        var jobName = $(event.currentTarget).attr("job-name");
        $("#index-job-name").text(jobName);
        $("#content").load("html/status/job/job_status_detail.html");
    });
}

function bindTriggerButton() {
    $(document).off("click", "button[operation='trigger-job'][data-toggle!='modal']");
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
    $(document).off("click", "button[operation='disable-job'][data-toggle!='modal']");
    $(document).on("click", "button[operation='disable-job'][data-toggle!='modal']", function(event) {
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
    $(document).off("click", "button[operation='enable-job'][data-toggle!='modal']");
    $(document).on("click", "button[operation='enable-job'][data-toggle!='modal']", function(event) {
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
    $(document).off("click", "button[operation='shutdown-job'][data-toggle!='modal']");
    $(document).on("click", "button[operation='shutdown-job'][data-toggle!='modal']", function(event) {
        showShutdownConfirmModal();
        var jobName = $(event.currentTarget).attr("job-name");
        $(document).off("click", "#confirm-btn");
        $(document).on("click", "#confirm-btn", function() {
            $.ajax({
                url: "/api/jobs/" + jobName + "/shutdown",
                type: "POST",
                success: function () {
                    $("#confirm-dialog").modal("hide");
                    $(".modal-backdrop").remove();
                    $("body").removeClass("modal-open");
                    $("#jobs-status-overview-tbl").bootstrapTable("refresh");
                }
            });
        });
    });
}

function bindRemoveButton() {
    $(document).off("click", "button[operation='remove-job'][data-toggle!='modal']");
    $(document).on("click", "button[operation='remove-job'][data-toggle!='modal']", function(event) {
        var jobName = $(event.currentTarget).attr("job-name");
        showDeleteConfirmModal();
        $(document).off("click", "#confirm-btn");
        $(document).on("click", "#confirm-btn", function() {
            $.ajax({
                url: "/api/jobs/config/" + jobName,
                type: "DELETE",
                success: function() {
                    $("#confirm-dialog").modal("hide");
                    $(".modal-backdrop").remove();
                    $("body").removeClass("modal-open");
                    refreshJobNavTag();
                    refreshServerNavTag();
                    $("#jobs-status-overview-tbl").bootstrapTable("refresh");
                }
            });
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
    $("#reconcile-interval-minutes").attr("value", data.reconcileIntervalMinutes);
    $("#description").text(data.description);
    $("#script-command-line").attr("value", data.scriptCommandLine);
    if ("DATAFLOW" === $("#job-type").val()) {
        $("#streaming-process-group").show();
    }
    if ("SCRIPT" === $("#job-type").val()) {
        $("#script-commandLine-group").show();
    }
}
