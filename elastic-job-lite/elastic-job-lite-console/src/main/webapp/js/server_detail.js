$(function() {
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
    $.get("server/jobs", {ip : $("#server-ip").text()}, function (data) {
        $("#jobs tbody").empty();
        for (var i = 0;i < data.length;i++) {
            var ip = data[i].ip;
            var status = data[i].status;
            var baseTd = "<td>" + data[i].jobName + "</td><td>" + status + "</td><td>" + data[i].processSuccessCount + "</td><td>" + data[i].processFailureCount + "</td><td>" + data[i].sharding + "</td>";
            var operationTd = "";
            var triggerButton = "<button operation='trigger' class='btn btn-success' job-name='" + data[i].jobName + "'>触发</button>";
            var resumeButton = "<button operation='resume' class='btn btn-success' job-name='" + data[i].jobName + "'>恢复</button>";
            var pauseButton = "<button operation='pause' class='btn btn-warning' job-name='" + data[i].jobName + "'" + ">暂停</button>";
            var shutdownButton = "<button operation='shutdown' class='btn btn-danger' job-name='" + data[i].jobName + "'>关闭</button>";
            var removeButton = "<button operation='remove' class='btn btn-danger' job-name='" + data[i].jobName + "'>删除</button>";
            operationTd = triggerButton + "&nbsp;";
            if ("PAUSED" === status) {
                operationTd = triggerButton + resumeButton + "&nbsp;";
            } else if ("DISABLED" !== status && "CRASHED" !== status && "SHUTDOWN" !== status) {
                operationTd = triggerButton + pauseButton + "&nbsp;";
            }
            if ("SHUTDOWN" !== status) {
                operationTd = operationTd + shutdownButton + "&nbsp;";
            }
            if ("SHUTDOWN" === status || "CRASHED" === status) {
                operationTd = operationTd + removeButton;
            }
            operationTd = "<td>" + operationTd + "</td>";
            var trClass = "";
            if ("READY" === status) {
                trClass = "info";
            } else if ("RUNNING" === status) {
                trClass = "success";
            } else if ("DISABLED" === status || "PAUSED" === status) {
                trClass = "warning";
            } else if ("CRASHED" === status || "SHUTDOWN" === status) {
                trClass = "danger";
            }
            $("#jobs tbody").append("<tr class='" + trClass + "'>" + baseTd + operationTd + "</tr>");
        }
    });
}

function bindTriggerButtons() {
    $(document).on("click", "button[operation='trigger'][data-toggle!='modal']", function(event) {
        $.post("job/trigger", {jobName : $(event.currentTarget).attr("job-name"), ip : $("#server-ip").text()}, function (data) {
            renderJobs();
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
            renderJobs();
            showSuccessDialog();
        });
    });
    $(document).on("click", "button[operation='pause'][data-toggle='modal']", function(event) {
        $("#chosen-job-name").text($(event.currentTarget).attr("job-name"));
    });
}

function bindResumeButtons() {
    $(document).on("click", "button[operation='resume']", function(event) {
        $.post("job/resume", {jobName : $(event.currentTarget).attr("job-name"), ip : $("#server-ip").text()}, function (data) {
            renderJobs();
            showSuccessDialog();
        });
    });
}

function bindTriggerAllButton() {
    $(document).on("click", "#trigger-all-jobs-btn", function(event) {
        $.post("job/triggerAll/ip", {ip : $("#server-ip").text()}, function (data) {
            renderJobs();
            showSuccessDialog();
        });
    });
}

function bindPauseAllButton() {
    $(document).on("click", "#pause-all-jobs-btn", function(event) {
        $.post("job/pauseAll/ip", {ip : $("#server-ip").text()}, function (data) {
            renderJobs();
            showSuccessDialog();
        });
    });
}

function bindResumeAllButton() {
    $(document).on("click", "#resume-all-jobs-btn", function(event) {
        $.post("job/resumeAll/ip", {ip : $("#server-ip").text()}, function (data) {
            renderJobs();
            showSuccessDialog();
        });
    });
}

function bindShutdownButtons() {
    $(document).on("click", "button[operation='shutdown']", function(event) {
        $.post("job/shutdown", {jobName : $(event.currentTarget).attr("job-name"), ip : $("#server-ip").text()}, function (data) {
            renderJobs();
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
            renderJobs();
            showSuccessDialog();
        });
    });
    $(document).on("click", "button[operation='remove']", function(event) {
        $("#chosen-job-name").text($(event.currentTarget).attr("job-name"));
    });
}
