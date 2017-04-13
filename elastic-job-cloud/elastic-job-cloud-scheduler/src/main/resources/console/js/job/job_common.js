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
                        message: "作业实现类不能为空"
                    },
                    regexp: {
                        regexp: /^([a-zA-Z_][a-zA-Z0-9_]*\.)*[a-zA-Z_][a-zA-Z0-9_]*$/,
                        message: "作业实现类不能包含非法字符"
                    }
                }
            },
            jobName: {
                jobNameCheck: true,
                validators: {
                    notEmpty: {
                        message: "作业名称不能为空"
                    },
                    stringLength: {
                        max: 100,
                        message: "作业名称长度不能超过100字符大小"
                    },
                    regexp: {
                        regexp: /^([a-zA-Z_][a-zA-Z0-9_]*\.)*[a-zA-Z_][a-zA-Z0-9_]*$/,
                        message: "作业名称包含非法字符"
                    },
                    callback: {
                        message: "作业名称已经注册",
                        callback: function () {
                            var jobName = $("#job-name").val();
                            var result = true;
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
                            return result;
                        }
                    }
                }
            },
            jobAppName: {
                validators: {
                    callback: {
                        message: "应用未注册",
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
                        message: "cron表达式不能超过40字符大小"
                    },
                    notEmpty: {
                        message: "cron表达式不能为空"
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
            jobMemory: {
                validators: {
                    notEmpty: {
                        message: "单片作业内存不能为空"
                    }
                }
            },
            shardingTotalCount: {
                validators: {
                    notEmpty: {
                        message: "分片数不能为空"
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
                        message: "SCRIPT类型作业命令行执行脚本不能为空"
                    }
                }
            },
            shardingItemParameters: {
                validators: {
                    regexp: {
                        regexp: /^(\d+)=([a-zA-Z0-9_\u4e00-\u9fa5]+)(,(\d+)=([a-zA-Z0-9_\u4e00-\u9fa5]+))*$/,
                        message: "作业分片项的格式不正确"
                    },
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

function submitConfirm(type, url) {
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
                submitJobForm(type,url);
            } else if(null !== applicationContext && 0 === beanName.length) {
                $("#delete-data—beanName").modal();
                setTimeout(function() {
                    $("#delete-data—beanName").modal("hide");
                }, 3000); 
            } else if(null !== beanName && 0 === applicationContext.length) {
                $("#delete-data-applicationContext").modal();
                setTimeout(function() {
                    $("#delete-data-applicationContext").modal("hide");
                }, 3000);
            } else {
                submitJobForm(type, url);
            }
        }
    });
}

function submitJobForm(type, url) {
    $.ajax({
        type: type,
        dataType: "json",
        data: JSON.stringify(getJob()),
        url: url,
        contentType: "application/json",
        success: function(data) {
            window.location = "index.html";
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
        description: $("#description").val()
    };
}
