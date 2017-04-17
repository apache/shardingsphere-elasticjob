$(function() {
    $("#add-job").click(function() {
        $(".box-body").remove();
        $("#add-job-body").load("html/job/add_job.html");
        $("#data-add-job").modal({backdrop : "static", keyboard : true});
    });
    bindDetailJobButton();
    bindDeleteJobButton();
    bindModifyJobButton();
    bindEnableJobButton();
    bindDisableJobButton();
});

function operationJob(val, row) {
    var detailButton = "<button operation='detailJob' class='btn btn-info' jobName='" + row.jobName + "'>详情</button>";
    var modifyButton = "<button operation='modifyJob' class='btn btn-warning' jobName='" + row.jobName + "'>修改</button>";
    var deleteButton = "<button operation='deleteJob' class='btn btn-danger' jobName='" + row.jobName + "'>删除</button>";
    var enableButton = "<button operation='enableJob' class='btn btn-success' jobName='" + row.jobName + "'>生效</button>";
    var disableButton = "<button operation='disableJob' class='btn btn-warning' jobName='" + row.jobName + "'>失效</button>";
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
    $(document).on("click", "button[operation='detailJob'][data-toggle!='modal']", function(event) {
        var jobName = $(event.currentTarget).attr("jobName");
        $.ajax({
            url: "/api/job/jobs/" + jobName,
            contentType: "application/json",
            success: function(result) {
                $(".box-body").remove();
                $("#detail-job-body").load("html/job/detail_job.html", null, function() {
                    if("SCRIPT" === result.jobType) {
                        $("#bootstrap-script-div").show();
                    } else {
                        $("#bootstrap-script-div").hide();
                    }
                    renderJob(result);
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
                    }
                });
            }
        });
    });
}

function bindModifyJobButton() {
    $(document).on("click", "button[operation='modifyJob'][data-toggle!='modal']", function(event) {
        var jobName = $(event.currentTarget).attr("jobName");
        $.ajax({
            url: "/api/job/jobs/" + jobName,
            success: function(result) {
                if (null !== result) {
                    $(".box-body").remove();
                    $("#update-job-body").load("html/job/modify_job.html", null, function() {
                        $('#data-update-job').modal({backdrop : "static", keyboard : true});
                        renderJob(result);
                    });
                }
            }
        });
    });
}

function bindEnableJobButton() {
    $(document).on("click", "button[operation='enableJob'][data-toggle!='modal']", function(event) {
        var jobName = $(event.currentTarget).attr("jobName");
        $.ajax({
            url: "/api/job/" + jobName + "/disable",
            type: "DELETE",
            contentType: "application/json",
            success: function(result) {
                $("#job-table").bootstrapTable("refresh");
                showSuccessDialog();
            }
        });
    });
}

function bindDisableJobButton() {
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
    $("#job-type").val(job.jobType);
    if("SIMPLE" === job.jobType) {
        $("#job-class").attr("value", job.jobClass);
        $("#job-class-model").show();
        $("#streaming-process").hide();
        $("#streaming-process-box").hide();
        $("#bootstrap-script-div").hide();
    } else if("DATAFLOW" === job.jobType) {
        $("#job-class").attr("value", job.jobClass);
        $("#job-class-model").show();
        $("#streaming-process").show();
        $("#streaming-process-box").show();
        $("#bootstrap-script-div").hide();
    } else if("SCRIPT" === job.jobType) {
        $("#job-class").attr("");
        $("#job-class-model").hide();
        $("#streaming-process").hide();
        $("#streaming-process-box").hide();
        $("#bootstrap-script-div").show();
    }
}
