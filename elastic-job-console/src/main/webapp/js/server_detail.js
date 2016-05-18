$(function() {
    renderJobs();
    bindPauseButtons();
    bindPauseLeaderButtons();
    bindResumeButtons();
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
            var leader = data[i].leader;
            var baseTd = "<td>" + data[i].jobName + "</td><td>" + status + "</td><td>" + data[i].processSuccessCount + "</td><td>" + data[i].processFailureCount + "</td><td>" + data[i].sharding + "</td><td>" + (true === leader ? "<span class='glyphicon glyphicon-ok'></span>" : "<span class='glyphicon glyphicon-remove'></span>") + "</td>";
            var operationTd = "";
            var resumeButton = "<button operation='resume' class='btn btn-success' job-name='" + data[i].jobName + "'>恢复</button>";
            var resumeWithWarningButton = "<button operation='resume' class='btn btn-success disabled' job-name='" + data[i].jobName + "' disabled title='先恢复主节点才能恢复从节点作业'>恢复</button>";
            var pauseButton = "<button operation='pause' class='btn btn-warning' job-name='" + data[i].jobName + "'" + (leader ? "data-toggle='modal' data-target='#pause-leader-confirm-dialog'" : "") + ">暂停</button>";
            var shutdownButton = "<button operation='shutdown' class='btn btn-danger' job-name='" + data[i].jobName + "'>关闭</button>";
            var removeButton = "<button operation='remove' class='btn btn-danger' job-name='" + data[i].jobName + "'>删除</button>";
            if ("PAUSED" === status) {
                if (data[i].leaderPaused && !leader) {
                    operationTd = resumeWithWarningButton + "&nbsp;";
                } else {
                    operationTd = resumeButton + "&nbsp;";
                }
            } else if ("DISABLED" !== status && "CRASHED" !== status && "SHUTDOWN" !== status) {
                operationTd = pauseButton + "&nbsp;";
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

function bindPauseLeaderButtons() {
    $(document).on("click", "#pause-leader-confirm-dialog-confirm-btn,#pause-all-jobs-btn", function(event) {
        $.post("job/pauseAll/name", {jobName : $("#chosen-job-name").text()}, function (data) {
            $("#pause-leader-confirm-dialog").modal("hide");
            renderJobs();
            showSuccessDialog();
        });
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
