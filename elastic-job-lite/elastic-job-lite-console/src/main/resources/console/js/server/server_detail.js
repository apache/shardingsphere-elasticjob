$(function() {
    $("#server-ip").text(getCurrentUrl("serverIp"));
    renderJobs();
    bindTriggerButtons();
    bindPauseButtons();
    bindResumeButtons();
    bindTriggerAllButton();
    bindPauseAllButton();
    bindResumeAllButton();
    bindShutdownButtons();
    bindRemoveButtons();
});

function renderJobs() {
    var ip = $("#server-ip").text();
    $("#jobs").bootstrapTable({
        url: "/api/server/jobs/" + ip,
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
        columns: 
        [{
            field: "jobName",
            title: "作业名"
        }, {
            field: "status",
            title: "状态"
        }, {
            field: "sharding",
            title: "分片项"
        },{
            field: "operation",
            title: "操作",
            formatter: "generateOperationButtons"
        }]
    });
}

function generateOperationButtons(val, row){
    var operationTd = "";
    var triggerButton = "<button operation='trigger' class='btn-xs btn-success' job-name='" + row.jobName + "'>触发</button>";
    var resumeButton = "<button operation='resume' class='btn-xs btn-info' job-name='" + row.jobName + "'>恢复</button>";
    var pauseButton = "<button operation='pause' class='btn-xs btn-warning' job-name='" + row.jobName + "'>暂停</button>";
    var shutdownButton = "<button operation='shutdown' class='btn-xs btn-danger' job-name='" + row.jobName + "'>关闭</button>";
    var removeButton = "<button operation='remove' class='btn-xs btn-danger' job-name='" + row.jobName + "'>删除</button>";
    operationTd = triggerButton + "&nbsp;";
    if ("PAUSED" === row.status) {
        operationTd = triggerButton + "&nbsp;" + resumeButton;
    } else if ("DISABLED" !== row.status && "CRASHED" !== row.status && "SHUTDOWN" !== row.status) {
        operationTd = triggerButton  + "&nbsp;" + pauseButton;
    }
    if ("SHUTDOWN" !== row.status) {
        operationTd = operationTd  + "&nbsp;" + shutdownButton;
    }
    if ("SHUTDOWN" === row.status || "CRASHED" === row.status) {
        operationTd = removeButton;
    }
    return operationTd;
}

function bindTriggerButtons() {
    $(document).on("click", "button[operation='trigger'][data-toggle!='modal']", function(event) {
        $.ajax({
            url: "/api/job/trigger",
            type: "POST",
            data: JSON.stringify({jobName : $(event.currentTarget).attr("job-name"), ip : $("#server-ip").text()}),
            contentType: "application/json",
            dataType: "json",
            success: function(){
                $("#jobs").bootstrapTable("refresh");
                showSuccessDialog();
            }
        });
    });
    $(document).on("click", "button[operation='trigger'][data-toggle='modal']", function(event) {
        $("#chosen-job-name").text($(event.currentTarget).attr("job-name"));
    });
}

function bindPauseButtons() {
    $(document).on("click", "button[operation='pause'][data-toggle!='modal']", function(event) {
        $.ajax({
            url: "/api/job/pause",
            type: "POST",
            data: JSON.stringify({jobName : $(event.currentTarget).attr("job-name"), ip : $("#server-ip").text()}),
            contentType: "application/json",
            dataType: "json",
            success: function(){
                $("#jobs").bootstrapTable("refresh");
                showSuccessDialog();
            }
        });
    });
    $(document).on("click", "button[operation='pause'][data-toggle='modal']", function(event) {
        $("#chosen-job-name").text($(event.currentTarget).attr("job-name"));
    });
}

function bindResumeButtons() {
    $(document).on("click", "button[operation='resume']", function(event) {
        $.ajax({
            url: "/api/job/resume",
            type: "POST",
            data: JSON.stringify({jobName : $(event.currentTarget).attr("job-name"), ip : $("#server-ip").text()}),
            contentType: "application/json",
            dataType: "json",
            success: function(){
                $("#jobs").bootstrapTable("refresh");
                showSuccessDialog();
            }
        });
    });
}

function bindTriggerAllButton() {
    $(document).on("click", "#trigger-all-jobs-btn", function() {
        $.ajax({
            url: "/api/job/triggerAll/ip",
            type: "POST",
            data: JSON.stringify({ip : $("#server-ip").text()}),
            contentType: "application/json",
            dataType: "json",
            success: function(){
                $("#jobs").bootstrapTable("refresh");
                showSuccessDialog();
            }
        });
    });
}

function bindPauseAllButton() {
    $(document).on("click", "#pause-all-jobs-btn", function() {
        $.ajax({
            url: "/api/job/pauseAll/ip",
            type: "POST",
            data: JSON.stringify({ip : $("#server-ip").text()}),
            contentType: "application/json",
            dataType: "json",
            success: function(){
                $("#jobs").bootstrapTable("refresh");
                showSuccessDialog();
            }
        });
    });
}

function bindResumeAllButton() {
    $(document).on("click", "#resume-all-jobs-btn", function() {
        $.ajax({
            url: "/api/job/resumeAll/ip",
            type: "POST",
            data: JSON.stringify({ip : $("#server-ip").text()}),
            contentType: "application/json",
            dataType: "json",
            success: function(){
                $("#jobs").bootstrapTable("refresh");
                showSuccessDialog();
            }
        });
    });
}

function bindShutdownButtons() {
    $(document).on("click", "button[operation='shutdown']", function(event) {
        $.ajax({
            url: "/api/job/shutdown",
            type: "POST",
            data: JSON.stringify({jobName : $(event.currentTarget).attr("job-name"), ip : $("#server-ip").text()}),
            contentType: "application/json",
            dataType: "json",
            success: function(){
                $("#jobs").bootstrapTable("refresh");
                showSuccessDialog();
            }
        });
    });
    $(document).on("click", "button[operation='shutdown']", function(event) {
        $("#chosen-job-name").text($(event.currentTarget).attr("job-name"));
    });
}

function bindRemoveButtons() {
    $(document).on("click", "button[operation='remove']", function(event) {
        $.ajax({
            url: "/api/job/remove",
            type: "POST",
            data: JSON.stringify({jobName : $(event.currentTarget).attr("job-name"), ip : $("#server-ip").text()}),
            contentType: "application/json",
            dataType: "json",
            success: function(data){
                if (data.length > 0) {
                    showFailureDialog("remove-job-failure-dialog");
                } else {
                    showSuccessDialog();
                }
                $("#jobs").bootstrapTable("refresh");
            }
        });
    });
    $(document).on("click", "button[operation='remove']", function(event) {
        $("#chosen-job-name").text($(event.currentTarget).attr("job-name"));
    });
}
