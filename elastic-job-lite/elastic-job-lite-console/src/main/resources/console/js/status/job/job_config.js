$(function() {
    validate();
    bindSubmitJobSettingsForm();
});

function getJobParams() {
    var jobName = $("#job-overviews-name").text();
    var jobParams;
    $.ajax({
        url: "/api/jobs/config/" + jobName,
        async: false,
        success: function(data) {
            jobParams = data;
        }
    });
    return jobParams;
}

function bindSubmitJobSettingsForm() {
    $("#update-job-info-btn").on("click", function(){
        var bootstrapValidator = $("#job-config-form").data("bootstrapValidator");
        bootstrapValidator.validate();
        if (bootstrapValidator.isValid()) {
            var jobName = $("#job-name").val();
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
            var reconcileIntervalMinutes = $("#reconcile-interval-minutes").val();
            var postJson = {jobName: jobName, jobType : jobType, jobClass : jobClass, shardingTotalCount: shardingTotalCount, jobParameter: jobParameter, cron: cron, streamingProcess: streamingProcess, maxTimeDiffSeconds: maxTimeDiffSeconds, monitorPort: monitorPort, monitorExecution: monitorExecution, failover: failover, misfire: misfire, shardingItemParameters: shardingItemParameters, jobShardingStrategyClass: jobShardingStrategyClass, jobProperties: {"executor_service_handler": executorServiceHandler, "job_exception_handler": jobExceptionHandler}, description: description, scriptCommandLine: scriptCommandLine, reconcileIntervalMinutes:reconcileIntervalMinutes};
            var jobParams = getJobParams();
            if (jobParams.monitorExecution !== monitorExecution || jobParams.failover !== failover || jobParams.misfire !== misfire) {
                showUpdateConfirmModal();
                $(document).off("click", "#confirm-btn");
                $(document).on("click", "#confirm-btn", function() {
                    $("#confirm-dialog").modal("hide");
                    submitAjax(postJson);
                });
            } else {
                submitAjax(postJson);
            }
        }
    });
}

function submitAjax(postJson) {
    $.ajax({
        url: "/api/jobs/config",
        type: "PUT",
        data: JSON.stringify(postJson),
        contentType: "application/json",
        dataType: "json",
        success: function() {
            $("#data-update-job").modal("hide");
            $("#jobs-status-overview-tbl").bootstrapTable("refresh");
            showSuccessDialog();
        }
    });
}

function validate() {
    $("#job-config-form").bootstrapValidator({
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
    $("#job-config-form").submit(function(event) {
        event.preventDefault();
    });
}
