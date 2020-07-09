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

function validate() {
    $("#app-form").bootstrapValidator({
        message: "This value is not valid",
        feedbackIcons: {
            valid: "glyphicon glyphicon-ok",
            invalid: "glyphicon glyphicon-remove",
            validating: "glyphicon glyphicon-refresh"
        },
        fields: {
            appName: {
                validators: {
                    notEmpty: {
                        message: $.i18n.prop("app-name-not-null")
                    },
                    stringLength: {
                        max: 100,
                        message: $.i18n.prop("app-name-length-limit")
                    },
                    callback: {
                        message: $.i18n.prop("app-name-exists"),
                        callback: function() {
                            var appName = $("#app-name").val();
                            var result = true;
                                $.ajax({
                                    url: "/api/app/" + appName,
                                    contentType: "application/json",
                                    async: false,
                                    success: function(data) {
                                        if (null !== data) {
                                            result = false;
                                        }
                                    }
                                });
                            return result;
                        }
                    }
                }
            },
            bootstrapScript: {
                validators: {
                    notEmpty: {
                        message: $.i18n.prop("app-bootstrap-script-not-null")
                    }
                }
            },
            cpuCount: {
                validators: {
                    notEmpty: {
                        message: $.i18n.prop("app-cpu-count-not-null")
                    },
                    regexp: {
                        regexp: /^(-?\d+)(\.\d+)?$/,
                        message: $.i18n.prop("app-cpu-count-regexp-limit")
                    }
                }
            },
            appMemory: {
                validators: {
                    notEmpty: {
                        message: $.i18n.prop("app-memory-not-null")
                    }
                }
            },
            eventTraceSamplingCount: {
                validators: {
                    notEmpty: {
                        message: $.i18n.prop("event-trace-sampling-count-not-null")
                    }
                }
            },
            appURL: {
                validators: {
                    notEmpty: {
                        message: $.i18n.prop("app-url-not-null")
                    }
                }
            }
        }
    });
}

function submitConfirm(type, modal) {
    $("#save-button").on("click", function() {
        var bootstrapValidator = $("#app-form").data("bootstrapValidator");
        bootstrapValidator.validate();
        if(bootstrapValidator.isValid()) {
            $.ajax({
                type: type,
                dataType: "json",
                data: JSON.stringify(getApp()),
                url: "/api/app",
                contentType: "application/json",
                success: function(data) {
                    modal.modal("hide");
                    $("#app-table").bootstrapTable("refresh");
                    $(".modal-backdrop").remove();
                    $("body").removeClass("modal-open");
                    refreshAppNavTag();
                }
            });
        }
    });
}

function getApp() {
    return {
        appName: $("#app-name").val(),
        cpuCount: $("#cpu-count").val(),
        memoryMB: $("#app-memory").val(),
        bootstrapScript: $("#bootstrap-script").val(),
        appCacheEnable: $("#app-cache-enable").prop("checked"),
        appURL: $("#app-url").val(),
        eventTraceSamplingCount: $("#event-trace-sampling-count").val()
    };
}
