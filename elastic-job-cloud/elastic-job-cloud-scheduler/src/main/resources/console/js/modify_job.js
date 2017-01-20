$(function() {
    getJobResult();
    bootstrapValidator();
    submitBootstrapValidator();
    dataControl();
});
    
function getJobResult() {
    $.ajax({
        url:"/job/jobs/"+getJobName(),
        async: false,
        success:function(result){
            if(result.jobType == "SCRIPT"){
                $("#bootstrapScriptDiv").show();
            }
            else{
                $("#bootstrapScriptDiv").hide();
            }
            showJobSettingInfo(result);
        }
    });
}
    
function bindSubmitJobSettingsForm() {
    $.ajax({
        type: 'put',
        dataType:'json',
        data:JSON.stringify(dataInfo()),
        url:'/job/update',
        contentType: "application/json",
        success: function(data) {
            window.location="index.html";
        },
        error:function(XMLHttpRequest, textStatus, errorThrown){
            alert ("XMLHttpRequest.status="+XMLHttpRequest.status+"\ntextStatus="+textStatus+"\nerrorThrown=" + errorThrown);
        }
    });
}
    
function getJobName (){
    var name,value,jobName;
    var str=location.href; //取得整个地址栏
    var num=str.indexOf("?");
    str=str.substr(num+1); //取得所有参数   stringvar.substr(start [, length ]
    var arr=str.split("&"); //各个参数放到数组里
    for(var i=0;i < arr.length;i++){
        num=arr[i].indexOf("=");
        if(num>0){
            name=arr[i].substring(0,num);
            value=arr[i].substr(num+1);
            jobName=value;
            }
        }
    return jobName;
}
    
function showJobSettingInfo(result){
    $("#jobName").attr("value",result.jobName);
    $("#cron").attr("value",result.cron);
    $("#jobExecutionType").val(result.jobExecutionType);
    $("#shardingTotalCount").attr("value",result.shardingTotalCount);
    $("#jobParameter").attr("value",result.jobParameter);
    $("#cpuCount").attr("value",result.cpuCount); 
    $("#memoryMB").attr("value",result.memoryMB);
    $("#bootstrapScript").attr("value",result.bootstrapScript);
    $("#beanName").attr("value",result.beanName);
    $("#applicationContext").attr("value",result.applicationContext);
    $("#appURL").attr("value",result.appURL);
    $("#description").val(result.description);
    $("#shardingItemParameters").val(result.shardingItemParameters);
    $("#jobType").val(result.jobType);
    $("#scriptCommandLine").attr("value",result.scriptCommandLine);
    if(result.jobType =='SIMPLE'){
        $("#jobClass").attr("value",result.jobClass);
        $("#jobClassModel").show();
        $("#scriptCommandLine_text").hide();
        $("#streamingProcess").hide();
        $("#streamingProcess_box").hide();
        $("#bootstrapScriptDiv").hide();
    }else if(result.jobType =='DATAFLOW'){
        $("#jobClass").attr("value",result.jobClass);
        $("#jobClassModel").show();
        $("#streamingProcess").show();
        $("#streamingProcess_box").show();
        $("#scriptCommandLine_text").hide();
        $("#bootstrapScriptDiv").hide();
    }else if(result.jobType =='SCRIPT'){
        $("#jobClass").attr("");
        $("#jobClassModel").hide();
        $("#scriptCommandLine_text").show(); 
        $("#streamingProcess").hide();
        $("#streamingProcess_box").hide();
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