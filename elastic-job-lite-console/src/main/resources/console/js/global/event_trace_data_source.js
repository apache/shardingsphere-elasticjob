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
    doLocale();
    authorityControl();
    renderDataSources();
    validate();
    dealDataSourceModal();
    handleFieldValidator();
    submitDataSource();
    bindButtons();
    bindConnectionTest();
});

function renderDataSources() {
    $("#data-sources").bootstrapTable({
        url: "api/data-source",
        cache: false,
        search: true,
        showRefresh: true,
        showColumns: true
    }).on("all.bs.table", function() {
        doLocale();
    });
    renderDataSourceForDashboardNav();
}

function generateOperationButtons(val, row) {
    var operationTd;
    var name = row.name;
    if (row.activated) {
        operationTd = "<button disabled operation='connect-datasource' class='btn-xs' dataSourceName='" + name + "' data-lang='status-connected'></button>&nbsp;<button operation='delete-datasource' class='btn-xs btn-danger' data-toggle='modal' id='delete-dialog' dataSourceName='" + name + "' data-lang='operation-delete'></button>";
    } else {
        operationTd = "<button operation='connect-datasource' class='btn-xs btn-info' dataSourceName='" + name + "' data-loading-text='loading...' data-lang='operation-connect'></button>&nbsp;<button operation='delete-datasource' class='btn-xs btn-danger' data-toggle='modal' id='delete-dialog' dataSourceName='" + name + "' data-lang='operation-delete'></button>";
    }
    return operationTd;
}

function bindButtons() {
    bindConnectButtons();
    bindDeleteButtons();
}

function bindConnectButtons() {
    $(document).off("click", "button[operation='connect-datasource']");
    $(document).on("click", "button[operation='connect-datasource']", function(event) {
        var btn = $(this).button("loading");
        var dataSourceName = $(event.currentTarget).attr("dataSourceName");
        $.ajax({
            url: "api/data-source/connect",
            type: "POST",
            data: JSON.stringify({"name" : dataSourceName}),
            contentType: "application/json",
            dataType: "json",
            success: function(data) {
                if (data) {
                    $("#data-sources").bootstrapTable("refresh");
                    renderDataSourceForDashboardNav();
                    showSuccessDialog();
                } else {
                    showDataSourceFailureDialog();
                }
                btn.button("reset");
            }
        });
    });
}

function bindDeleteButtons() {
    $(document).off("click", "button[operation='delete-datasource']");
    $(document).on("click", "button[operation='delete-datasource']", function(event) {
        showDeleteConfirmModal();
        var dataSourceName = $(event.currentTarget).attr("dataSourceName");
        $(document).off("click", "#confirm-btn");
        $(document).on("click", "#confirm-btn", function() {
            $.ajax({
                url: "api/data-source",
                type: "DELETE",
                data: JSON.stringify({"name" : dataSourceName}),
                contentType: "application/json",
                dataType: "json",
                success: function() {
                    $("#data-sources").bootstrapTable("refresh");
                    $("#confirm-dialog").modal("hide");
                    $(".modal-backdrop").remove();
                    $("body").removeClass("modal-open");
                    renderDataSourceForDashboardNav();
                    refreshEventTraceNavTag();
                }
            });
        });
    });
}

function dealDataSourceModal() {
    $("#add-data-source").click(function() {
        $("#add-data-source-center").modal({backdrop: 'static', keyboard: true});
    });
    $("#close-add-data-source-form").click(function() {
        $("#add-data-source-center").on("hide.bs.modal", function () {
            $("#data-source-form")[0].reset();
        });
        $("#data-source-form").data("bootstrapValidator").resetForm();
    });
}

function handleFieldValidator() {
    $("#password").focus(function() {
        $("#data-source-form").data("bootstrapValidator").enableFieldValidators("password", true);
    });
    $("#password").blur(function() {
        $("#data-source-form").data("bootstrapValidator").enableFieldValidators("password", "" === $("#password").val() ? false : true);
    });
}

