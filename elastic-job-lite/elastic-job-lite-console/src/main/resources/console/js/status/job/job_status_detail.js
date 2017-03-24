$(function() {
    $("#job-name").text(getCurrentUrl("job-name"));
    renderServers();
    bindButtons();
});

function renderServers() {
    var jobName = $("#job-name").text();
    $("#job-servers").bootstrapTable({
        url: "/api/jobs/servers/" + jobName,
        cache: false,
        columns: [
        {
            field: "ip",
            title: "IP地址",
            sortable: "true"
        }, {
            field: "instanceId",
            title: "实例ID", 
            sortable: "true"
        }, {
            field: "sharding",
            title: "分片项"
        }, {
            field: "status",
            title: "状态", 
            sortable: "true",
            formatter: "statusFormatter"
        },  {
            field: "operation",
            title: "操作",
            formatter: "generateOperationButtons"
        }]
    });
}

function statusFormatter(value, row) {
    switch(value) {
        case "RUNNING":
            return "<span class='label label-primary'>运行中</span>";
            break;
        case "READY":
            return "<span class='label label-info'>准备中</span>";
            break;
        case "PAUSED":
            return "<span class='label label-warning'>暂停中</span>";
            break;
        case "DISABLED":
            return "<span class='label label-warning'>禁用中</span>";
            break;
        case "CRASHED":
            return "<span class='label label-danger'>宕机</span>";
            break;
        case "SHUTDOWN":
            return "<span class='label label-danger'>停止</span>";
            break;
    }
}

function generateOperationButtons(val, row) {
    var triggerButton = "<button operation='trigger' class='btn-xs btn-success' ip='" + row.ip + "' instance-id='" + row.instanceId + "'>触发</button>";
    var executionButton = "<button operation='execution' class='btn-xs btn-info' ip='" + row.ip + "' instance-id='" + row.instanceId + "'>详情</button>";
    var resumeButton = "<button operation='resume' class='btn-xs btn-success' ip='" + row.ip + "' instance-id='" + row.instanceId + "'>恢复</button>";
    var pauseButton = "<button operation='pause' class='btn-xs btn-warning' ip='" + row.ip + "' instance-id='" + row.instanceId + "'>暂停</button>";
    var shutdownButton = "<button operation='shutdown' class='btn-xs btn-danger' ip='" + row.ip + "' instance-id='" + row.instanceId + "'>关闭</button>";
    var removeButton = "<button operation='remove' class='btn-xs btn-danger' ip='" + row.ip + "' instance-id='" + row.instanceId + "'>删除</button>";
    var disableButton = "<button operation='disable' class='btn-xs btn-warning' ip='" + row.ip + "' instance-id='" + row.instanceId + "'>禁用</button>";
    var enableButton = "<button operation='enable' class='btn-xs btn-success' ip='" + row.ip + "' instance-id='" + row.instanceId + "'>启用</button>";
    var operationTd = triggerButton + "&nbsp;" + executionButton + "&nbsp;";
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
    if ("DISABLED" === row.status) {
        operationTd = operationTd + enableButton;
    } else if ("CRASHED" !== row.status && "SHUTDOWN" !== row.status) {
        operationTd = operationTd + disableButton;
    }
    return operationTd;
}

function bindButtons() {
    bindExecutionButton();
    bindTriggerButton();
    bindPauseButton();
    bindResumeButton();
    bindShutdownButton();
    bindRemoveButton();
    bindDisableButton();
    bindEnableButton();
}

function bindExecutionButton() {
    $(document).on("click", "button[operation='execution'][data-toggle!='modal']", function(event) {
        $('#execution-info').modal({backdrop : 'static', keyboard : true});
        renderExecution();
    });
}


function bindTriggerButton() {
    $(document).on("click", "button[operation='trigger'][data-toggle!='modal']", function(event) {
        var jobName = $("#job-name").text();
        $.ajax({
            url: "/api/jobs/trigger",
            type: "POST",
            data: JSON.stringify({jobName : jobName, ip : $(event.currentTarget).attr("ip"), instanceId : $(event.currentTarget).attr("instance-id")}),
            contentType: "application/json",
            dataType: "json",
            success: function() {
                $("#job-servers").bootstrapTable("refresh");
                showSuccessDialog();
            }
        });
    });
}

