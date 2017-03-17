$(function() {
    $("#job-name").text(getCurrentUrl("jobName"));
    renderSettings();
    validate();
    bindSubmitJobSettingsForm();
    $('[href="#settings"]').click(function() {
        renderSettings();
    });
    $('[href="#servers"]').click(function() {
        renderServers();
    });
    $('[href="#execution-info"]').click(function() {
        renderExecution();
    });
    bindTriggerButtons();
    bindTriggerAllButton();
    bindPauseButtons();
    bindPauseAllButton();
    bindResumeButtons();
    bindResumeAllButton();
    bindShutdownButtons();
    bindRemoveButtons();
    bindDisableButtons();
    bindEnableButtons();
});

function renderSettings() {
    var jobName = $("#job-name").text();
    $.ajax({
        url: "/job/settings/" + jobName,
        async: false,
        success: function(data) {
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
    });
}

function bindSubmitJobSettingsForm() {
    $("#update-job-info-btn").on("click", function(ev){
        var bootstrapValidator = $("#job-settings-form").data("bootstrapValidator");
        bootstrapValidator.validate();
        if (bootstrapValidator.isValid()) {
            var jobName = $("#job-name").text();
            var jobType = $("#job-type").val();
            var jobClass = $("#job-class").val();
            var shardingTotalCount = $("#sharding-total-count").val();
            var jobParameter = $("#job-parameter").val();
            var cron = $("#cron").val();
            var streamingProcess = $("#streaming-process").prop("checked");
            var maxTimeDiffSeconds = $("#max-time-diff-seconds").val();
            var monitorPort = $("#monitor-port").val();
            var monitorExecution = $("#monitor-execution").prop("checked");
            var failover = $("#failover").prop("checked");
            var misfire = $("#misfire").prop("checked");
            var driver = $("#driver").val();
            var url = $("#url").val();
            var username = $("#username").val();
            var password = $("#password").val();
            var logLevel = $("#logLevel").val();
            var shardingItemParameters = $("#sharding-item-parameters").val();
            var jobShardingStrategyClass = $("#job-sharding-strategy-class").val();
            var scriptCommandLine = $("#script-command-line").val();
            var executorServiceHandler = $("#executor-service-handler").val();
            var jobExceptionHandler = $("#job-exception-handler").val();
            var description = $("#description").val();
            var reconcileCycleTime = $("#reconcile-cycle-time").val();
            var postJson = {jobName: jobName, jobType : jobType, jobClass : jobClass, shardingTotalCount: shardingTotalCount, jobParameter: jobParameter, cron: cron, streamingProcess: streamingProcess, maxTimeDiffSeconds: maxTimeDiffSeconds, monitorPort: monitorPort, monitorExecution: monitorExecution, failover: failover, misfire: misfire, shardingItemParameters: shardingItemParameters, jobShardingStrategyClass: jobShardingStrategyClass, jobProperties: {"executor_service_handler": executorServiceHandler, "job_exception_handler": jobExceptionHandler}, description: description, scriptCommandLine: scriptCommandLine, reconcileCycleTime:reconcileCycleTime};
            $.ajax({
                url: "/job/settings",
                type: "POST",
                data: JSON.stringify(postJson),
                contentType: "application/json",
                dataType: "json",
                success: function() {
                    showSuccessDialog();
                    if (monitorExecution) {
                        $("#execution-info-tab").removeClass("disabled");
                    } else {
                        $("#execution-info-tab").addClass("disabled");
                    }
                }
            });
        }
    });
}

function renderServers() {
    var jobName = $("#job-name").text();
    $("#job-servers").bootstrapTable({
        url: "/job/servers/" + jobName,
        method: "get",
        cache: false,
        rowStyle: function (row, index) {
            var strclass = "";
            if ("READY" === row.status) {
                strclass = "info";
            } else if ("RUNNING" === row.status) {
                strclass = "success";
            } else if ("DISABLED" === row.status || "PAUSED" === row.status) {
                strclass = "warning";
            } else if ("CRASHED" === row.status || "SHUTDOWN" === row.status) {
                strclass = "danger";
            } else {
                return {};
            }
            return { classes: strclass }
        },
        columns: [
        {
            field: "ip",
            title: "IP地址"
        }, {
            field: "hostName",
            title: "机器名"
        }, {
            field: "status",
            title: "状态"
        }, {
            field: "sharding",
            title: "分片项"
        }, {
            field: "operation",
            title: "操作",
            formatter: "generateOperationButtons"
        }]
    });
}

function generateOperationButtons(val, row) {
    var triggerButton = "<button operation='trigger' class='btn btn-success' ip='" + row.ip + "'>触发</button>";
    var resumeButton = "<button operation='resume' class='btn btn-success' ip='" + row.ip + "'>恢复</button>";
    var pauseButton = "<button operation='pause' class='btn btn-warning' ip='" + row.ip + "'" + ">暂停</button>";
    var shutdownButton = "<button operation='shutdown' class='btn btn-danger' ip='" + row.ip + "'>关闭</button>";
    var removeButton = "<button operation='remove' class='btn btn-danger' ip='" + row.ip + "'>删除</button>";
    var disableButton = "<button operation='disable' class='btn btn-danger' ip='" + row.ip + "'>失效</button>";
    var enableButton = "<button operation='enable' class='btn btn-success' ip='" + row.ip + "'>生效</button>";
    var operationTd = triggerButton + "&nbsp;";
    if ("PAUSED" === row.status) {
        operationTd = operationTd + resumeButton + "&nbsp;";
    } else if ("DISABLED" !== row.status && "CRASHED" !== row.status && "SHUTDOWN" !== row.status) {
        operationTd = operationTd + pauseButton + "&nbsp;";
    }
    if ("SHUTDOWN" !== row.status) {
        operationTd = operationTd + shutdownButton + "&nbsp;";
    }
    if ("SHUTDOWN" === row.status || "CRASHED" === row.status) {
        operationTd = removeButton + "&nbsp;";
    }
    if ("DISABLED" === row.status) {
        operationTd = operationTd + enableButton;
    } else if ("CRASHED" !== row.status && "SHUTDOWN" !== row.status){
        operationTd = operationTd + disableButton;
    }
    return operationTd;
}

function bindTriggerButtons() {
    $(document).on("click", "button[operation='trigger'][data-toggle!='modal']", function(event) {
        var jobName = $("#job-name").text();
        $.ajax({
            url: "/job/trigger",
            type: "POST",
            data: JSON.stringify({jobName : jobName, ip : $(event.currentTarget).attr("ip")}),
            contentType: "application/json",
            dataType: "json",
            success: function() {
                $("#job-servers").bootstrapTable("refresh");
                showSuccessDialog();
            }
        });
    });
}

function bindTriggerAllButton() {
    $(document).on("click", "#trigger-all-jobs-btn", function() {
        var jobName = $("#job-name").text();
        $.ajax({
            url: "/job/triggerAll/name",
            type: "POST",
            data: JSON.stringify({jobName : jobName}),
            contentType: "application/json",
            dataType: "json",
            success: function() {
                $("#job-servers").bootstrapTable("refresh");
                showSuccessDialog();
            }
        });
    });
}

function bindPauseButtons() {
    $(document).on("click", "button[operation='pause'][data-toggle!='modal']", function(event) {
        var jobName = $("#job-name").text();
        $.ajax({
            url: "/job/pause",
            type: "POST",
            data: JSON.stringify({jobName : jobName, ip : $(event.currentTarget).attr("ip")}),
            contentType: "application/json",
            dataType: "json",
            success: function() {
                $("#job-servers").bootstrapTable("refresh");
                showSuccessDialog();
            }
        });
    });
}

function bindPauseAllButton() {
    $(document).on("click", "#pause-all-jobs-btn", function() {
        var jobName = $("#job-name").text();
        $.ajax({
            url: "/job/pauseAll/name",
            type: "POST",
            data: JSON.stringify({jobName : jobName}),
            contentType: "application/json",
            dataType: "json",
            success: function() {
                $("#job-servers").bootstrapTable("refresh");
                showSuccessDialog();
            }
        });
    });
}

function bindResumeButtons() {
    $(document).on("click", "button[operation='resume']", function(event) {
        var jobName = $("#job-name").text();
        $.ajax({
            url: "/job/resume",
            type: "POST",
            data: JSON.stringify({jobName : jobName, ip : $(event.currentTarget).attr("ip")}),
            contentType: "application/json",
            dataType: "json",
            success: function() {
                $("#job-servers").bootstrapTable("refresh");
                showSuccessDialog();
            }
        });
    });
}

function bindResumeAllButton() {
    $(document).on("click", "#resume-all-jobs-btn", function() {
        var jobName = $("#job-name").text();
        $.ajax({
            url: "/job/resumeAll/name",
            type: "POST",
            data: JSON.stringify({jobName : jobName}),
            contentType: "application/json",
            dataType: "json",
            success: function() {
                $("#job-servers").bootstrapTable("refresh");
                showSuccessDialog();
            }
        });
    });
}

function renderExecution() {
    var jobName = $("#job-name").text();
    $("#execution").bootstrapTable({
        url: "/job/execution/" + jobName,
        method: "get",
        cache: false,
        rowStyle: function (row, index) {
            var strclass = "";
            if ("RUNNING" === row.status) {
                strclass = "success";
            } else if ("COMPLETED" === row.status) {
                strclass = "info";
            } else if ("PENDING" === row.status) {
                strclass = "warning";
            } else {
                return {};
            }
            return { classes : strclass }
        },
        columns: [
        {
            field: "item",
            title: "分片项"
        }, {
            field: "status",
            title: "状态"
        }, {
            field: "failoverIp",
            title: "失效转移执行"
        }, {
            field: "lastBeginTime",
            title: "上次作业开始时间",
            formatter: "dateFormatter"
        }, {
            field: "lastCompleteTime",
            title: "上次作业完成时间",
            formatter: "dateFormatter"
        }, {
            field: "nextFireTime",
            title: "下次作业运行时间",
            formatter: "dateFormatter"
        }]
    });
}
function dateFormatter(val, row) {
    return null === row.nextFireTime ? null : new Date(row.nextFireTime).toLocaleString();
}

function ipFormatter(val, row) {
    return null === row.failoverIp ? "-" : row.failoverIp;
}

function bindShutdownButtons() {
    $(document).on("click", "button[operation='shutdown']", function(event) {
        var jobName = $("#job-name").text();
        $.ajax({
            url: "/job/shutdown",
            type: "POST",
            data: JSON.stringify({jobName : jobName, ip : $(event.currentTarget).attr("ip")}),
            contentType: "application/json",
            dataType: "json",
            success: function() {
                $("#job-servers").bootstrapTable("refresh");
                showSuccessDialog();
            }
        });
    });
}

function bindRemoveButtons() {
    $(document).on("click", "button[operation='remove']", function(event) {
        var jobName = $("#job-name").text();
        $.ajax({
            url: "/job/remove",
            type: "POST",
            data: JSON.stringify({jobName : jobName, ip : $(event.currentTarget).attr("ip")}),
            contentType: "application/json",
            dataType: "json",
            success: function(data) {
                if (data.length > 0) {
                    showFailureDialog("remove-job-failure-dialog");
                } else {
                    showSuccessDialog();
                }
                $("#job-servers").bootstrapTable("refresh");
            }
        });
    });
}

function bindDisableButtons() {
    $(document).on("click", "button[operation='disable']", function(event) {
        var jobName = $("#job-name").text();
        $.ajax({
            url: "/job/disable",
            type: "POST",
            data: JSON.stringify({jobName : jobName, ip : $(event.currentTarget).attr("ip")}),
            contentType: "application/json",
            dataType: "json",
            success: function() {
                $("#job-servers").bootstrapTable("refresh");
                showSuccessDialog();
            }
        });
    });
}

function bindEnableButtons() {
    $(document).on("click", "button[operation='enable']", function(event) {
        var jobName = $("#job-name").text();
        $.ajax({
            url: "/job/enable",
            type: "POST",
            data: JSON.stringify({jobName : jobName, ip : $(event.currentTarget).attr("ip")}),
            contentType: "application/json",
            dataType: "json",
            success: function() {
                $("#job-servers").bootstrapTable("refresh");
                showSuccessDialog();
            }
        });
    });
}

function validate() {
    $("#job-settings-form").bootstrapValidator({
        message: "This value is not valid",
        feedbackIcons: {
            valid: "glyphicon glyphicon-ok",
            invalid: "glyphicon glyphicon-remove",
            validating: "glyphicon glyphicon-refresh"
        },
        fields: {
            shardingTotalCount: {
                validators: {
                    notEmpty: {
                        message: "分片数量不能为空"
                    },
                    regexp: {
                        regexp: /^(-?\d+)?$/,
                        message: "分片数量只能是整数"
                    }
                }
            },
            cron: {
                validators: {
                    stringLength: {
                        max: 40,
                        message: "cron表达式不能超过40字符大小"
                    },
                    notEmpty: {
                        message: "cron表达式不能为空"
                    }
                }
            },
            monitorPort: {
                validators: {
                    regexp: {
                        regexp: /^(-?\d+)?$/,
                        message: "监控端口只能是整数"
                    },
                    notEmpty: {
                        message: "监控端口不能为空"
                    },
                    callback: {
                        message: "监控端口范围必须在1000~65535之间，-1表示不启用端口监控",
                        callback: function(value, validator) {
                            var monitorPort = parseInt(validator.getFieldElements("monitorPort").val(), 10);
                            if ((monitorPort >= 1000 && monitorPort <= 65535) || monitorPort === -1) {
                                validator.updateStatus("monitorPort", "VALID");
                                return true;
                            }
                            return false;
                        }
                    }
                }
            }
        }
    });
    $("#job-settings-form").submit(function(event) {
        event.preventDefault();
    });
}
