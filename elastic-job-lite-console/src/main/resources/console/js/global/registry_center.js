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
    renderRegCenters();
    validate();
    dealRegCenterModal();
    handleFieldValidator();
    submitRegCenter();
    bindButtons();
});

function renderRegCenters() {
    $("#reg-centers").bootstrapTable({
        url: "api/registry-center",
        cache: false,
        search: true,
        showRefresh: true,
        showColumns: true
    }).on("all.bs.table", function() {
        doLocale();
    });
    renderRegCenterForDashboardNav();
}

function generateOperationButtons(val, row) {
    var operationTd;
    var name = row.name;
    if (row.activated) {
        operationTd = "<button disabled operation='connect-reg-center' class='btn-xs' regName='" + name + "' data-lang='status-connected'></button>&nbsp;<button operation='delete-reg-center' class='btn-xs btn-danger' data-toggle='modal' id='delete-dialog' regName='" + name + "' data-lang='operation-delete'></button>";
    } else {
        operationTd = "<button operation='connect-reg-center' class='btn-xs btn-info' regName='" + name + "' data-loading-text='loading...' data-lang='operation-connect'></button>&nbsp;<button operation='delete-reg-center' class='btn-xs btn-danger' data-toggle='modal' id='delete-dialog' regName='" + name + "' data-lang='operation-delete'></button>";
    }
    return operationTd;
}

function bindButtons() {
    bindConnectButtons();
    bindDeleteButtons();
}

function bindConnectButtons() {
    $(document).off("click", "button[operation='connect-reg-center']");
    $(document).on("click", "button[operation='connect-reg-center']", function(event) {
        var btn = $(this).button("loading");
        var regName = $(event.currentTarget).attr("regName");
        $.ajax({
            url: "api/registry-center/connect",
            type: "POST",
            data: JSON.stringify({"name" : regName}),
            contentType: "application/json",
            dataType: "json",
            success: function(data) {
                if (data) {
                    $("#reg-centers").bootstrapTable("refresh");
                    renderRegCenterForDashboardNav();
                    refreshJobNavTag();
                    refreshServerNavTag();
                    showSuccessDialog();
                } else {
                    showRegCenterFailureDialog();
                }
                btn.button("reset");
            }
        });
    });
}

function bindDeleteButtons() {
    $(document).off("click", "button[operation='delete-reg-center']");
    $(document).on("click", "button[operation='delete-reg-center']", function(event) {
        showDeleteConfirmModal();
        var regName = $(event.currentTarget).attr("regName");
        $(document).off("click", "#confirm-btn");
        $(document).on("click", "#confirm-btn", function() {
            $.ajax({
                url: "api/registry-center",
                type: "DELETE",
                data: JSON.stringify({"name" : regName}),
                contentType: "application/json",
                dataType: "json",
                success: function() {
                    $("#reg-centers").bootstrapTable("refresh");
                    $("#confirm-dialog").modal("hide");
                    $(".modal-backdrop").remove();
                    $("body").removeClass("modal-open");
                    renderRegCenterForDashboardNav();
                    refreshRegCenterNavTag();
                }
            });
        });
    });
}

function dealRegCenterModal() {
    $("#add-register").click(function() {
        $("#add-reg-center").modal({backdrop: 'static', keyboard: true});
    });
    $("#close-add-reg-form").click(function() {
        $("#add-reg-center").on("hide.bs.modal", function () {
            $("#reg-center-form")[0].reset();
        });
        $("#reg-center-form").data("bootstrapValidator").resetForm();
    });
}

function handleFieldValidator() {
    $("#digest").focus(function() {
        $("#reg-center-form").data("bootstrapValidator").enableFieldValidators("digest", true);
    });
    $("#digest").blur(function() {
        $("#reg-center-form").data("bootstrapValidator").enableFieldValidators("digest", "" !== $("#digest").val());
    });
    $("#namespace").focus(function() {
        $("#reg-center-form").data("bootstrapValidator").enableFieldValidators("namespace", true);
    });
    $("#namespace").blur(function() {
        $("#reg-center-form").data("bootstrapValidator").enableFieldValidators("namespace", "" !== $("#namespace").val());
    });
}

function submitRegCenter() {
    $("#add-reg-center-btn").on("click", function(event) {
        if ("" === $("#digest").val()) {
            $("#reg-center-form").data("bootstrapValidator").enableFieldValidators("digest", false);
        }
        if ("" === $("#namespace").val()) {
            $("#reg-center-form").data("bootstrapValidator").enableFieldValidators("namespace", false);
        }
        var bootstrapValidator = $("#reg-center-form").data("bootstrapValidator");
        bootstrapValidator.validate();
        if(bootstrapValidator.isValid()) {
            var name = $("#name").val();
            var zkAddressList = $("#zk-address-list").val();
            var namespace = $("#namespace").val();
            var digest = $("#digest").val();
            $.ajax({
                url: "api/registry-center",
                type: "POST",
                data: JSON.stringify({"name": name, "zkAddressList": zkAddressList, "namespace": namespace, "digest": digest}),
                contentType: "application/json",
                dataType: "json",
                success: function(data) {
                    if (data) {
                        $("#add-reg-center").on("hide.bs.modal", function() {
                            $("#reg-center-form")[0].reset();
                        });
                        $("#reg-center-form").data("bootstrapValidator").resetForm();
                        $("#add-reg-center").modal("hide");
                        $("#reg-centers").bootstrapTable("refresh");
                        $(".modal-backdrop").remove();
                        $("body").removeClass("modal-open");
                        renderRegCenterForDashboardNav();
                        refreshRegCenterNavTag();
                    }
                }
            });
        }
    });
}

function validate() {
    $("#reg-center-form").bootstrapValidator({
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
                        message: $.i18n.prop("registry-center-name-not-null")
                    },
                    stringLength: {
                        max: 50,
                        message: $.i18n.prop("registry-center-name-length-limit")
                    },
                    callback: {
                        message: $.i18n.prop("registry-center-existed"),
                        callback: function() {
                            var regName = $("#name").val();
                            var result = true;
                            $.ajax({
                                url: "api/registry-center",
                                contentType: "application/json",
                                async: false,
                                success: function(data) {
                                    for (var index = 0; index < data.length; index++) {
                                        if (regName === data[index].name) {
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
            zkAddressList: {
                validators: {
                    notEmpty: {
                        message: $.i18n.prop("registry-center-zk-address-not-null")
                    },
                    stringLength: {
                        max: 100,
                        message: $.i18n.prop("registry-center-zk-address-length-limit")
                    }
                }
            },
            namespace: {
                validators: {
                    stringLength: {
                        max: 50,
                        message: $.i18n.prop("registry-center-namespace-length-limit")
                    }
                }
            },
            digest: {
                validators: {
                    stringLength: {
                        max: 20,
                        message: $.i18n.prop("registry-center-digest-length-limit")
                    }
                }
            }
        }
    });
    $("#reg-center-form").submit(function(event) {
        event.preventDefault();
    });
}
