$(document).ready(function() {
    var jobName = GetQueryParam("jobName");
    var serverIp = GetQueryParam("serverIp");
    if(null != jobName){
        $('#content').load('job_detail.html');
    }
    else if(null != serverIp){
        $('#content').load('server_detail.html');
    }
    else{
        $('#content').load('registry_center.html');
        renderRegistryCenterForDashboardNav();
        renderJobsForDashboardNav();
        renderJServersForDashboardNav();
    }
    $('#registry-center-dimension').click(function() {
        $('#content').load('registry_center.html');
    });
    $('#overview').click(function() {
        $('#content').load('overview.html');
    });
    $('#jobs-dimension').click(function() {
        $('#content').load('job_detail.html');
    });
    $('#servers-dimension').click(function() {
        $('#content').load('server_detail.html');
    });
    $('#help').click(function() {
        $('#content').load('help.html');
    });
});
