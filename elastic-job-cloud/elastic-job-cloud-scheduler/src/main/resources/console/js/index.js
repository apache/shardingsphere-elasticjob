$(function() {
    $("#content-right").load("/html/app/apps_overview.html");
    refreshJobNavTag();
    refreshAppNavTag();
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
