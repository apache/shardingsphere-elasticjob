$(function() {
    $("#job-name").text($("#index-job-name").text());
    authorityControl();
    renderShardingTable();
    renderBreadCrumbMenu();
    bindButtons();
});

function renderShardingTable() {
    var jobName = $("#job-name").text();
    $("#sharding").bootstrapTable({
        url: "/api/jobs/" + jobName + "/sharding",
        cache: false,
        columns: [
            {
                field: "item",
                title: "分片项",
                sortable: "true"
            }, {
                field: "serverIp",
                title: "服务器IP",
                sortable: "true"
            }, {
                field: "instanceId",
                title: "进程ID",
                sortable: "true"
            }, {
                field: "status",
                title: "状态",
                formatter: "shardingStatusFormatter"
            }, {
                field: "failover",
                title: "失效转移",
                formatter: "failoverFormatter"
            }, {
                field: "operation",
                title: "操作",
                formatter: "generateOperationButtons"
            }]
    });
}

function shardingStatusFormatter(value, row) {
    switch(value) {
        case "DISABLED":
            return "<span class='label label-warning'>禁用中</span>";
            break;
        case "RUNNING":
            return "<span class='label label-primary'>运行中</span>";
            break;
        case "SHARDING_ERROR":
            return "<span class='label label-info'>分片调整中</span>";
            break;
        default:
            return "<span class='label label-default'>等待运行</span>";
            break;
    }
}

function failoverFormatter(value, row) {
    return value ? "是" : "-";
}

function generateOperationButtons(val, row) {
    var disableButton = "<button operation='disable-sharding' class='btn-xs btn-warning' job-name='" + row.jobName + "' item='" + row.item + "' >禁用</button>";
    var enableButton = "<button operation='enable-sharding' class='btn-xs btn-success' job-name='" + row.jobName + "' item='" + row.item + "' >启用</button>";
    if ("DISABLED" === row.status) {
        return enableButton;
    } else {
        return disableButton;
    }
}

function bindButtons() {
    bindDisableButton();
    bindEnableButton();
}

function bindDisableButton() {
    $(document).off("click", "button[operation='disable-sharding']");
    $(document).on("click", "button[operation='disable-sharding']", function(event) {
        var jobName = $("#index-job-name").text();
        var item = $(event.currentTarget).attr("item");
        $.ajax({
            url: "/api/jobs/" + jobName + "/sharding/" + item + "/disable",
            type: "POST",
            success: function() {
                showSuccessDialog();
                $("#sharding").bootstrapTable("refresh");
            }
        });
    });
}

function bindEnableButton() {
    $(document).off("click", "button[operation='enable-sharding']");
    $(document).on("click", "button[operation='enable-sharding']", function(event) {
        var jobName = $("#index-job-name").text();
        var item = $(event.currentTarget).attr("item");
        $.ajax({
            url: "/api/jobs/" + jobName + "/sharding/" + item + "/disable",
            type: "DELETE",
            success: function () {
                showSuccessDialog();
                $("#sharding").bootstrapTable("refresh");
            }
        });
    });
}

function renderBreadCrumbMenu() {
    $("#breadcrumb-job").click(function() {
        $("#content").load("html/status/job/jobs_status_overview.html");
    });
}