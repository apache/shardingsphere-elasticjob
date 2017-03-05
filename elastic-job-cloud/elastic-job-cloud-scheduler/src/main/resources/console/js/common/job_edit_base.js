function bootstrapValidator(){
    $('#job-settings-form').bootstrapValidator({
        message: 'This value is not valid',
        feedbackIcons: {
            valid: 'glyphicon glyphicon-ok',
            invalid: 'glyphicon glyphicon-remove',
            validating: 'glyphicon glyphicon-refresh'
        },
        submitHandler: function(validator, form, submitButton) {
            $.post(form.attr('action'), form.serialize(), function(result) {
                if (result.valid == true || result.valid == 'true') {
                    $('#job-settings-form').bootstrapValidator('disableSubmitButtons', true);
                }
                else {
                    $('#job-settings-form').bootstrapValidator('disableSubmitButtons', false);
                }
            }, 'json');
        },
        fields: {
            jobClass: {
                validators: {
                    notEmpty: {
                        message: '作业实现类不能为空'
                    },
                    regexp: {
                        regexp: /^([a-zA-Z_][a-zA-Z0-9_]*\.)*[a-zA-Z_][a-zA-Z0-9_]*$/,
                        message: '作业实现类不能包含非法字符'
                    }
                }
            },
            jobName: {
                jobNameCheck: true,
                validators: {
                    notEmpty: {
                        message: '作业名称不能为空'
                    },
                    stringLength: {
                        max: 100,
                        message: '作业名称长度不能超过100字符大小'
                    },
                    regexp: {
                        regexp: /^([a-zA-Z_][a-zA-Z0-9_]*\.)*[a-zA-Z_][a-zA-Z0-9_]*$/,
                        message: '作业名称包含非法字符'
                    },
                    callback: {
                        message: '作业已经注册',
                        callback: function () {
                            var jobName = $('#jobName').val();
                            var res = true;
                                $.ajax({
                                    url: '/job/jobs/'+jobName,
                                    contentType: "application/json",
                                    async: false,
                                    success: function (data) {
                                        if (data !== null) {
                                            res = false;
                                        }
                                    }
                                });
                            return res;
                        }
                    }
                }
            },
            appName: {
                validators: {
                    notEmpty: {
                        message: '作业app名称不能为空'
                    },
                    stringLength: {
                        max: 100,
                        message: '作业app名称长度不能超过100字符大小'
                    },
                    regexp: {
                        regexp: /[a-zA-Z0-9_-][a-zA-Z_.]*$/,
                        message: '作业app名称包含非法字符'
                    },
                    callback: {
                        message: '应用已经注册',
                        callback: function () {
                            var appName = $('#appName').val();
                            var res = true;
                                $.ajax({
                                    url: '/app/'+appName,
                                    contentType: "application/json",
                                    async: false,
                                    success: function (data) {
                                        if (data !== null) {
                                            res = false;
                                        }
                                    }
                                });
                            return res;
                        }
                    }
                }
            },
            jobAppName: {
                validators: {
                    callback: {
                        message: '应用未注册',
                        callback: function (validator) {
                            var appName = $('#jobAppName').val();
                            var res = false;
                                $.ajax({
                                    url: '/app/'+appName,
                                    contentType: "application/json",
                                    async: false,
                                    success: function (data) {
                                        if (data !== null) {
                                            res = true;
                                        }
                                    }
                                });
                            return res;
                        }
                    }
                }
            },
            cron: {
                validators: {
                    notEmpty: {
                        message: 'cron表达式不能为空'
                    }
                }
            },
            cpuCount: {
                validators: {
                    notEmpty: {
                        message: 'cpu数量不能为空'
                    },
                    regexp: {
                        regexp: /^(-?\d+)(\.\d+)?$/,
                        message: 'cpu数量只能包含数字和小数点'
                    }
                }
            },
            memoryMB: {
                validators: {
                    notEmpty: {
                        message: '单片作业内存不能为空'
                    }
                }
            },
            shardingTotalCount: {
                validators: {
                    notEmpty: {
                        message: '作业分片数不能为空'
                    }
                }
            },
            appURL: {
                validators: {
                    notEmpty: {
                        message: '应用所在路径不能为空'
                    }
                }
            },
            bootstrapScript : {
                validators: {
                    notEmpty: {
                        message: '启动脚本不能为空'
                    }
                }
            },
            beanName : {
                validators: {
                }
            },
            applicationContext : {
                validators: {
                }
            },
            scriptCommandLine : {
                validators: {
                    notEmpty: {
                        message: 'SCRIPT类型作业命令行执行脚本不能为空'
                    }
                }
            },
            shardingItemParameters: {
                validators: {
                    regexp: {
                        regexp: /^(\d+)=([a-zA-Z0-9_\u4e00-\u9fa5]+)(,(\d+)=([a-zA-Z0-9_\u4e00-\u9fa5]+))*$/,
                        message: '作业分片项的格式不正确'
                    },
                }
            }
        }
    });
    $("#job-settings-form").submit(function(ev){
        ev.preventDefault();
    });
}
    
