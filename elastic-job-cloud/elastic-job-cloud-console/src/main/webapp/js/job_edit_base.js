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
                        message: 'The jobClass is required and cannot be empty'
                    },
                    regexp: {
                        regexp: /^([a-zA-Z_][a-zA-Z0-9_]*\.)*[a-zA-Z_][a-zA-Z0-9_]*$/,
                        message: 'The jobClass is invalid'
                    }
                }
            },
            jobName: {
                validators: {
                    notEmpty: {
                        message: 'The jobName is required and cannot be empty'
                    },
                    stringLength: {
                        max: 100,
                        message: 'The jobName must be less than 100 characters long'
                    }
                }
            },
            cron: {
                validators: {
                    notEmpty: {
                        message: 'The cron is required and cannot be empty'
                    }
                }
            },
            cpuCount: {
                validators: {
                    notEmpty: {
                        message: 'The cpuCount is required and cannot be empty'
                    },
                    regexp: {
                        regexp: /^(-?\d+)(\.\d+)?$/,
                        message: 'The cpuCount can only consist of number'
                    }
                }
            },
            appURL: {
                validators: {
                    notEmpty: {
                        message: 'The appURL is required and cannot be empty'
                    }
                }
            },
            bootstrapScript : {
                validators: {
                    notEmpty: {
                        message: 'The bootstrapScript is required and cannot be empty'
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
            shardingItemParameters: {
                validators: {
                    regexp: {
                        regexp: /^(\d+)=([a-zA-Z_\u4e00-\u9fa5]+)(,(\d+)=([a-zA-Z_\u4e00-\u9fa5]+))*$/,
                        message: 'The shardingItemParameters is invalid'
                    }
                }
            }
        }
    });
    $("#job-settings-form").submit(function(ev){
        ev.preventDefault();
    });
}
    
function submitBootstrapValidator(){
    $("#save_form").on("click", function(){
        var bootstrapValidator = $("#job-settings-form").data('bootstrapValidator');
        bootstrapValidator.validate();
        if(bootstrapValidator.isValid()){
            var beanName = $("#beanName").val();
            var applicationContext = $("#applicationContext").val();
            if(beanName.length == 0 && applicationContext.length == 0){
                bindSubmitJobSettingsForm();
            }else if(null != applicationContext && beanName.length == 0){
                    alert("Spring方式配置请填写 beanName");
            }else if(null != beanName && applicationContext.length == 0){
                alert("Spring方式配置请填写 Spring配置文件相对路径及名称");
            }else{
                bindSubmitJobSettingsForm();
            }
        }
    });
}
    
function dataControl(){
    $('#jobType').change(function() {
        var jobType = $('#jobType').val();
        if(jobType =='SIMPLE'){ 
            $("#scriptCommandLine").attr("disabled","disabled");
            $("#streamingProcess").hide();
            $("#streamingProcess_box").hide();
        }else if(jobType =='DATAFLOW'){ 
            $("#streamingProcess").show();
            $("#streamingProcess_box").show();
            $("#scriptCommandLine").attr("disabled","disabled");
        }else if(jobType =='SCRIPT'){
            $("#scriptCommandLine").removeAttr("disabled"); 
            $("#streamingProcess").hide();
            $("#streamingProcess_box").hide();
        }
    });
}

function dataInfo(){
    return {
        "jobName":$("#jobName").val(),
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
        "appURL":$("#appURL").val(),
        "applicationContext":$("#applicationContext").val(),
        "shardingItemParameters": $("#shardingItemParameters").val(),
        "scriptCommandLine":$("#scriptCommandLine").val(),
        "description":$("#description").val()
    };
}
