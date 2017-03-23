$(function() {
    $("#job-name").text(getCurrentUrl("jobName"));
    renderServers();
    $('[href="#servers"]').click(function() {
        renderServers();
    });
    $('[href="#execution-info"]').click(function() {
        renderExecution();
    });
    bindTriggerButtons();
    bindPauseButtons();
    bindResumeButtons();
    bindShutdownButtons();
    bindRemoveButtons();
    bindDisableButtons();
    bindEnableButtons();
});

function renderServers() {
    var jobName = $("#job-name").text();
    $("#job-servers").bootstrapTable({
        url: "/api/job/servers/" + jobName,
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
        columns: [
        {
            field: "ip",
            title: "IP地址"
        }, {
            field: "status",
            title: "状态"
        }, {
            field: "sharding",
            title: "分片项"
        }, {
            field: "operation",
            title: "操作",
            formatter: "generateOperationButtons"
        }]
    });
}

function generateOperationButtons(val, row) {
    var triggerButton = "<button operation='trigger' class='btn-xs btn-success' ip='" + row.ip + "'>触发</button>";
    var resumeButton = "<button operation='resume' class='btn-xs btn-success' ip='" + row.ip + "'>恢复</button>";
    var pauseButton = "<button operation='pause' class='btn-xs btn-warning' ip='" + row.ip + "'" + ">暂停</button>";
    var shutdownButton = "<button operation='shutdown' class='btn-xs btn-danger' ip='" + row.ip + "'>关闭</button>";
    var removeButton = "<button operation='remove' class='btn-xs btn-danger' ip='" + row.ip + "'>删除</button>";
    var disableButton = "<button operation='disable' class='btn-xs btn-danger' ip='" + row.ip + "'>失效</button>";
    var enableButton = "<button operation='enable' class='btn-xs btn-success' ip='" + row.ip + "'>生效</button>";
    var operationTd = triggerButton + "&nbsp;";
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
    } else if ("CRASHED" !== row.status && "SHUTDOWN" !== row.status){
        operationTd = operationTd + disableButton;
    }
    return operationTd;
}

function bindTriggerButtons() {
    $(document).on("click", "button[operation='trigger'][data-toggle!='modal']", function(event) {
        var jobName = $("#job-name").text();
        $.ajax({
            url: "/api/job/trigger",
            type: "POST",
            data: JSON.stringify({jobName : jobName, ip : $(event.currentTarget).attr("ip")}),
            contentType: "application/json",
            dataType: "json",
            success: function() {
                $("#job-servers").bootstrapTable("refresh");
                showSuccessDialog();
            }
        });
    });
}

function bindPauseButtons() {
    $(document).on("click", "button[operation='pause'][data-toggle!='modal']", function(event) {
        var jobName = $("#job-name").text();
        $.ajax({
            url: "/api/job/pause",
            type: "POST",
            data: JSON.stringify({jobName : jobName, ip : $(event.currentTarget).attr("ip")}),
            contentType: "application/json",
            dataType: "json",
            success: function() {
                $("#job-servers").bootstrapTable("refresh");
                showSuccessDialog();
            }
        });
    });
}

function bindResumeButtons() {
    $(document).on("click", "button[operation='resume']", function(event) {
        var jobName = $("#job-name").text();
        $.ajax({
            url: "/api/job/resume",
            type: "POST",
            data: JSON.stringify({jobName : jobName, ip : $(event.currentTarget).attr("ip")}),
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
        url: "/api/job/execution/" + jobName,
        method: "get",
        cache: false,
        rowStyle: function (row, index) {
            var statusClass = "";
            if ("RUNNING" === row.status) {
                statusClass = "success";
            } else if ("COMPLETED" === row.status) {
                statusClass = "info";
            } else if ("PENDING" === row.status) {
                statusClass = "warning";
            } else {
                return {};
            }
            return { classes : statusClass }
        },
        columns: [
        {
            field: "item",
            title: "分片项"
        }, {
            field: "status",
            title: "状态"
        }, {
            field: "failoverIp",
            title: "失效转移执行"
        }]
    });
}

function bindShutdownButtons() {
    $(document).on("click", "button[operation='shutdown']", function(event) {
        var jobName = $("#job-name").text();
        $.ajax({
            url: "/api/job/shutdown",
            type: "POST",
            data: JSON.stringify({jobName : jobName, ip : $(event.currentTarget).attr("ip")}),
            contentType: "application/json",
            dataType: "json",
            success: function() {
                $("#job-servers").bootstrapTable("refresh");
                showSuccessDialog();
            }
        });
    });
}

function bindRemoveButtons() {
    $(document).on("click", "button[operation='remove']", function(event) {
        var jobName = $("#job-name").text();
        $.ajax({
            url: "/api/job/remove",
            type: "POST",
            data: JSON.stringify({jobName : jobName, ip : $(event.currentTarget).attr("ip")}),
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

function bindDisableButtons() {
    $(document).on("click", "button[operation='disable']", function(event) {
        var jobName = $("#job-name").text();
        $.ajax({
            url: "/api/job/disable",
            type: "POST",
            data: JSON.stringify({jobName : jobName, ip : $(event.currentTarget).attr("ip")}),
            contentType: "application/json",
            dataType: "json",
            success: function() {
                $("#job-servers").bootstrapTable("refresh");
                showSuccessDialog();
            }
        });
    });
}

function bindEnableButtons() {
    $(document).on("click", "button[operation='enable']", function(event) {
        var jobName = $("#job-name").text();
        $.ajax({
            url: "/api/job/enable",
            type: "POST",
            data: JSON.stringify({jobName : jobName, ip : $(event.currentTarget).attr("ip")}),
            contentType: "application/json",
            dataType: "json",
            success: function() {
                $("#job-servers").bootstrapTable("refresh");
                showSuccessDialog();
            }
        });
    });
}
