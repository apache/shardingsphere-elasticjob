$(function() {
    renderSettings();
    bindSubmitJobSettingsForm();
    $('[href="#settings"]').click(function(event) {
        renderSettings();
    });
    $('[href="#servers"]').click(function(event) {
        renderServers();
    });
    $('[href="#execution_info"]').click(function(event) {
        renderExecution();
    });
    bindStopButtons();
    bindStopAllButtons();
    bindResumeButtons();
    bindResumeAllButton();
    bindShutdownButtons();
});

function renderSettings() {
    $.get("job/settings", {jobName : $("#job-name").text()}, function (data) {
        $("#jobClass").attr("value", data.jobClass);
        $("#shardingTotalCount").attr("value", data.shardingTotalCount);
        $("#cron").attr("value", data.cron);
        $("#shardingItemParameters").text(data.shardingItemParameters);
        $("#jobParameter").attr("value", data.jobParameter);
        $("#monitorExecution").attr("checked", data.monitorExecution);
        $("#failover").attr("checked", data.failover);
        $("#misfire").attr("checked", data.misfire);
        $("#processCountIntervalSeconds").attr("value", data.processCountIntervalSeconds);
        $("#concurrentDataProcessThreadCount").attr("value", data.concurrentDataProcessThreadCount);
        $("#fetchDataCount").attr("value", data.fetchDataCount);
        $("#maxTimeDiffSeconds").attr("value", data.maxTimeDiffSeconds);
        $("#monitorPort").attr("value", data.monitorPort);
        $("#jobShardingStrategyClass").attr("value", data.jobShardingStrategyClass);
        $("#description").text(data.description);
        if (!data.monitorExecution) {
            $("#execution_info_tab").addClass("disabled");
        }
    });
}

function bindSubmitJobSettingsForm() {
    $("#job-settings-form").submit(function(event) {
        event.preventDefault();
        var jobName = $("#job-name").text();
        var jobClass = $("#jobClass").val();
        var shardingTotalCount = $("#shardingTotalCount").val();
        var jobParameter = $("#jobParameter").val();
        var cron = $("#cron").val();
        var concurrentDataProcessThreadCount = $("#concurrentDataProcessThreadCount").val();
        var processCountIntervalSeconds = $("#processCountIntervalSeconds").val();
        var fetchDataCount = $("#fetchDataCount").val();
        var maxTimeDiffSeconds = $("#maxTimeDiffSeconds").val();
        var monitorPort = $("#monitorPort").val();
        var monitorExecution = $("#monitorExecution").prop("checked");
        var failover = $("#failover").prop("checked");
        var misfire = $("#misfire").prop("checked");
        var shardingItemParameters = $("#shardingItemParameters").val();
        var jobShardingStrategyClass = $("#jobShardingStrategyClass").val();
        var description = $("#description").val();
        $.post("job/settings", {jobName: jobName, jobClass : jobClass, shardingTotalCount: shardingTotalCount, jobParameter: jobParameter, cron: cron, concurrentDataProcessThreadCount: concurrentDataProcessThreadCount, processCountIntervalSeconds: processCountIntervalSeconds, fetchDataCount: fetchDataCount, maxTimeDiffSeconds: maxTimeDiffSeconds, monitorPort: monitorPort, monitorExecution: monitorExecution, failover: failover, misfire: misfire, shardingItemParameters: shardingItemParameters, jobShardingStrategyClass: jobShardingStrategyClass, description: description}, function(data) {
            showSuccessDialog();
            if (monitorExecution) {
                $("#execution_info_tab").removeClass("disabled");
            } else {
                $("#execution_info_tab").addClass("disabled");
            }
        });
    });
}

