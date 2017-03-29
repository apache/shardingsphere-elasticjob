$(function() {
    $("#job-name").text(getCurrentUrl("job-name"));
    renderServers();
    renderExecution();
    bindButtons();
    renderBreadCrumbMenu();
});

function renderServers() {
    var jobName = $("#job-name").text();
    $("#job-servers").bootstrapTable({
        url: "/api/jobs/" + jobName + "/servers",
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
    var disableButton = "<button operation='disable' class='btn-xs btn-warning' ip='" + row.ip + "' instance-id='" + row.instanceId + "'>禁用</button>";
    var enableButton = "<button operation='enable' class='btn-xs btn-success' ip='" + row.ip + "' instance-id='" + row.instanceId + "'>启用</button>";
    var shutdownButton = "<button operation='shutdown' class='btn-xs btn-danger' ip='" + row.ip + "' instance-id='" + row.instanceId + "'>关闭</button>";
    var operationTd = triggerButton + "&nbsp;";
    if ("DISABLED" === row.status) {
        operationTd = operationTd + enableButton;
    } else if ("CRASHED" !== row.status && "SHUTDOWN" !== row.status) {
        operationTd = operationTd + disableButton;
    }
    if ("SHUTDOWN" !== row.status) {
        operationTd = operationTd + shutdownButton + "&nbsp;";
    }
    return operationTd;
}

function bindButtons() {
    bindTriggerButton();
    bindShutdownButton();
    bindDisableButton();
    bindEnableButton();
}

function bindTriggerButton() {
    $(document).on("click", "button[operation='trigger'][data-toggle!='modal']", function(event) {
        var jobName = $("#job-name").text();
        $.ajax({
            url: "/api/jobs/" + jobName + "/trigger",
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

function bindShutdownButton() {
    $(document).on("click", "button[operation='shutdown']", function(event) {
        var jobName = $("#job-name").text();
        $.ajax({
            url: "/api/jobs/" + jobName + "/shutdown",
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
        url: "/api/jobs/" + jobName + "/execution",
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

function renderBreadCrumbMenu() {
    $("#breadcrumb-job").click(function() {
        $("#content").load("html/status/job/jobs_status_overview.html");
    });
}