$("#shardingItemParameters").blur(function(){
    if($("#shardingItemParameters").val() == ''){
        $('#job-settings-form').data('bootstrapValidator').enableFieldValidators('shardingItemParameters', false);
    }
    else{
        $('#job-settings-form').data('bootstrapValidator').enableFieldValidators('shardingItemParameters', true);
    }
});
    
$("#shardingItemParameters").focus(function(){
    $('#job-settings-form').data('bootstrapValidator').enableFieldValidators('shardingItemParameters', true);
});
    
function submitBootstrapValidator(){
    $("#save_form").on("click", function(){
        if($('#shardingItemParameters').val() == '' || null == $('#shardingItemParameters').val()){
            $('#job-settings-form').data('bootstrapValidator').enableFieldValidators('shardingItemParameters', false);
        }
        var bootstrapValidator = $("#job-settings-form").data('bootstrapValidator');
        bootstrapValidator.validate();
        if(bootstrapValidator.isValid()){
            var beanName = $("#beanName").val();
            var applicationContext = $("#applicationContext").val();
            if(beanName.length == 0 && applicationContext.length == 0){
                bindSubmitJobSettingsForm();
            }else if(null != applicationContext && beanName.length == 0){
                $("#delete-data—beanName").modal();
                setTimeout(function(){
                    $("#delete-data—beanName").modal("hide")
                },2000); 
            }else if(null != beanName && applicationContext.length == 0){
                $("#delete-data-applicationContext").modal();
                setTimeout(function(){
                    $("#delete-data-applicationContext").modal("hide")
                },2000);
            }else{
                bindSubmitJobSettingsForm();
            }
        }
    });
}
    
function submitAppBootstrapValidator(){
    $("#save_form").on("click", function(){
        var bootstrapValidator = $("#job-settings-form").data('bootstrapValidator');
        bootstrapValidator.validate();
        if(bootstrapValidator.isValid()){
            bindSubmitAppSettingsForm();
        }
    });
}
    
function dataControl(){
    $('#jobType').change(function() {
        var jobType = $('#jobType').val();
        if(jobType =='SIMPLE'){ 
            $("#jobClassModel").show();
            $("#scriptCommandLineText").hide();
            $("#streamingProcess").hide();
            $("#streamingProcessBox").hide();
            $("#bootstrapScriptDiv").hide();
        }else if(jobType =='DATAFLOW'){
            $("#jobClassModel").show();
            $("#streamingProcess").show();
            $("#streamingProcessBox").show();
            $("#scriptCommandLineText").hide();
            $("#bootstrapScriptDiv").hide();
        }else if(jobType =='SCRIPT'){
            $("#jobClassModel").hide();
            $("#scriptCommandLineText").show(); 
            $("#streamingProcess").hide();
            $("#streamingProcessBox").hide();
            $("#bootstrapScriptDiv").show();
        }
    });
}
    
function dataInfo(){
    return {
        "jobName":$("#jobName").val(),
        "appName":$("#jobAppName").val(),
        "jobClass":$("#jobClass").val(),
        "cron":$("#cron").val(),
        "jobType":$("#jobType").val(),
        "cpuCount":$("#cpuCount").val(),
        "jobExecutionType":$("#jobExecutionType").val(),
        "memoryMB":$("#memoryMB").val(),
        "bootstrapScript":$("#bootstrapScript").val(),
        "beanName":$("#beanName").val(),
        "shardingTotalCount":$("#shardingTotalCount").val(),
        "jobParameter":$("#jobParameter").val(),
        "failover":$("#failover").prop("checked"),
        "misfire":$("#misfire").prop("checked"),
        "streamingProcess":$("#streamingProcess").prop("checked"),
        "applicationContext":$("#applicationContext").val(),
        "shardingItemParameters": $("#shardingItemParameters").val(),
        "scriptCommandLine":$("#scriptCommandLine").val(),
        "description":$("#description").val()
    };
}
    
function dataAppInfo(){
    return {
        "appName":$("#appName").val(),
        "cpuCount":$("#cpuCount").val(),
        "memoryMB":$("#memoryMB").val(),
        "bootstrapScript":$("#bootstrapScript").val(),
        "appCacheEnable":$("#appCacheEnable").prop("checked"),
        "appURL":$("#appURL").val(),
        "eventTraceSamplingCount":$("#eventTraceSamplingCount").val()
    };
}