function renderServers() {
    $.get("job/servers", {jobName : $("#job-name").text()}, function (data) {
        $("#servers tbody").empty();
        var leaderStatus;
        for (var i = 0;i < data.length;i++) {
            var status = data[i].status;
            var leader = data[i].leader;
            if (leader) {
                leaderStatus = status;
            }
            var baseTd = "<td>" + data[i].ip + "</td><td>" + data[i].hostName + "</td><td>" + status + "</td><td>" + data[i].processSuccessCount + "</td><td>" + data[i].processFailureCount + "</td><td>" + data[i].sharding + "</td><td>" + (true === leader ? "<span class='glyphicon glyphicon-ok'></span>" : "<span class='glyphicon glyphicon-remove'></span>") + "</td>";
            var operationTd;
            if ("SHUTDOWN" === status) {
                operationTd = "-";
            } else if ("STOPED" === status) {
                operationTd = "<button operation='resume' class='btn btn-success' ip='" + data[i].ip + "' leader='" + leader + "'>恢复</button>";
            } else if ("DISABLED" !== status && "CRASHED" !== status) {
                operationTd = "<button operation='stop' class='btn btn-warning' ip='" + data[i].ip + "'" + (leader ? "data-toggle='modal' data-target='#stop-leader-confirm-dialog'" : "") + ">暂停</button>";
            } else {
                operationTd = "-";
            }
            if ("-" !== operationTd) {
                operationTd = operationTd + "&nbsp;<button operation='shutdown' class='btn btn-danger' ip='" + data[i].ip + "'>关闭</button>";
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
            $("#servers tbody").append("<tr class='" + trClass + "'>" + baseTd + operationTd + "</tr>");
        }
        if ("STOPED" === leaderStatus) {
            $("button[operation='resume'][leader='false']").attr("disabled", true);
            $("button[operation='resume'][leader='false']").addClass("disabled");
            $("button[operation='resume'][leader='false']").attr("title", "先恢复主节点才能恢复从节点作业");
        }
    });
}

function bindStopButtons() {
    $(document).on("click", "button[operation='stop'][data-toggle!='modal']", function(event) {
        var jobName = $("#job-name").text();
        $.post("job/stop", {jobName : jobName, ip : $(event.currentTarget).attr("ip")}, function (data) {
            renderServers();
            showSuccessDialog();
        });
    });
}

function bindStopAllButtons() {
    $(document).on("click", "#stop-leader-confirm-dialog-confirm-btn,#stop-all-jobs-btn", function(event) {
        var jobName = $("#job-name").text();
        $.post("job/stopAll/name", {jobName : jobName}, function (data) {
            $("#stop-leader-confirm-dialog").modal("hide");
            renderServers();
            showSuccessDialog();
        });
    });
}

function bindResumeButtons() {
    $(document).on("click", "button[operation='resume']", function(event) {
        var jobName = $("#job-name").text();
        $.post("job/resume", {jobName : jobName, ip : $(event.currentTarget).attr("ip")}, function (data) {
            renderServers();
            showSuccessDialog();
        });
    });
}

function bindResumeAllButton() {
    $(document).on("click", "#resume-all-jobs-btn", function(event) {
        var jobName = $("#job-name").text();
        $.post("job/resumeAll/name", {jobName : jobName}, function (data) {
            renderServers();
            showSuccessDialog();
        });
    });
}

function renderExecution() {
    $.get("job/execution", {jobName : $("#job-name").text()}, function (data) {
        $("#execution tbody").empty();
        for (var i = 0;i < data.length;i++) {
            var status = data[i].status;
            var failoverIp = null == data[i].failoverIp ? "-" : data[i].failoverIp;
            var lastBeginTime = null == data[i].lastBeginTime ? null : new Date(data[i].lastBeginTime).toLocaleString();
            var lastCompleteTime = null == data[i].lastCompleteTime ? null : new Date(data[i].lastCompleteTime).toLocaleString();
            var nextFireTime = null == data[i].nextFireTime ? null : new Date(data[i].nextFireTime).toLocaleString();
            var baseTd = "<td>" + data[i].item + "</td><td>" + status + "</td><td>" + failoverIp + "</td><td>" + lastBeginTime + "</td><td>" + lastCompleteTime + "</td><td>" + nextFireTime + "</td>";
            var trClass = "";
            if ("RUNNING" === status) {
                trClass = "success";
            } else if ("COMPLETED" === status) {
                trClass = "info";
            } else if ("PENDING" === status) {
                trClass = "warning";
            }
            $("#execution tbody").append("<tr class='" + trClass + "'>" + baseTd + "</tr>");
        }
    });
}

function bindShutdownButtons() {
    $(document).on("click", "button[operation='shutdown']", function(event) {
        var jobName = $("#job-name").text();
        $.post("job/shutdown", {jobName : jobName, ip : $(event.currentTarget).attr("ip")}, function (data) {
            renderServers();
            showSuccessDialog();
        });
    });
}