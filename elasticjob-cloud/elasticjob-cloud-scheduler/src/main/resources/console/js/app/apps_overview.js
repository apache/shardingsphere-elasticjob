/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

$(function() {
    authorityControl();
    renderAppOverview();
    $("#add-app").click(function() {
        $(".box-body").remove();
        $("#add-app-body").load("html/app/add_app.html", null, function() {
            doLocale();
            tooltipLocale();
        });
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
        cache: false
    };
    $("#app-table").bootstrapTable({
        columns: jsonData.columns,
        url: jsonData.url,
        cache: jsonData.cache
    }).on("all.bs.table", function() {
        doLocale();
    });
}

function operationApp(val, row) {
    var detailButton = "<button operation='detailApp' class='btn-xs btn-info' appName='" + row.appName + "' data-lang='operation-detail'></button>";
    var modifyButton = "<button operation='modifyApp' class='btn-xs btn-warning' appName='" + row.appName + "' data-lang='operation-update'></button>";
    var deleteButton = "<button operation='deleteApp' class='btn-xs btn-danger' appName='" + row.appName + "' data-lang='operation-delete'></button>";
    var enableButton = "<button operation='enableApp' class='btn-xs btn-success' appName='" + row.appName + "' data-lang='operation-enable'></button>";
    var disableButton = "<button operation='disableApp' class='btn-xs btn-warning' appName='" + row.appName + "' data-lang='operation-disable'></button>";
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
                        doLocale();
                        tooltipLocale();
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
                        doLocale();
                        tooltipLocale();
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
            url: "/api/app/" + appName + "/enable",
            type: "POST",
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
