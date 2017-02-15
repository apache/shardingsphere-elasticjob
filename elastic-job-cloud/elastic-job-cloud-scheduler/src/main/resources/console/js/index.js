$(document).ready(function() {
    if(getUrlData() != null){
        $('#contentRight').load('/html/app/app_overview.html');
    }else{
        $('#contentRight').load('/html/job/job_overview.html');
    }
    $('#registerApp').click(function() {
        $('#contentRight').load('/html/app/app_overview.html');
    });
    $('#registerJob').click(function() {
        $('#contentRight').load('/html/job/job_overview.html');
    });
    $('#status').click(function() {
        $('#contentRight').load('/html/job/job_status.html');
    });
    $('#dashboard').click(function() {
        $('#contentRight').load('/html/history/job_dashboard.html');
    });
    $('#execDetail').click(function() {
        $('#contentRight').load('/html/history/job_exec_detail.html');
    });
    $('#execStatus').click(function() {
        $('#contentRight').load('/html/history/job_exec_status.html');
    });
});

function getUrlData(){
    var name,value;
    var str=location.href; //取得整个地址栏
    var num=str.indexOf("?");
    str=str.substr(num+1); //取得所有参数   stringvar.substr(start [, length ]
    var arr=str.split("&"); //各个参数放到数组里
    for(var i=0;i < arr.length;i++){
        num=arr[i].indexOf("=");
        if(num>0){
           name=arr[i].substring(0,num);
           value=arr[i].substr(num+1);
         }
     }
    return value;
}