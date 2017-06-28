$(function() {
    authorityControl();
    renderServersOverview();
    bindButtons();
});

function renderServersOverview() {
    var jsonData = {
        cache: false,
        search: true,
        showRefresh: true,
        showColumns: true
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
    $("#servers-overview-tbl").bootstrapTable({
        columns: jsonData.columns,
        url: jsonData.url,
        cache: jsonData.cache
    }).on("all.bs.table", function() {
        doLocale();
    });
}

function bindButtons() {
    bindServerStatusDetailButton();
    bindDisableServerButton();
    bindEnableServerButton();
    bindShutdownServerButton();
    bindRemoveServerButton();
}

function generateOperationButtons(val, row) {
    var detailButton = "<button operation='server-detail' class='btn-xs btn-info' server-ip='" + row.serverIp + "' data-lang='operation-detail'></button>";
    var disableButton = "<button operation='disable-server' class='btn-xs btn-warning' server-ip='" + row.serverIp + "' data-lang='operation-disable'></button>";
    var enableButton = "<button operation='enable-server' class='btn-xs btn-success' server-ip='" + row.serverIp + "' data-lang='operation-enable'></button>";
    var shutdownButton = "<button operation='shutdown-server' class='btn-xs btn-danger' server-ip='" + row.serverIp + "' data-lang='operation-shutdown'></button>";
    var removeButton = "<button operation='remove-server' class='btn-xs btn-danger' server-ip='" + row.serverIp + "' data-lang='operation-remove'></button>";
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
    $(document).off("click", "button[operation='server-detail'][data-toggle!='modal']");
    $(document).on("click", "button[operation='server-detail'][data-toggle!='modal']", function(event) {
        var serverIp = $(event.currentTarget).attr("server-ip");
        $("#index-server-ip").text(serverIp);
        $("#content").load("html/status/server/server_status_detail.html", null, function(){
            doLocale();
        });
    });
}

function bindDisableServerButton() {
    $(document).off("click", "button[operation='disable-server'][data-toggle!='modal']");
    $(document).on("click", "button[operation='disable-server'][data-toggle!='modal']", function(event) {
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
    $(document).off("click", "button[operation='enable-server'][data-toggle!='modal']");
    $(document).on("click", "button[operation='enable-server'][data-toggle!='modal']", function(event) {
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
    $(document).off("click", "button[operation='shutdown-server'][data-toggle!='modal']");
    $(document).on("click", "button[operation='shutdown-server'][data-toggle!='modal']", function(event) {
        showShutdownConfirmModal();
        var serverIp = $(event.currentTarget).attr("server-ip");
        $(document).off("click", "#confirm-btn");
        $(document).on("click", "#confirm-btn", function() {
            $.ajax({
                url: "/api/servers/" + serverIp + "/shutdown",
                type: "POST",
                success: function () {
                    $("#confirm-dialog").modal("hide");
                    $(".modal-backdrop").remove();
                    $("body").removeClass("modal-open");
                    $("#servers-overview-tbl").bootstrapTable("refresh");
                }
            });
        });
    });
}

function bindRemoveServerButton() {
    $(document).off("click", "button[operation='remove-server'][data-toggle!='modal']");
    $(document).on("click", "button[operation='remove-server'][data-toggle!='modal']", function(event) {
        showDeleteConfirmModal();
        var serverIp = $(event.currentTarget).attr("server-ip");
        $(document).off("click", "#confirm-btn");
        $(document).on("click", "#confirm-btn", function() {
            $.ajax({
                url: "/api/servers/" + serverIp,
                type: "DELETE",
                success: function () {
                    $("#confirm-dialog").modal("hide");
                    $(".modal-backdrop").remove();
                    $("body").removeClass("modal-open");
                    refreshServerNavTag();
                    $("#servers-overview-tbl").bootstrapTable("refresh");
                }
            });
        });
    });
}
