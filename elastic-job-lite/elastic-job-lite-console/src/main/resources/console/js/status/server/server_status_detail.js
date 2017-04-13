$(function() {
    $("#server-ip").text($("#index-server-ip").text());
    renderJobs();
    renderBreadCrumbMenu();
});

function renderJobs() {
    var ip = $("#server-ip").text();
    $("#server-jobs-tbl").bootstrapTable({
        url: "/api/servers/" + ip + "/jobs",
        cache: false,
        columns: 
        [{
            field: "jobName",
            title: "作业名",
            sortable: "true"
        }, {
            field: "instanceCount",
            title: "运行实例数"
        }, {
            field: "status",
            title: "状态",
            sortable: "true",
            formatter: "statusFormatter"
        }, {
            field: "operation",
            title: "操作",
            formatter: "generateOperationButtons"
        }],
        onLoadSuccess: function() {
            bindButtons();
        },
        onSort: function(name, order) {
            $("#server-jobs-tbl").bootstrapTable("refresh");
        }
    });
}

function statusFormatter(val, row) {
    if (0 === row.instanceCount ) {
        return "<span class='label label-default'>已下线</span>";
    }
    switch(val) {
        case "OK":
            return "<span class='label label-success'>已启用</span>";
            break;
        case "DISABLED":
            return "<span class='label label-warning'>已禁用</span>";
            break;
    }
}

function generateOperationButtons(val, row) {
    if (0 === row.instanceCount ) {
        return "<button operation='remove' class='btn-xs btn-danger' job-name='" + row.jobName + "'>清理</button>";
    }
    var disableButton = "<button operation='disable' class='btn-xs btn-warning' ip='" + row.ip + "' job-name='" + row.jobName + "'>禁用</button>";
    var enableButton = "<button operation='enable' class='btn-xs btn-success' ip='" + row.ip + "' job-name='" + row.jobName + "'>启用</button>";
    var shutdownButton = "<button operation='shutdown' class='btn-xs btn-danger' job-name='" + row.jobName + "'>终止</button>";
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
    bindRemoveButton();
}

function bindDisableButton() {
    $("button[operation='disable']").click(function(event) {
        $.ajax({
            url: "/api/servers/" + $("#server-ip").text() + "/jobs/" + $(event.currentTarget).attr("job-name") + "/disable",
            type: "POST",
            success: function() {
                $("#server-jobs-tbl").bootstrapTable("refresh");
                showSuccessDialog();
            }
        });
    });
}

function bindEnableButton() {
    $("button[operation='enable']").click(function(event) {
        $.ajax({
            url: "/api/servers/" + $("#server-ip").text() + "/jobs/" + $(event.currentTarget).attr("job-name") + "/disable",
            type: "DELETE",
            success: function() {
                $("#server-jobs-tbl").bootstrapTable("refresh");
                showSuccessDialog();
            }
        });
    });
}

function bindShutdownButton() {
    $("button[operation='shutdown']").click(function(event) {
        $("#shutdown-confirm-dialog").modal({backdrop: 'static', keyboard: true});
        var serverIp = $("#server-ip").text();
        var jobName = $(event.currentTarget).attr("job-name");
        $(document).off("click", "#shutdown-confirm-dialog-confirm-btn");
        $(document).on("click", "#shutdown-confirm-dialog-confirm-btn", function() {
            $.ajax({
                url: "/api/servers/" + serverIp + "/jobs/" + jobName + "/shutdown",
                type: "POST",
                success: function () {
                    $("#shutdown-confirm-dialog").modal("hide");
                    $(".modal-backdrop").remove();
                    $("body").removeClass("modal-open");
                    $("#server-jobs-tbl").bootstrapTable("refresh");
                }
            });
        });
    });
}

function bindRemoveButton() {
    $("button[operation='remove']").click(function(event) {
        $("#delete-confirm-dialog").modal({backdrop: 'static', keyboard: true});
        var serverIp = $("#server-ip").text();
        var jobName = $(event.currentTarget).attr("job-name");
        $(document).off("click", "#delete-confirm-dialog-confirm-btn");
        $(document).on("click", "#delete-confirm-dialog-confirm-btn", function() {
            $.ajax({
                url: "/api/servers/" + serverIp + "/jobs/" + jobName,
                type: "DELETE",
                success: function () {
                    $("#delete-confirm-dialog").modal("hide");
                    $(".modal-backdrop").remove();
                    $("body").removeClass("modal-open");
                    refreshServerNavTag();
                    $("#server-jobs-tbl").bootstrapTable("refresh");
                }
            });
        });
    });
}

function renderBreadCrumbMenu() {
    $("#breadcrumb-server").click(function() {
        $("#content").load("html/status/server/servers_status_overview.html");
    });
}
