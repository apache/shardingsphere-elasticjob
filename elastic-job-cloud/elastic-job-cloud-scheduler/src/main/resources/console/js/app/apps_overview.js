$(function() {
    authorityControl();
    renderAppOverview();
    $("#add-app").click(function() {
        $(".box-body").remove();
        $("#add-app-body").load("html/app/add_app.html");
        $("#data-add-app").modal({backdrop: "static", keyboard: true});
    });
    bindDetailAppButton();
    bindModifyAppButton();
    bindEnableAppButton();
    bindDisableAppButton();
    bindDeleteAppButton();
});

function renderAppOverview() {
    var jsonData = {
        url: "/api/app/list",
        cache: false,
        columns:
            [{
                field: "appName",
                title: "应用名称",
                sortable: "true"
            }, {
                field: "appURL",
                title: "应用下载路径",
                sortable: "true"
            }, {
                field: "bootstrapScript",
                title: "启动脚本",
                sortable: "true"
            },  {
                field: "operation",
                title: "操作",
                formatter: "operationApp"
            }]
    };
    $("#app-table").bootstrapTable({
        columns: jsonData.columns,
        url: jsonData.url,
        cache: jsonData.cache
    });
}

function operationApp(val, row) {
    var detailButton = "<button operation='detailApp' class='btn-xs btn-info' appName='" + row.appName + "'>详情</button>";
    var modifyButton = "<button operation='modifyApp' class='btn-xs btn-warning' appName='" + row.appName + "'>修改</button>";
    var deleteButton = "<button operation='deleteApp' class='btn-xs btn-danger' appName='" + row.appName + "'>删除</button>";
    var enableButton = "<button operation='enableApp' class='btn-xs btn-success' appName='" + row.appName + "'>生效</button>";
    var disableButton = "<button operation='disableApp' class='btn-xs btn-warning' appName='" + row.appName + "'>失效</button>";
    var operationId = detailButton + "&nbsp;" + modifyButton  +"&nbsp;" + deleteButton;
    if(selectAppStatus(row.appName)) {
        operationId = operationId + "&nbsp;" + enableButton;
    } else {
        operationId = operationId + "&nbsp;" + disableButton;
    }
    return operationId;
}

function bindDetailAppButton() {
    $(document).off("click", "button[operation='detailApp'][data-toggle!='modal']");
    $(document).on("click", "button[operation='detailApp'][data-toggle!='modal']", function(event) {
        var appName = $(event.currentTarget).attr("appName");
        $.ajax({
            url: "/api/app/" + appName,
            contentType: "application/json",
            success: function(result) {
                if (null !== result) {
                    $(".box-body").remove();
                    $("#detail-app-body").load("html/app/detail_app.html", null, function() {
                        renderApp(result);
                        $("#data-detail-app").modal({backdrop : "static", keyboard : true});
                        $("#close-button").on("click", function() {
                            $("#data-detail-app").modal("hide");
                        });
                    });
                }
            }
        });
    });
}

function bindModifyAppButton() {
    $(document).off("click", "button[operation='modifyApp'][data-toggle!='modal']");
    $(document).on("click", "button[operation='modifyApp'][data-toggle!='modal']", function(event) {
        var appName = $(event.currentTarget).attr("appName");
        $.ajax({
            url: "/api/app/" + appName,
            success: function(result) {
                if(null !== result) {
                    $(".box-body").remove();
                    $("#update-app-body").load("html/app/modify_app.html", null, function() {
                        renderApp(result);
                        $("#data-update-app").modal({backdrop : "static", keyboard : true});
                    });
                }
            }
        });
    });
}

function bindEnableAppButton() {
    $(document).off("click", "button[operation='enableApp'][data-toggle!='modal']");
    $(document).on("click", "button[operation='enableApp'][data-toggle!='modal']", function(event) {
        var appName = $(event.currentTarget).attr("appName");
        $.ajax({
            url: "/api/app/" + appName + "/disable",
            type: "DELETE",
            contentType: "application/json",
            success: function(result) {
                showSuccessDialog();
                $("#app-table").bootstrapTable("refresh");
            }
        });
    });
}

function bindDisableAppButton() {
    $(document).off("click", "button[operation='disableApp'][data-toggle!='modal']");
    $(document).on("click", "button[operation='disableApp'][data-toggle!='modal']", function(event) {
        var appName = $(event.currentTarget).attr("appName");
        $.ajax({
            url: "/api/app/" + appName + "/disable",
            type: "POST",
            contentType: "application/json",
            success: function(result) {
                showSuccessDialog();
                $("#app-table").bootstrapTable("refresh");
            }
        });
    });
}

function bindDeleteAppButton() {
    $(document).off("click", "button[operation='deleteApp'][data-toggle!='modal']");
    $(document).on("click", "button[operation='deleteApp'][data-toggle!='modal']", function(event) {
        var appName = $(event.currentTarget).attr("appName");
        $("#delete-data-app").modal({backdrop : "static", keyboard : true});
        var flag = true;
        $("#delete-app-remove").on("click", function() {
            flag = false;
        });
        $("#delete-app-confirm").on("click", function() {
            if(flag) {
                $.ajax({
                    url: "/api/app/" + appName,
                    type: "DELETE",
                    contentType: "application/json",
                    success: function(result) {
                        $("#app-table").bootstrapTable("refresh");
                        $("#delete-data-app").hide();
                        refreshAppNavTag();
                        refreshJobNavTag();
                    }
                });
            }
        });
    });
}

function renderApp(app) {
    $("#app-name").attr("value", app.appName);
    $("#cpu-count").attr("value", app.cpuCount); 
    $("#app-memory").attr("value", app.memoryMB);
    $("#bootstrap-script").attr("value", app.bootstrapScript);
    $("#app-url").attr("value", app.appURL);
    $("#event-trace-sampling-count").val(app.eventTraceSamplingCount);
    $("#app-cache-enable").prop("checked", app.appCacheEnable);
}
