$(function() {
    $("#server-ip").text($("#index-server-ip").text());
    renderJobs();
    bindButtons();
    renderBreadCrumbMenu();
});

function renderJobs() {
    var ip = $("#server-ip").text();
    $("#server-jobs").bootstrapTable({
        url: "/api/servers/" + ip + "/jobs",
        cache: false,
        columns: 
        [{
            field: "jobName",
            title: "作业名",
            sortable: "true"
        }, {
            field: "shardingItems",
            title: "分片项"
        }, {
            field: "status",
            title: "状态",
            sortable: "true",
            formatter: "statusFormatter"
        }, {
            field: "operation",
            title: "操作",
            formatter: "generateOperationButtons"
        }]
    });
}

function statusFormatter(value) {
    switch(value) {
        case "OK":
            return "<span class='label label-success'>正常</span>";
            break;
        case "DISABLED":
            return "<span class='label label-warning'>被禁用</span>";
            break;
    }
}

function generateOperationButtons(val, row) {
    var disableButton = "<button operation='disable' class='btn-xs btn-warning' ip='" + row.ip + "' job-name='" + row.jobName + "'>禁用</button>";
    var enableButton = "<button operation='enable' class='btn-xs btn-success' ip='" + row.ip + "' job-name='" + row.jobName + "'>启用</button>";
    var shutdownButton = "<button operation='shutdown' class='btn-xs btn-danger' job-name='" + row.jobName + "'>关闭</button>";
    var operationTd = "";
    if ("DISABLED" === row.status) {
        operationTd = enableButton + "&nbsp;" + shutdownButton;
    } else {
        operationTd = disableButton + "&nbsp;" + shutdownButton;
    }
    return operationTd;
}

function bindButtons() {
    bindDisableButton();
    bindEnableButton();
    bindShutdownButton();
}

function bindDisableButton() {
    $(document).on("click", "button[operation='disable']", function(event) {
        $.ajax({
            url: "/api/servers/" + $("#server-ip").text() + "/jobs/" + $(event.currentTarget).attr("job-name") + "/disable",
            type: "POST",
            success: function() {
                $("#server-jobs").bootstrapTable("refresh");
                showSuccessDialog();
            }
        });
    });
}

function bindEnableButton() {
    $(document).on("click", "button[operation='enable']", function(event) {
        $.ajax({
            url: "/api/servers/" + $("#server-ip").text() + "/jobs/" + $(event.currentTarget).attr("job-name") + "/disable",
            type: "DELETE",
            success: function() {
                $("#server-jobs").bootstrapTable("refresh");
                showSuccessDialog();
            }
        });
    });
}

function bindShutdownButton() {
    $(document).on("click", "button[operation='shutdown']", function(event) {
        $.ajax({
            url: "/api/servers/" + $("#server-ip").text() + "/jobs/" + $(event.currentTarget).attr("job-name") + "/shutdown",
            type: "POST",
            success: function(){
                $("#server-jobs").bootstrapTable("refresh");
                showSuccessDialog();
            }
        });
    });
}

function renderBreadCrumbMenu() {
    $("#breadcrumb-server").click(function() {
        $("#content").load("html/status/server/servers_status_overview.html");
    });
}
