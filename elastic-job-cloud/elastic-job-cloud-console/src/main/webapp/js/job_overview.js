function viewOper(val, row){
    var jobName = row.jobName;
    var result = '<button type="button" class="btn btn-success" onClick="detailJob(\'' + jobName + '\')">详情</button>'+ ' <a href="modify_job?jobName='+encodeURIComponent(jobName)+'" class="btn btn-info">修改</a>'+
    ' <button type="button" class="btn btn-danger" onClick="deleteJob(\'' + jobName + '\')">删除</button>';
    return result;
}
    
function deleteJob(jobName){
    event.returnValue = confirm("确认要删除吗？");
    if(event.returnValue == true){
        $.ajax({
            url:"/job/deregister",
            type:"DELETE",
            contentType: "application/json",
            data:jobName,
            success:function(result){
                alert("删除任务成功！");
                $("#JobExecDetailTable").bootstrapTable('refresh');
                }
        });
    }
}
    
function detailJob(jobName){
    $.ajax({
        url:"/job/jobs/"+jobName,
        async: false,
        success:function(result){
            if (null != result) {
                showJobDetail(result);
            }else{
                alert("展示详情页失败！");
            }
        }
    });
}
    
function closeModal(){
    $("#detail-data").hide();
}
    
function showJobDetail(result){
    $("#jobName").attr("value",result.jobName);
    $("#cron").attr("value",result.cron);
    $("#jobClass").attr("value",result.jobClass);
    $("#jobType").attr("value",result.jobType);
    $("#jobExecutionType").attr("value",result.jobExecutionType);
    $("#shardingTotalCount").attr("value",result.shardingTotalCount);
    $("#cpuCount").attr("value",result.cpuCount);
    $("#memoryMB").attr("value",result.memoryMB);
    $("#bootstrapScript").attr("value",result.bootstrapScript);
    $("#beanName").attr("value",result.beanName);
    $("#applicationContext").attr("value",result.applicationContext);
    $("#description").attr("value",result.description);
    $("#jobParameter").attr("value",result.jobParameter);
    $("#appURL").attr("value",result.appURL);
    $("#shardingItemParameters").attr("value",result.shardingItemParameters);
    $("#scriptCommandLine").attr("value",result.scriptCommandLine);
    if(result.failover == true){
        $("#failover").attr("value","支持");
    }else{
        $("#failover").attr("value","不支持");
    }
    if(result.misfire == true){
        $("#misfire").attr("value","支持");
    }else{
        $("#misfire").attr("value","不支持");
    }
    if(result.streamingProcess == true){
        $("#streamingProcess").attr("value","支持");
    }else{
        $("#streamingProcess").attr("value","不支持");
    }
    $("#detail-data").modal();
}