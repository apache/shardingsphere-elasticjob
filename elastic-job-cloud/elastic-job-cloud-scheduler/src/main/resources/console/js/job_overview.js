function viewOper(val, row){
    var jobName = row.jobName;
    var result = '<button type="button" class="btn btn-success" onClick="detailJob(\'' + jobName + '\')">详情</button>'+ ' <a href="modify_job.html?jobName='+encodeURIComponent(jobName)+'" class="btn btn-info">修改</a>'+
    ' <button type="button" class="btn btn-danger" onClick="deleteJob(\'' + jobName + '\')">删除</button>';
    return result;
}
    
function deleteJob(jobName){
    $("#delete-data").modal();
    $('#deleteConfirm').on("click", function(){
        $.ajax({
            url:"/job/deregister",
            type:"DELETE",
            contentType: "application/json",
            data:jobName,
            success:function(result){
                $("#JobExecDetailTable").bootstrapTable('refresh');
                $("#delete-data").hide();
                }
        });
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
                $("#data-detail").modal();
            }else{
                alert("展示详情页失败！");
            }
        }
    });
}