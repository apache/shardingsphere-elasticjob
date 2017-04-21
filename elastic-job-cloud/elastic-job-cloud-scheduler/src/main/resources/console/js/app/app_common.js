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
                        message: "应用名称不能为空"
                    },
                    stringLength: {
                        max: 100,
                        message: "应用名称长度不能超过100字符大小"
                    },
                    regexp: {
                        regexp: /^[\w\.-]+$/,
                        message: "应用名称只能使用数字、字母、下划线(_)、短横线(-)和点号(.)"
                    },
                    callback: {
                        message: "应用已经注册",
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
                        message: "启动脚本不能为空"
                    }
                }
            },
            cpuCount: {
                validators: {
                    notEmpty: {
                        message: "cpu数量不能为空"
                    },
                    regexp: {
                        regexp: /^(-?\d+)(\.\d+)?$/,
                        message: "cpu数量只能包含数字和小数点"
                    }
                }
            },
            appMemory: {
                validators: {
                    notEmpty: {
                        message: "单片内存不能为空"
                    }
                }
            },
            eventTraceSamplingCount: {
                validators: {
                    notEmpty: {
                        message: "作业事件采样次数(Daemon)不能为空"
                    }
                }
            },
            appURL: {
                validators: {
                    notEmpty: {
                        message: "应用所在路径不能为空"
                    }
                }
            },
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
