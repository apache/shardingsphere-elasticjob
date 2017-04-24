$(function() {
    if(null !== getCurrentUrl("appName")) {
        $("#content-right").load("/html/app/apps_overview.html");
    }else{
        $("#content-right").load("/html/job/jobs_overview.html");
    }
    $("#register-app").click(function() {
        $("#content-right").load("/html/app/apps_overview.html");
    });
    $("#register-job").click(function() {
        $("#content-right").load("/html/job/jobs_overview.html");
    });
    $("#status").click(function() {
        $("#content-right").load("/html/job/job_status.html");
    });
    $("#dashboard").click(function() {
        $("#content-right").load("/html/history/job_dashboard.html");
    });
    $("#exec-details").click(function() {
        $("#content-right").load("/html/history/job_exec_details.html");
    });
    $("#exec-status").click(function() {
        $("#content-right").load("/html/history/job_exec_status.html");
    });
});

function getCurrentUrl(param) {
    var regular = new RegExp(param);
    var result = window.location.search.substr(1).match(regular);
    if (null !== result) {
        return unescape(result[1]);
    }
    return null;
}
