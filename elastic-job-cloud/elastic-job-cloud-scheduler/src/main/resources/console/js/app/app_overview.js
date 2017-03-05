function viewOperApp(val, row){
    var appName = row.appName;
    var result = '<button type="button" class="btn btn-info" onClick="detailApp(\'' + appName + '\')">详情</button>'+ ' <button type="button" class="btn btn-warning" onClick="getAppResult(\'' + appName + '\')">修改</button>';
    return result;
}
    
function detailApp(appName){
    $.ajax({
        url:"/app/"+appName,
        async: false,
        contentType: "application/json",
        success:function(result){
            if (null != result) {
                $("#AppDetailTable").bootstrapTable('removeAll');
                $("#AppDetailTable").bootstrapTable('append',result);
                $("#dataDetailApp").modal({backdrop: 'static', keyboard: true});
            }
        }
    });
}
    
$('#add_app').click(function(){
    $('#addAppBody').load('html/app/add_app.html');
    $('#dataAddApp').modal({backdrop: 'static', keyboard: true});
});
    
function getAppResult(appName) {
    $.ajax({
        url:"/app/"+appName,
        async: false,
        success:function(result){
            if(null !=result ){
                $('#updateAppBody').load('html/app/modify_app.html',null,function(){
                    showAppSettingInfo(result);
                    $("#dataUpdateApp").modal({backdrop: 'static', keyboard: true});
                });
            }
        }
    });
}
    
function showAppSettingInfo(result){
    $("#appName").attr("value",result.appName);
    $("#cpuCount").attr("value",result.cpuCount); 
    $("#memoryMB").attr("value",result.memoryMB);
    $("#bootstrapScript").attr("value",result.bootstrapScript);
    $("#appURL").attr("value",result.appURL);
    $("#eventTraceSamplingCount").val(result.eventTraceSamplingCount);
    if(result.appCacheEnable == true){
        $("#appCacheEnable").prop("checked",true);
    }else{
        $("#appCacheEnable").prop("checked",false);
    }
}