function bindPauseButton() {
    $(document).on("click", "button[operation='pause'][data-toggle!='modal']", function(event) {
        var jobName = $("#job-name").text();
        $.ajax({
            url: "/api/jobs/pause",
            type: "POST",
            data: JSON.stringify({jobName : jobName, ip : $(event.currentTarget).attr("ip"), instanceId : $(event.currentTarget).attr("instance-id")}),
            contentType: "application/json",
            dataType: "json",
            success: function() {
                $("#job-servers").bootstrapTable("refresh");
                showSuccessDialog();
            }
        });
    });
}

function bindResumeButton() {
    $(document).on("click", "button[operation='resume']", function(event) {
        var jobName = $("#job-name").text();
        $.ajax({
            url: "/api/jobs/resume",
            type: "POST",
            data: JSON.stringify({jobName : jobName, ip : $(event.currentTarget).attr("ip"), instanceId : $(event.currentTarget).attr("instance-id")}),
            contentType: "application/json",
            dataType: "json",
            success: function() {
                $("#job-servers").bootstrapTable("refresh");
                showSuccessDialog();
            }
        });
    });
}

function bindShutdownButton() {
    $(document).on("click", "button[operation='shutdown']", function(event) {
        var jobName = $("#job-name").text();
        $.ajax({
            url: "/api/jobs/shutdown",
            type: "POST",
            data: JSON.stringify({jobName : jobName, ip : $(event.currentTarget).attr("ip"), instanceId : $(event.currentTarget).attr("instance-id")}),
            contentType: "application/json",
            dataType: "json",
            success: function() {
                $("#job-servers").bootstrapTable("refresh");
                showSuccessDialog();
            }
        });
    });
}

function bindRemoveButton() {
    $(document).on("click", "button[operation='remove']", function(event) {
        var jobName = $("#job-name").text();
        $.ajax({
            url: "/api/jobs/remove",
            type: "POST",
            data: JSON.stringify({jobName : jobName, ip : $(event.currentTarget).attr("ip"), instanceId : $(event.currentTarget).attr("instance-id")}),
            contentType: "application/json",
            dataType: "json",
            success: function(data) {
                if (data.length > 0) {
                    showFailureDialog("remove-job-failure-dialog");
                } else {
                    showSuccessDialog();
                }
                $("#job-servers").bootstrapTable("refresh");
            }
        });
    });
}

function bindDisableButton() {
    $(document).on("click", "button[operation='disable']", function(event) {
        var jobName = $("#job-name").text();
        $.ajax({
            url: "/api/jobs/disable",
            type: "POST",
            data: JSON.stringify({jobName : jobName, ip : $(event.currentTarget).attr("ip"), instanceId : $(event.currentTarget).attr("instance-id")}),
            contentType: "application/json",
            dataType: "json",
            success: function() {
                $("#job-servers").bootstrapTable("refresh");
                showSuccessDialog();
            }
        });
    });
}

function bindEnableButton() {
    $(document).on("click", "button[operation='enable']", function(event) {
        var jobName = $("#job-name").text();
        $.ajax({
            url: "/api/jobs/enable",
            type: "POST",
            data: JSON.stringify({jobName : jobName, ip : $(event.currentTarget).attr("ip"), instanceId : $(event.currentTarget).attr("instance-id")}),
            contentType: "application/json",
            dataType: "json",
            success: function() {
                $("#job-servers").bootstrapTable("refresh");
                showSuccessDialog();
            }
        });
    });
}

function renderExecution() {
    var jobName = $("#job-name").text();
    $("#execution").bootstrapTable({
        url: "/api/jobs/execution/" + jobName,
        cache: false,
        columns: [
            {
                field: "item",
                title: "分片项"
            }, {
                field: "status",
                title: "状态",
                formatter: "executionStatusFormatter"
            }, {
                field: "failoverIp",
                title: "失效转移执行"
            }]
    });
}

function executionStatusFormatter(value, row) {
    switch(value) {
        case "RUNNING":
            return "<span class='label label-primary'>运行中</span>";
            break;
        case "COMPLETED":
            return "<span class='label label-success'>已完成</span>";
            break;
        case "PENDING":
            return "<span class='label label-warning'>待运行</span>";
            break;
    }
}