$(function() {
    renderJobs();
    bindStopButtons();
    bindStopLeaderButtons();
    bindResumeButtons();
    bindStopAllButton();
    bindResumeAllButton();
    bindShutdownButtons();
});

function renderJobs() {
    $.get("server/jobs", {ip : $("#server-ip").text()}, function (data) {
        $("#jobs tbody").empty();
        for (var i = 0;i < data.length;i++) {
            var ip = data[i].ip;
            var status = data[i].status;
            var leader = data[i].leader;
            var baseTd = "<td>" + data[i].jobName + "</td><td>" + status + "</td><td>" + data[i].processSuccessCount + "</td><td>" + data[i].processFailureCount + "</td><td>" + data[i].sharding + "</td><td>" + (true === leader ? "<span class='glyphicon glyphicon-ok'></span>" : "<span class='glyphicon glyphicon-remove'></span>") + "</td>";
            var operationTd;
            if ("SHUTDOWN" === status) {
                operationTd = "-";
            } else if ("STOPED" === status) {
                if (data[i].leaderStoped && !leader) {
                    operationTd = "<button operation='resume' class='btn btn-success disabled' job-name='" + data[i].jobName + "' disabled title='先恢复主节点才能恢复从节点作业'>恢复</button>";
                } else {
                    operationTd = "<button operation='resume' class='btn btn-success' job-name='" + data[i].jobName + "'>恢复</button>";
                }
            } else if ("DISABLED" !== status && "CRASHED" !== status) {
                operationTd = "<button operation='stop' class='btn btn-warning' job-name='" + data[i].jobName + "'" + (leader ? "data-toggle='modal' data-target='#stop-leader-confirm-dialog'" : "") + ">暂停</button>";
            } else {
                operationTd = "";
            }
            if ("-" !== operationTd) {
                operationTd = operationTd + "&nbsp;<button operation='shutdown' class='btn btn-danger' job-name='" + data[i].jobName + "'>关闭</button>";
            }
            operationTd = "<td>" + operationTd + "</td>";
            var trClass = "";
            if ("READY" === status) {
                trClass = "info";
            } else if ("RUNNING" === status) {
                trClass = "success";
            } else if ("DISABLED" === status || "STOPED" === status) {
                trClass = "warning";
            } else if ("CRASHED" === status || "SHUTDOWN" === status) {
                trClass = "danger";
            }
            $("#jobs tbody").append("<tr class='" + trClass + "'>" + baseTd + operationTd + "</tr>");
        }
    });
}

function bindStopButtons() {
    $(document).on("click", "button[operation='stop'][data-toggle!='modal']", function(event) {
        $.post("job/stop", {jobName : $(event.currentTarget).attr("job-name"), ip : $("#server-ip").text()}, function (data) {
            renderJobs();
            showSuccessDialog();
        });
    });
    $(document).on("click", "button[operation='stop'][data-toggle='modal']", function(event) {
        $("#chosen-job-name").text($(event.currentTarget).attr("job-name"));
    });
}

function bindStopLeaderButtons() {
    $(document).on("click", "#stop-leader-confirm-dialog-confirm-btn,#stop-all-jobs-btn", function(event) {
        $.post("job/stopAll/name", {jobName : $("#chosen-job-name").text()}, function (data) {
            $("#stop-leader-confirm-dialog").modal("hide");
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

function bindStopAllButton() {
    $(document).on("click", "#stop-all-jobs-btn", function(event) {
        $.post("job/stopAll/ip", {ip : $("#server-ip").text()}, function (data) {
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
