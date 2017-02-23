$(function() {
    $("#job-name").text(GetQueryParam("jobName"));
    renderSettings();
    bindSubmitJobSettingsForm();
    $('[href="#settings"]').click(function() {
        renderSettings();
    });
    $('[href="#servers"]').click(function() {
        renderServers();
    });
    $('[href="#execution_info"]').click(function() {
        renderExecution();
    });
    bindTriggerButtons();
    bindTriggerAllButtons();
    bindPauseButtons();
    bindPauseAllButtons();
    bindResumeButtons();
    bindResumeAllButton();
    bindShutdownButtons();
    bindRemoveButtons();
    bindDisableButtons();
    bindEnableButtons();
});

function renderSettings() {
    $.get("job/settings", {jobName : $("#job-name").text()}, function (data) {
        $("#jobType").attr("value", data.jobType);
        $("#jobClass").attr("value", data.jobClass);
        $("#shardingTotalCount").attr("value", data.shardingTotalCount);
        $("#cron").attr("value", data.cron);
        $("#shardingItemParameters").text(data.shardingItemParameters);
        $("#jobParameter").attr("value", data.jobParameter);
        $("#monitorExecution").attr("checked", data.monitorExecution);
        $("#failover").attr("checked", data.failover);
        $("#misfire").attr("checked", data.misfire);
        $("#streamingProcess").attr("checked", data.streamingProcess);
        $("#maxTimeDiffSeconds").attr("value", data.maxTimeDiffSeconds);
        $("#monitorPort").attr("value", data.monitorPort);
        $("#jobShardingStrategyClass").attr("value", data.jobShardingStrategyClass);
        $("#executorServiceHandler").attr("value", data.jobProperties["executor_service_handler"]);
        $("#jobExceptionHandler").attr("value", data.jobProperties["job_exception_handler"]);
        $("#reconcileCycleTime").attr("value", data.reconcileCycleTime);
        $("#description").text(data.description);
        if (!data.monitorExecution) {
            $("#execution_info_tab").addClass("disabled");
        }
        $("#scriptCommandLine").attr("value", data.scriptCommandLine);
        if($("#jobType").val() == "DATAFLOW"){
            $('#streamingProcessGroup').show();
        }
        if($("#jobType").val() == "SCRIPT"){
            $('#scriptCommandLineGroup').show();
        }
    });
}

