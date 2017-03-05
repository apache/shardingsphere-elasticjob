function viewOper(val, row){
    var jobName = row.jobName;
    var result = '<button type="button" class="btn btn-info" onClick="detailJob(\'' + jobName + '\')">详情</button>'+ ' <button type="button" class="btn btn-warning" onClick="updateJob(\'' + jobName + '\')">修改</button>'+
    ' <button type="button" class="btn btn-danger" onClick="deleteJob(\'' + jobName + '\')">删除</button>';
    return result;
}
    
function deleteJob(jobName){
    $("#delete-data").modal({backdrop: 'static', keyboard: true});
    var flag = true;
    $('#deleteRemove').on("click",function(){
        flag=false;
    });
    $('#deleteConfirm').on("click", function(){
        if(flag == true){
            $.ajax({
                url:"/job/deregister",
                type:"DELETE",
                contentType:"application/json",
                data:jobName,
                success:function(result){
                    $("#JobExecDetailTable").bootstrapTable('refresh');
                    $("#delete-data").hide();
                    }
            });
        }
    });
}
    
function detailJob(jobName){
    $.ajax({
        url:"/job/jobs/"+jobName,
        async: false,
        contentType: "application/json",
        success:function(result){
            if (null != result) {
                $("#JobDetailTable").bootstrapTable('removeAll');
                $("#JobDetailTable").bootstrapTable('append',result);
                if(result.streamingProcess == null){
                    $('#JobDetailTable').bootstrapTable('hideColumn', 'streamingProcess');
                }
                if(result.bootstrapScript == null){
                    $('#JobDetailTable').bootstrapTable('hideColumn', 'bootstrapScript');
                }
                if(result.scriptCommandLine == null){
                    $('#JobDetailTable').bootstrapTable('hideColumn', 'scriptCommandLine');
                }
                $("#dataDetail").modal({backdrop: 'static', keyboard: true});
            }else{
                alert("展示详情页失败！");
            }
        }
    });
}
    
$('#addJob').click(function() {
    $('#addJobBody').load('html/job/add_job.html');
    $('#dataAdd').modal({backdrop: 'static', keyboard: true});
});
    
function updateJob(jobName){
    $.ajax({
        url:"/job/jobs/"+jobName,
        async: false,
        success:function(result){
            if (null != result) {
                $('#updateJobBody').load('html/job/modify_job.html',null,function(){
                    if(result.jobType == "SCRIPT"){
                        $("#bootstrapScriptDiv").show();
                    }
                    else{
                        $("#bootstrapScriptDiv").hide();
                    }
                    showJobSettingInfo(result);
                    $('#dataUpdate').modal({backdrop: 'static', keyboard: true});
                });
            }else{
                alert("数据加载失败！");
            }
        }
    });
}
    
function showJobSettingInfo(result){
    $("#jobName").attr("value",result.jobName);
    $("#jobAppName").attr("value",result.appName);
    $("#cron").attr("value",result.cron);
    $("#jobExecutionType").val(result.jobExecutionType);
    $("#shardingTotalCount").attr("value",result.shardingTotalCount);
    $("#jobParameter").attr("value",result.jobParameter);
    $("#cpuCount").attr("value",result.cpuCount); 
    $("#memoryMB").attr("value",result.memoryMB);
    $("#bootstrapScript").attr("value",result.bootstrapScript);
    $("#beanName").attr("value",result.beanName);
    $("#applicationContext").attr("value",result.applicationContext);
    $("#description").val(result.description);
    $("#shardingItemParameters").val(result.shardingItemParameters);
    $("#jobType").val(result.jobType);
    $("#scriptCommandLine").attr("value",result.scriptCommandLine);
    if(result.jobType =='SIMPLE'){
        $("#jobClass").attr("value",result.jobClass);
        $("#jobClassModel").show();
        $("#scriptCommandLineText").hide();
        $("#streamingProcess").hide();
        $("#streamingProcessBox").hide();
        $("#bootstrapScriptDiv").hide();
    }else if(result.jobType =='DATAFLOW'){
        $("#jobClass").attr("value",result.jobClass);
        $("#jobClassModel").show();
        $("#streamingProcess").show();
        $("#streamingProcessBox").show();
        $("#scriptCommandLineText").hide();
        $("#bootstrapScriptDiv").hide();
    }else if(result.jobType =='SCRIPT'){
        $("#jobClass").attr("");
        $("#jobClassModel").hide();
        $("#scriptCommandLineText").show(); 
        $("#streamingProcess").hide();
        $("#streamingProcessBox").hide();
        $("#bootstrapScriptDiv").show();
    }
    if(result.failover == true){
        $("#failover").prop("checked",true);
    }else{
        $("#failover").prop("checked",false);
    }
    if(result.misfire == true){
        $("#misfire").prop("checked",true);
    }else{
        $("#misfire").prop("checked",false);
    }
    if(result.streamingProcess == true){
        $("#streamingProcess").prop("checked",true);
    }else{
        $("#streamingProcess").prop("checked",false);
    }
}