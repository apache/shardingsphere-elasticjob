$(function() {
    renderServersOverview();
    bindOperationButtons();
});

function renderServersOverview() {
    var jsonData = {
        cache: false,
        columns:
            [{
                field: "serverIp",
                title: "服务器IP",
                sortable: "true"
            }, {
                field: "instancesNum",
                title: "运行实例数",
                sortable: "true"
            }, {
                field: "jobsNum",
                title: "作业总数",
                sortable: "true"
            }, {
                field: "disabledJobsNum",
                title: "禁用作业数",
                sortable: "true"
            }, {
                field: "operation",
                title: "操作",
                formatter: "generateOperationButtons"
            }]
    };
    var activated = false;
    $.ajax({
        url: "/api/registry-center/activated",
        async: false,
        success: function(data) {
            activated = data;
        }
    });
    if (activated) {
        jsonData.url = "/api/servers";
    }
    $("#servers-overview-tbl").bootstrapTable(jsonData);
}

function bindOperationButtons() {
    bindServerStatusDetailButton();
    bindDisableServerButton();
    bindEnableServerButton();
    bindShutdownServerButton();
    bindRemoveServerButton();
}

function generateOperationButtons(val, row) {
    var detailButton = "<button operation='server-detail' class='btn-xs btn-info' server-ip='" + row.serverIp + "'>详情</button>";
    var disableButton = "<button operation='disable-server' class='btn-xs btn-warning' server-ip='" + row.serverIp + "'>禁用</button>";
    var enableButton = "<button operation='enable-server' class='btn-xs btn-success' server-ip='" + row.serverIp + "'>启用</button>";
    var shutdownButton = "<button operation='shutdown-server' class='btn-xs btn-danger' server-ip='" + row.serverIp + "'>终止</button>";
    var removeButton = "<button operation='remove-server' class='btn-xs btn-danger' server-ip='" + row.serverIp + "'>清理</button>";
    if (row.instancesNum == 0) {
        return removeButton;
    }
    var operationTd = "";
    if (row.disabledJobsNum > 0) {
        operationTd = detailButton  + "&nbsp;" + enableButton + "&nbsp;" + shutdownButton;
    } else if (row.instancesNum > 0) {
        operationTd = detailButton  + "&nbsp;" + disableButton + "&nbsp;" + shutdownButton;
    }
    return operationTd;
}

function bindServerStatusDetailButton() {
    $(document).on("click", "button[operation='server-detail'][data-toggle!='modal']", function(event) {
        var serverIp = $(event.currentTarget).attr("server-ip");
        $("#index-server-ip").text(serverIp);
        $("#content").load("html/status/server/server_status_detail.html");
    });
}

function bindDisableServerButton() {
    $(document).on("click", "button[operation='disable-server']", function(event) {
        var serverIp = $(event.currentTarget).attr("server-ip");
        $.ajax({
            url: "/api/servers/" + serverIp + "/disable",
            type: "POST",
            success: function() {
                showSuccessDialog();
                $("#servers-overview-tbl").bootstrapTable("refresh");
            }
        });
    });
}

function bindEnableServerButton() {
    $(document).on("click", "button[operation='enable-server']", function(event) {
        var serverIp = $(event.currentTarget).attr("server-ip");
        $.ajax({
            url: "/api/servers/" + serverIp + "/disable",
            type: "DELETE",
            success: function() {
                showSuccessDialog();
                $("#servers-overview-tbl").bootstrapTable("refresh");
            }
        });
    });
}

function bindShutdownServerButton() {
    $(document).on("click", "button[operation='shutdown-server']", function(event) {
        $("#shutdown-confirm-dialog").modal({backdrop: 'static', keyboard: true});
        var serverIp = $(event.currentTarget).attr("server-ip");
        $(document).off("click", "#shutdown-confirm-dialog-confirm-btn");
        $(document).on("click", "#shutdown-confirm-dialog-confirm-btn", function() {
            $.ajax({
                url: "/api/servers/" + serverIp + "/shutdown",
                type: "POST",
                success: function () {
                    $("#shutdown-confirm-dialog").modal("hide");
                    $(".modal-backdrop").remove();
                    $("body").removeClass("modal-open");
                    $("#servers-overview-tbl").bootstrapTable("refresh");
                }
            });
        });
    });
}

function bindRemoveServerButton() {
    $(document).on("click", "button[operation='remove-server']", function(event) {
        $("#delete-confirm-dialog").modal({backdrop: 'static', keyboard: true});
        var serverIp = $(event.currentTarget).attr("server-ip");
        $(document).off("click", "#delete-confirm-dialog-confirm-btn");
        $(document).on("click", "#delete-confirm-dialog-confirm-btn", function() {
            $.ajax({
                url: "/api/servers/" + serverIp,
                type: "DELETE",
                success: function () {
                    $("#delete-confirm-dialog").modal("hide");
                    $(".modal-backdrop").remove();
                    $("body").removeClass("modal-open");
                    refreshServerNavTag();
                    $("#servers-overview-tbl").bootstrapTable("refresh");
                }
            });
        });
    });
}