function bindSubmitJobSettingsForm() {
    $("#job-settings-form").submit(function(event) {
        event.preventDefault();
        var jobName = $("#job-name").text();
        var jobType = $("#jobType").val();
        var jobClass = $("#jobClass").val();
        var shardingTotalCount = $("#shardingTotalCount").val();
        var jobParameter = $("#jobParameter").val();
        var cron = $("#cron").val();
        var streamingProcess = $("#streamingProcess").prop("checked");
        var maxTimeDiffSeconds = $("#maxTimeDiffSeconds").val();
        var monitorPort = $("#monitorPort").val();
        var monitorExecution = $("#monitorExecution").prop("checked");
        var failover = $("#failover").prop("checked");
        var misfire = $("#misfire").prop("checked");
        var driver = $("#driver").val();
        var url = $("#url").val();
        var username = $("#username").val();
        var password = $("#password").val();
        var logLevel = $("#logLevel").val();
        var shardingItemParameters = $("#shardingItemParameters").val();
        var jobShardingStrategyClass = $("#jobShardingStrategyClass").val();
        var scriptCommandLine = $("#scriptCommandLine").val();
        var executorServiceHandler = $("#executorServiceHandler").val();
        var jobExceptionHandler = $("#jobExceptionHandler").val();
        var description = $("#description").val();
        var reconcileCycleTime = $("#reconcileCycleTime").val();
        var postJson = {jobName: jobName, jobType : jobType, jobClass : jobClass, shardingTotalCount: shardingTotalCount, jobParameter: jobParameter, cron: cron, streamingProcess: streamingProcess, maxTimeDiffSeconds: maxTimeDiffSeconds, monitorPort: monitorPort, monitorExecution: monitorExecution, failover: failover, misfire: misfire, shardingItemParameters: shardingItemParameters, jobShardingStrategyClass: jobShardingStrategyClass, jobProperties: {"executor_service_handler": executorServiceHandler, "job_exception_handler": jobExceptionHandler}, description: description, scriptCommandLine: scriptCommandLine, reconcileCycleTime:reconcileCycleTime};
        $.post("job/settings", postJson, function() {
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
    var jobName = $("#job-name").text();
    $('#jobServers').bootstrapTable({
        url: 'job/servers?jobName=' + jobName,
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
            field: 'ip',
            title: 'IP地址'
        }, {
            field: 'hostName',
            title: '机器名'
        }, {
            field: 'status',
            title: '状态'
        },{
            field: 'sharding',
            title: '分片项'
        },{
            field: 'oper',
            title: '操作',
            formatter: 'operFormatter'
        }]
    });
}

function operFormatter(val, row){
    var operationTd = "";
    var triggerButton = "<button operation='trigger' class='btn btn-success' ip='" + row.ip + "'>触发</button>";
    var resumeButton = "<button operation='resume' class='btn btn-success' ip='" + row.ip + "'>恢复</button>";
    var pauseButton = "<button operation='pause' class='btn btn-warning' ip='" + row.ip + "'" + ">暂停</button>";
    var shutdownButton = "<button operation='shutdown' class='btn btn-danger' ip='" + row.ip + "'>关闭</button>";
    var removeButton = "<button operation='remove' class='btn btn-danger' ip='" + row.ip + "'>删除</button>";
    var disableButton = "<button operation='disable' class='btn btn-danger' ip='" + row.ip + "'>失效</button>";
    var enableButton = "<button operation='enable' class='btn btn-success' ip='" + row.ip + "'>生效</button>";
    operationTd = triggerButton + "&nbsp;";
    if ("PAUSED" === row.status) {
        operationTd = operationTd + resumeButton + "&nbsp;";
    } else if ("DISABLED" !== row.status && "CRASHED" !== row.status && "SHUTDOWN" !== row.status) {
        operationTd = operationTd + pauseButton + "&nbsp;";
    }
    if ("SHUTDOWN" !== row.status) {
        operationTd = operationTd + shutdownButton + "&nbsp;";
    }
    if ("SHUTDOWN" === row.status || "CRASHED" === row.status) {
        operationTd = removeButton + "&nbsp;";
    }
    if("DISABLED" == row.status) {
        operationTd = operationTd + enableButton;
    } else if ("CRASHED" !== row.status && "SHUTDOWN" !== row.status){
        operationTd = operationTd + disableButton;
    }
    return operationTd;
}

function bindTriggerButtons() {
    $(document).on("click", "button[operation='trigger'][data-toggle!='modal']", function(event) {
        var jobName = $("#job-name").text();
        $.post("job/trigger", {jobName : jobName, ip : $(event.currentTarget).attr("ip")}, function () {
            $("#jobServers").bootstrapTable('refresh');
            showSuccessDialog();
        });
    });
}

function bindTriggerAllButtons() {
    $(document).on("click", "#trigger-all-jobs-btn", function() {
        var jobName = $("#job-name").text();
        $.post("job/triggerAll/name", {jobName : jobName}, function () {
            $("#jobServers").bootstrapTable('refresh');
            showSuccessDialog();
        });
    });
}

function bindPauseButtons() {
    $(document).on("click", "button[operation='pause'][data-toggle!='modal']", function(event) {
        var jobName = $("#job-name").text();
        $.post("job/pause", {jobName : jobName, ip : $(event.currentTarget).attr("ip")}, function () {
            $("#jobServers").bootstrapTable('refresh');
            showSuccessDialog();
        });
    });
}

function bindPauseAllButtons() {
    $(document).on("click", "#pause-all-jobs-btn", function() {
        var jobName = $("#job-name").text();
        $.post("job/pauseAll/name", {jobName : jobName}, function () {
            $("#jobServers").bootstrapTable('refresh');
            showSuccessDialog();
        });
    });
}

function bindResumeButtons() {
    $(document).on("click", "button[operation='resume']", function(event) {
        var jobName = $("#job-name").text();
        $.post("job/resume", {jobName : jobName, ip : $(event.currentTarget).attr("ip")}, function () {
            $("#jobServers").bootstrapTable('refresh');
            showSuccessDialog();
        });
    });
}

function bindResumeAllButton() {
    $(document).on("click", "#resume-all-jobs-btn", function() {
        var jobName = $("#job-name").text();
        $.post("job/resumeAll/name", {jobName : jobName}, function () {
            $("#jobServers").bootstrapTable('refresh');
            showSuccessDialog();
        });
    });
}

function renderExecution() {
    var jobName = $("#job-name").text();
    $('#execution').bootstrapTable({
        url: 'job/execution?jobName=' + jobName,
        method: 'get',
        cache: false,
        rowStyle: function (row, index) {
            var strclass = "";
            if ("RUNNING" === row.status) {
                strclass = "success";
            } else if ("COMPLETED" === row.status) {
                strclass = "info";
            } else if ("PENDING" === row.status) {
                strclass = "warning";
            } else {
                return {};
            }
            return { classes: strclass }
        },
        columns: [
        {
            field: 'item',
            title: '分片项'
        }, {
            field: 'status',
            title: '状态'
        }, {
            field: 'failoverIp',
            title: '失效转移执行'
        },{
            field: 'lastBeginTime',
            title: '上次作业开始时间',
            formatter: 'dateFormatter'
        },{
            field: 'lastCompleteTime',
            title: '上次作业完成时间',
            formatter: 'dateFormatter'
        },{
            field: 'nextFireTime',
            title: '下次作业运行时间',
            formatter: 'dateFormatter'
        }]
    });
}
function dateFormatter(val, row){
    return null == row.nextFireTime ? null : new Date(row.nextFireTime).toLocaleString();
}

function ipFormatter(val, row){
    return null == data[i].failoverIp ? "-" : data[i].failoverIp;
}

function bindShutdownButtons() {
    $(document).on("click", "button[operation='shutdown']", function(event) {
        var jobName = $("#job-name").text();
        $.post("job/shutdown", {jobName : jobName, ip : $(event.currentTarget).attr("ip")}, function () {
            $("#jobServers").bootstrapTable('refresh');
            showSuccessDialog();
        });
    });
}

function bindRemoveButtons() {
    $(document).on("click", "button[operation='remove']", function(event) {
        var jobName = $("#job-name").text();
        $.post("job/remove", {jobName : jobName, ip : $(event.currentTarget).attr("ip")}, function (data) {
            if (data.length > 0) {
                showFailureDialog("remove-job-failure-dialog");
            } else {
                showSuccessDialog();
            }
            $("#jobServers").bootstrapTable('refresh');
        });
    });
}

function bindDisableButtons() {
    $(document).on("click", "button[operation='disable']", function(event) {
        var jobName = $("#job-name").text();
        $.post("job/disable", {jobName : jobName, ip : $(event.currentTarget).attr("ip")}, function () {
            $("#jobServers").bootstrapTable('refresh');
            showSuccessDialog();
        });
    });
}

function bindEnableButtons() {
    $(document).on("click", "button[operation='enable']", function(event) {
        var jobName = $("#job-name").text();
        $.post("job/enable", {jobName : jobName, ip : $(event.currentTarget).attr("ip")}, function () {
            $("#jobServers").bootstrapTable('refresh');
            showSuccessDialog();
        });
    });
}