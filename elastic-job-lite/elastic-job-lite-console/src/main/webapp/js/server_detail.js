$(function() {
    $("#server-ip").text(GetQueryParam("serverIp"));
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
    $('#jobs').bootstrapTable({
        url: 'server/jobs?ip=' + ip,
        method: 'get',
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
            field: 'jobName',
            title: '作业名'
        }, {
            field: 'status',
            title: '状态'
        }, {
            field: 'sharding',
            title: '分片项'
        },{
            field: 'operation',
            title: '操作',
            formatter: 'operFormatter'
        }]
    });
}

function operFormatter(val, row){
    var operationTd = "";
    var triggerButton = "<button operation='trigger' class='btn btn-success' job-name='" + row.jobName + "'>触发</button>";
    var resumeButton = "<button operation='resume' class='btn btn-success' job-name='" + row.jobName + "'>恢复</button>";
    var pauseButton = "<button operation='pause' class='btn btn-warning' job-name='" + row.jobName + "'>暂停</button>";
    var shutdownButton = "<button operation='shutdown' class='btn btn-danger' job-name='" + row.jobName + "'>关闭</button>";
    var removeButton = "<button operation='remove' class='btn btn-danger' job-name='" + row.jobName + "'>删除</button>";
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
        $.post("job/trigger", {jobName : $(event.currentTarget).attr("job-name"), ip : $("#server-ip").text()}, function (data) {
            $('#jobs').bootstrapTable('refresh');
            showSuccessDialog();
        });
    });
    $(document).on("click", "button[operation='trigger'][data-toggle='modal']", function(event) {
        $("#chosen-job-name").text($(event.currentTarget).attr("job-name"));
    });
}

function bindPauseButtons() {
    $(document).on("click", "button[operation='pause'][data-toggle!='modal']", function(event) {
        $.post("job/pause", {jobName : $(event.currentTarget).attr("job-name"), ip : $("#server-ip").text()}, function (data) {
            $('#jobs').bootstrapTable('refresh');
            showSuccessDialog();
        });
    });
    $(document).on("click", "button[operation='pause'][data-toggle='modal']", function(event) {
        $("#chosen-job-name").text($(event.currentTarget).attr("job-name"));
    });
}

function bindResumeButtons() {
    $(document).on("click", "button[operation='resume']", function(event) {
        $.post("job/resume", {jobName : $(event.currentTarget).attr("job-name"), ip : $("#server-ip").text()}, function () {
            $('#jobs').bootstrapTable('refresh');
            showSuccessDialog();
        });
    });
}

function bindTriggerAllButton() {
    $(document).on("click", "#trigger-all-jobs-btn", function() {
        $.post("job/triggerAll/ip", {ip : $("#server-ip").text()}, function () {
            $('#jobs').bootstrapTable('refresh');
            showSuccessDialog();
        });
    });
}

function bindPauseAllButton() {
    $(document).on("click", "#pause-all-jobs-btn", function() {
        $.post("job/pauseAll/ip", {ip : $("#server-ip").text()}, function () {
            $('#jobs').bootstrapTable('refresh');
            showSuccessDialog();
        });
    });
}

function bindResumeAllButton() {
    $(document).on("click", "#resume-all-jobs-btn", function() {
        $.post("job/resumeAll/ip", {ip : $("#server-ip").text()}, function () {
            $('#jobs').bootstrapTable('refresh');
            showSuccessDialog();
        });
    });
}

function bindShutdownButtons() {
    $(document).on("click", "button[operation='shutdown']", function(event) {
        $.post("job/shutdown", {jobName : $(event.currentTarget).attr("job-name"), ip : $("#server-ip").text()}, function (data) {
            $('#jobs').bootstrapTable('refresh');
            showSuccessDialog();
        });
    });
    $(document).on("click", "button[operation='shutdown']", function(event) {
        $("#chosen-job-name").text($(event.currentTarget).attr("job-name"));
    });
}

function bindRemoveButtons() {
    $(document).on("click", "button[operation='remove']", function(event) {
        $.post("job/remove", {jobName : $(event.currentTarget).attr("job-name"), ip : $("#server-ip").text()}, function (data) {
            if (data.length > 0) {
                showFailureDialog("remove-job-failure-dialog");
            } else {
                showSuccessDialog();
            }
            $('#jobs').bootstrapTable('refresh');
        });
    });
    $(document).on("click", "button[operation='remove']", function(event) {
        $("#chosen-job-name").text($(event.currentTarget).attr("job-name"));
    });
}