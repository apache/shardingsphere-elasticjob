function validate() {
    $("#job-form").bootstrapValidator({
        message: "This value is not valid",
        feedbackIcons: {
            valid: "glyphicon glyphicon-ok",
            invalid: "glyphicon glyphicon-remove",
            validating: "glyphicon glyphicon-refresh"
        },
        fields: {
            jobClass: {
                validators: {
                    notEmpty: {
                        message: $.i18n.prop("job-class-not-null")
                    },
                    regexp: {
                        regexp: /^[\w\.]+$/,
                        message: $.i18n.prop("job-class-regexp-limit")
                    }
                }
            },
            jobName: {
                jobNameCheck: true,
                validators: {
                    notEmpty: {
                        message: $.i18n.prop("job-name-not-null")
                    },
                    stringLength: {
                        max: 100,
                        message: $.i18n.prop("job-name-length-limit")
                    },
                    callback: {
                        message: $.i18n.prop("job-name-exists"),
                        callback: function () {
                            var jobName = $("#job-name").val();
                            var result = true;
                            if ("" !== jobName) {
                                $.ajax({
                                    url: "/api/job/jobs/" + jobName,
                                    contentType: "application/json",
                                    async: false,
                                    success: function(data) {
                                        if (null !== data) {
                                            result = false;
                                        }
                                    }
                                });
                            }
                            return result;
                        }
                    }
                }
            },
            jobAppName: {
                validators: {
                    callback: {
                        message: $.i18n.prop("app-name-unregistered"),
                        callback: function (validator) {
                            var appName = $("#job-app-name").val();
                            var result = false;
                                $.ajax({
                                    url: "/api/app/" + appName,
                                    contentType: "application/json",
                                    async: false,
                                    success: function(data) {
                                        if (null !== data) {
                                            result = true;
                                        }
                                    }
                                });
                            return result;
                        }
                    }
                }
            },
            cron: {
                validators: {
                    stringLength: {
                        max: 40,
                        message: $.i18n.prop("job-cron-length-limit")
                    },
                    notEmpty: {
                        message: $.i18n.prop("job-cron-not-null")
                    }
                }
            },
            cpuCount: {
                validators: {
                    notEmpty: {
                        message: $.i18n.prop("job-cpu-count-not-null")
                    },
                    regexp: {
                        regexp: /^(-?\d+)(\.\d+)?$/,
                        message: $.i18n.prop("job-cpu-count-regexp-limit")
                    }
                }
            },
            jobMemory: {
                validators: {
                    notEmpty: {
                        message: $.i18n.prop("job-memory-not-null")
                    }
                }
            },
            shardingTotalCount: {
                validators: {
                    notEmpty: {
                        message: $.i18n.prop("job-sharding-count-not-null")
                    }
                }
            },
            beanName: {
                validators: {
                }
            },
            applicationContext: {
                validators: {
                }
            },
            scriptCommandLine: {
                validators: {
                    notEmpty: {
                        message: $.i18n.prop("job-script-command-line-not-null")
                    }
                }
            },
            shardingItemParameters: {
                validators: {
                    regexp: {
                        regexp: /^(\d+)=(\w+)(,(\d+)=(\w+))*$/,
                        message: $.i18n.prop("job-sharding-item-parameters-regexp-limit")
                    }
                }
            }
        }
    });
}

$("#sharding-item-parameters").blur(function() {
    if($("" == "#sharding-item-parameters").val()) {
        $("#job-form").data("bootstrapValidator").enableFieldValidators("shardingItemParameters", false);
    } else {
        $("#job-form").data("bootstrapValidator").enableFieldValidators("shardingItemParameters", true);
    }
});

$("#sharding-item-parameters").focus(function() {
    $("#job-form").data('bootstrapValidator').enableFieldValidators("shardingItemParameters", true);
});

function submitConfirm(type, url, modal) {
    $("#save-button").on("click", function() {
        if($("" == "#sharding-item-parameters").val() || null === $("#sharding-item-parameters").val()) {
            $("#job-form").data("bootstrapValidator").enableFieldValidators("shardingItemParameters", false);
        }
        var bootstrapValidator = $("#job-form").data("bootstrapValidator");
        bootstrapValidator.validate();
        if(bootstrapValidator.isValid()) {
            var beanName = $("#bean-name").val();
            var applicationContext = $("#application-context").val();
            if(0 === beanName.length && 0 === applicationContext.length) {
                submitJobForm(type, url, modal);
            } else if(null !== applicationContext && 0 === beanName.length) {
                $("#delete-data—bean-name").modal();
                setTimeout(function() {
                    $("#delete-data—bean-name").modal("hide");
                }, 3000); 
            } else if(null !== beanName && 0 === applicationContext.length) {
                $("#delete-data-application-context").modal();
                setTimeout(function() {
                    $("#delete-data-application-context").modal("hide");
                }, 3000);
            } else {
                submitJobForm(type, url, modal);
            }
        }
    });
}

function submitJobForm(type, url, modal) {
    $.ajax({
        type: type,
        dataType: "json",
        data: JSON.stringify(getJob()),
        url: url,
        contentType: "application/json",
        success: function(data) {
            modal.modal("hide");
            $(".modal-backdrop").remove();
            $("body").removeClass("modal-open");
            $("#content-right").load("/html/job/jobs_overview.html");
            refreshJobNavTag();
        }
    });
}

function dataControl() {
    $("#job-type").change(function() {
        var jobType = $("#job-type").val();
        if("SIMPLE" === jobType) {
            $("#job-class-model").show();
            $("#streaming-process").hide();
            $("#streaming-process-box").hide();
            $("#bootstrap-script-div").hide();
        } else if("DATAFLOW" === jobType) {
            $("#job-class-model").show();
            $("#streaming-process").show();
            $("#streaming-process-box").show();
            $("#bootstrap-script-div").hide();
        } else if("SCRIPT" === jobType) {
            $("#job-class-model").hide();
            $("#streaming-process").hide();
            $("#streaming-process-box").hide();
            $("#bootstrap-script-div").show();
        }
    });
}

function getJob() {
    return {
        jobName: $("#job-name").val(),
        appName: $("#job-app-name").val(),
        jobClass: $("#job-class").val(),
        cron: $("#cron").val(),
        jobType: $("#job-type").val(),
        cpuCount: $("#cpu-count").val(),
        jobExecutionType: $("#job-execution-type").val(),
        memoryMB: $("#job-memory").val(),
        bootstrapScript: $("#bootstrap-script").val(),
        beanName: $("#bean-name").val(),
        shardingTotalCount: $("#sharding-total-count").val(),
        jobParameter: $("#job-parameter").val(),
        failover: $("#failover").prop("checked"),
        misfire: $("#misfire").prop("checked"),
        streamingProcess: $("#streaming-process").prop("checked"),
        applicationContext: $("#application-context").val(),
        shardingItemParameters: $("#sharding-item-parameters").val(),
        scriptCommandLine: $("#script-command-line").val(),
        description: $("#description").val()
    };
}
