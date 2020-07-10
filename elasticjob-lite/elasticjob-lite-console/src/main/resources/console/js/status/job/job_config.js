/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

$(function() {
    tooltipLocale();
    validate();
    bindSubmitJobConfigurationForm();
    bindResetForm();
});

function tooltipLocale(){
    for (var i = 0; i < $("[data-toggle='tooltip']").length; i++) {
        var object = $("[data-toggle='tooltip']")[i];
        $(object).attr('title',$.i18n.prop("placeholder-" + object.getAttribute("id"))).tooltip('fixTitle');
    }
}

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

function bindSubmitJobConfigurationForm() {
    $("#update-job-info-btn").on("click", function(){
        var bootstrapValidator = $("#job-config-form").data("bootstrapValidator");
        bootstrapValidator.validate();
        if (bootstrapValidator.isValid()) {
            var jobName = $("#job-name").val();
            var shardingTotalCount = $("#sharding-total-count").val();
            var jobParameter = $("#job-parameter").val();
            var cron = $("#cron").val();
            var streamingProcess = $("#streaming-process").prop("checked");
            var maxTimeDiffSeconds = $("#max-time-diff-seconds").val();
            var monitorExecution = $("#monitor-execution").prop("checked");
            var failover = $("#failover").prop("checked");
            var misfire = $("#misfire").prop("checked");
            var driver = $("#driver").val();
            var url = $("#url").val();
            var username = $("#username").val();
            var password = $("#password").val();
            var logLevel = $("#logLevel").val();
            var shardingItemParameters = $("#sharding-item-parameters").val();
            var jobShardingStrategyType = $("#job-sharding-strategy-type").val();
            var scriptCommandLine = $("#script-command-line").val();
            var jobExecutorServiceHandlerType = $("#job-executor-service-handler-type").val();
            var jobErrorHandlerType = $("#job-error-handler-type").val();
            var description = $("#description").val();
            var reconcileIntervalMinutes = $("#reconcile-interval-minutes").val();
            var postJson = {
                jobName: jobName,
                shardingTotalCount: shardingTotalCount,
                jobParameter: jobParameter,
                cron: cron,
                shardingItemParameters: shardingItemParameters,
                maxTimeDiffSeconds: maxTimeDiffSeconds,
                monitorExecution: monitorExecution,
                failover: failover,
                misfire: misfire,
                reconcileIntervalMinutes: reconcileIntervalMinutes,
                jobShardingStrategyType: jobShardingStrategyType,
                jobExecutorServiceHandlerType: jobExecutorServiceHandlerType,
                jobErrorHandlerType: jobErrorHandlerType,
                description: description,
                props: {'streaming.process': streamingProcess, 'script.command.line': scriptCommandLine}
            };
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
                        message: $.i18n.prop("job-sharding-count-not-null")
                    },
                    regexp: {
                        regexp: /^(-?\d+)?$/,
                        message: $.i18n.prop("job-sharding-count-should-be-integer")
                    }
                }
            },
            cron: {
                validators: {
                    stringLength: {
                        max: 40,
                        message: $.i18n.prop("job-cron-length-limit")
                    },
                    notEmpty: {
                        message: $.i18n.prop("job-cron-not-null")
                    }
                }
            }
        }
    });
    $("#job-config-form").submit(function(event) {
        event.preventDefault();
    });
}

function bindResetForm() {
    $("#reset").click(function() {
        $("#job-config-form").data("bootstrapValidator").resetForm();
    });
}
