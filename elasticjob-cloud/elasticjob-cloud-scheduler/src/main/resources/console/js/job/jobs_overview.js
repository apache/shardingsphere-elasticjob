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
    authorityControl();
    renderJobOverview();
    $("#add-job").click(function() {
        $(".box-body").remove();
        $("#add-job-body").load("html/job/add_job.html", null, function() {
            doLocale();
            tooltipLocale();
        });
        $("#data-add-job").modal({backdrop : "static", keyboard : true});
    });
    bindDetailJobButton();
    bindDeleteJobButton();
    bindModifyJobButton();
    bindEnableJobButton();
    bindDisableJobButton();
});

function renderJobOverview() {
    var jsonData = {
        url: "/api/job/jobs",
        cache: false
    };
    $("#job-table").bootstrapTable({
        columns: jsonData.columns,
        url: jsonData.url,
        cache: jsonData.cache
    }).on("all.bs.table", function() {
        doLocale();
    });
}

function operationJob(val, row) {
    var detailButton = "<button operation='detailJob' class='btn-xs btn-info' jobName='" + row.jobName + "' data-lang='operation-detail'></button>";
    var modifyButton = "<button operation='modifyJob' class='btn-xs btn-warning' jobName='" + row.jobName + "' data-lang='operation-update'></button>";
    var deleteButton = "<button operation='deleteJob' class='btn-xs btn-danger' jobName='" + row.jobName + "' data-lang='operation-delete'></button>";
    var enableButton = "<button operation='enableJob' class='btn-xs btn-success' jobName='" + row.jobName + "' appName='" + row.appName + "' data-lang='operation-enable'></button>";
    var disableButton = "<button operation='disableJob' class='btn-xs btn-warning' jobName='" + row.jobName + "' data-lang='operation-disable'></button>";
    var operationId = detailButton + "&nbsp;" + modifyButton  +"&nbsp;" + deleteButton;
    if(selectJobStatus(row.jobName)) {
        operationId = operationId + "&nbsp;" + enableButton;
    }else{
        operationId = operationId + "&nbsp;" + disableButton;
    }
    return operationId;
}

function selectJobStatus(jobName) {
    var resultValue = null;
    $.ajax({
        type:"GET",
        async: false,
        url: "/api/job/" + jobName + "/disable",
        contentType: "application/json",
        success: function(result) {
            resultValue = result;
        }
    });
    return resultValue;
}

function bindDetailJobButton() {
    $(document).off("click", "button[operation='detailJob'][data-toggle!='modal']");
    $(document).on("click", "button[operation='detailJob'][data-toggle!='modal']", function(event) {
        var jobName = $(event.currentTarget).attr("jobName");
        $.ajax({
            url: "/api/job/jobs/" + jobName,
            contentType: "application/json",
            success: function(result) {
                $(".box-body").remove();
                $("#detail-job-body").load("html/job/detail_job.html", null, function() {
                    renderJob(result);
                    doLocale();
                    tooltipLocale();
                    $("#data-detail-job").modal({backdrop : "static", keyboard : true});
                    $("#close-button").on("click", function(){
                        $("#data-detail-job").modal("hide");
                    });
                });
            }
        });
    });
}

function bindDeleteJobButton() {
    $(document).off("click", "button[operation='deleteJob'][data-toggle!='modal']");
    $(document).on("click", "button[operation='deleteJob'][data-toggle!='modal']", function(event) {
        var jobName = $(event.currentTarget).attr("jobName");
        $("#delete-data").modal({backdrop : "static", keyboard : true});
        var flag = true;
        $("#delete-job-remove").on("click", function() {
            flag = false;
        });
        $("#delete-job-confirm").on("click", function() {
            if(flag) {
                $.ajax({
                    url: "/api/job/deregister",
                    type: "DELETE",
                    contentType: "application/json",
                    data: jobName,
                    success: function(result) {
                        $("#job-table").bootstrapTable("refresh");
                        $("#delete-data").hide();
                        refreshJobNavTag();
                    }
                });
            }
        });
    });
}

function bindModifyJobButton() {
    $(document).off("click", "button[operation='modifyJob'][data-toggle!='modal']");
    $(document).on("click", "button[operation='modifyJob'][data-toggle!='modal']", function(event) {
        var jobName = $(event.currentTarget).attr("jobName");
        $.ajax({
            url: "/api/job/jobs/" + jobName,
            success: function(result) {
                if (null !== result) {
                    $(".box-body").remove();
                    $("#update-job-body").load("html/job/modify_job.html", null, function() {
                        doLocale();
                        tooltipLocale();
                        $('#data-update-job').modal({backdrop : "static", keyboard : true});
                        renderJob(result);
                    });
                }
            }
        });
    });
}

function bindEnableJobButton() {
    $(document).off("click", "button[operation='enableJob'][data-toggle!='modal']");
    $(document).on("click", "button[operation='enableJob'][data-toggle!='modal']", function(event) {
        var jobName = $(event.currentTarget).attr("jobName");
        var appName = $(event.currentTarget).attr("appName");
        if(selectAppStatus(appName)){
            showFailDialog();
        } else {
            $.ajax({
                url: "/api/job/" + jobName + "/enable",
                type: "POST",
                contentType: "application/json",
                success: function(result) {
                    $("#job-table").bootstrapTable("refresh");
                    showSuccessDialog();
                }
            });
        }
    });
}

function bindDisableJobButton() {
    $(document).off("click", "button[operation='disableJob'][data-toggle!='modal']");
    $(document).on("click", "button[operation='disableJob'][data-toggle!='modal']", function(event) {
        var jobName = $(event.currentTarget).attr("jobName");
        $.ajax({
            url: "/api/job/" + jobName + "/disable",
            type: "POST",
            contentType: "application/json",
            success: function(result) {
                $("#job-table").bootstrapTable("refresh");
                showSuccessDialog();
            }
        });
    });
}

function renderJob(job) {
    $("#job-name").attr("value", job.jobName);
    $("#job-app-name").attr("value", job.appName);
    $("#cron").attr("value", job.cron);
    $("#job-execution-type").val(job.jobExecutionType);
    $("#sharding-total-count").attr("value", job.shardingTotalCount);
    $("#job-parameter").attr("value", job.jobParameter);
    $("#cpu-count").attr("value", job.cpuCount);
    $("#job-memory").attr("value", job.memoryMB);
    $("#bean-name").attr("value", job.beanName);
    $("#application-context").attr("value", job.applicationContext);
    $("#description").val(job.description);
    $("#sharding-item-parameters").val(job.shardingItemParameters);
    $("#failover").prop("checked", job.failover);
    $("#misfire").prop("checked", job.misfire);
    $("#streaming-process").prop("checked", job.streamingProcess);
    $("#script-command-line").val(job.scriptCommandLine);
}

