/*
 * Copyright 1999-2015 dangdang.com.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

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
    $("#jobClass").attr("value",result.jobClass);
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
    if(result.jobType =='SIMPLE'){  
        $("#scriptCommandLine").attr("disabled","disabled");
        $("#streamingProcess").hide();
        $("#streamingProcess_box").hide();
    }else if(result.jobType =='DATAFLOW'){  
        $("#streamingProcess").show();
        $("#streamingProcess_box").show();
        $("#scriptCommandLine").attr("disabled","disabled");
    }else if(result.jobType =='SCRIPT'){
        $("#scriptCommandLine").removeAttr("disabled"); 
        $("#streamingProcess").hide();
        $("#streamingProcess_box").hide();
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