function submitDataSource() {
    $("#add-data-source-btn").on("click", function(event) {
        if ("" === $("#password").val()) {
            $("#data-source-form").data("bootstrapValidator").enableFieldValidators("password", false);
        }
        var bootstrapValidator = $("#data-source-form").data("bootstrapValidator");
        bootstrapValidator.validate();
        if(bootstrapValidator.isValid()) {
            var name = $("#name").val();
            var driver = $("#driver").val();
            var url = $("#url").val();
            var username = $("#username").val();
            var password = $("#password").val();
            $.ajax({
                url: "api/data-source",
                type: "POST",
                data: JSON.stringify({"name": name, "driver": driver, "url": url, "username": username, "password": password}),
                contentType: "application/json",
                dataType: "json",
                success: function(data) {
                    if (data) {
                        $("#add-data-source-center").on("hide.bs.modal", function() {
                            $("#data-source-form")[0].reset();
                        });
                        $("#data-source-form").data("bootstrapValidator").resetForm();
                        $("#add-data-source-center").modal("hide");
                        $("#data-sources").bootstrapTable("refresh");
                        $(".modal-backdrop").remove();
                        $("body").removeClass("modal-open");
                        renderDataSourceForDashboardNav();
                        refreshEventTraceNavTag();
                    }
                }
            });
        }
    });
}

function validate() {
    $("#data-source-form").bootstrapValidator({
        message: "This value is not valid",
        feedbackIcons: {
            valid: "glyphicon glyphicon-ok",
            invalid: "glyphicon glyphicon-remove",
            validating: "glyphicon glyphicon-refresh"
        },
        fields: {
            name: {
                validators: {
                    notEmpty: {
                        message: $.i18n.prop("event-trace-data-source-name-not-null")
                    },
                    stringLength: {
                        max: 50,
                        message: $.i18n.prop("event-trace-data-source-name-length-limit")
                    },
                    callback: {
                        message: $.i18n.prop("event-trace-data-source-existed"),
                        callback: function() {
                            var dataSourceName = $("#name").val();
                            var result = true;
                            $.ajax({
                                url: "api/data-source",
                                contentType: "application/json",
                                async: false,
                                success: function(data) {
                                    for (var index = 0; index < data.length; index++) {
                                        if (dataSourceName === data[index].name) {
                                            result = false;
                                        }
                                    }
                                }
                            });
                            return result;
                        }
                    }
                }
            },
            url: {
                validators: {
                    notEmpty: {
                        message: $.i18n.prop("event-trace-data-source-url-not-null")
                    },
                    stringLength: {
                        max: 200,
                        message: $.i18n.prop("event-trace-data-source-url-length-limit")
                    }
                }
            },
            username: {
                validators: {
                    notEmpty: {
                        message: $.i18n.prop("event-trace-data-source-username-not-null")
                    },
                    stringLength: {
                        max: 50,
                        message: $.i18n.prop("event-trace-data-source-username-length-limit")
                    }
                }
            },
            password: {
                validators: {
                    stringLength: {
                        max: 50,
                        message: $.i18n.prop("event-trace-data-source-password-length-limit")
                    }
                }
            }
        }
    });
    $("#data-source-form").submit(function(event) {
        event.preventDefault();
    });
}

function bindConnectionTest() {
    $("#connect-test").on("click", function() {
        var name = $("#name").val();
        var driver = $("#driver").val();
        var url = $("#url").val();
        var username = $("#username").val();
        var password = $("#password").val();
        $.ajax({
            url: "api/data-source/connectTest",
            type: "POST",
            data: JSON.stringify({"name": name, "driver": driver, "url": url, "username": username, "password": password}),
            contentType: "application/json",
            dataType: "json",
            success: function(data) {
                if (data) {
                    showDataSourceTestConnectionSuccessDialog();
                } else {
                    showDataSourceTestConnectionFailureDialog();
                }
            }
        });
    });
}
