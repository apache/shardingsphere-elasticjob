$(function() {
    var jobName = getCurrentUrl("jobName");
    var serverIp = getCurrentUrl("serverIp");
    var statusPage = getCurrentUrl("statusPage");
    if (null !== statusPage) {
        $("#content").load("html/job/jobs_status.html");
        $("#job").addClass("active");
    } else if (null !== jobName) {
        $("#content").load("html/job/jobs_overview.html");
        $("#job").addClass("active");
    } else if (null !== serverIp) {
        $("#content").load("html/server/server_detail.html");
        $("#server").addClass("active");
    } else {
        $("#content").load("html/reg/registry_center.html");
        $("#settings").addClass("active");
    }
    $("#reg-center").click(function() {
        $("#content").load("html/reg/registry_center.html");
    });
    $("#event-trace-data-source").click(function() {
        $("#content").load("html/event/event_trace_data_source.html");
    });
    $("#job-dimension").click(function() {
        $("#content").load("html/job/jobs_overview.html");
    });
    $("#job-status").click(function() {
        $("#content").load("html/job/jobs_status_overview.html");
    });
    $("#server-status").click(function() {
        $("#content").load("html/server/servers_overview.html");
    });
    $("#exec-detail").click(function() {
        $("#content").load("html/history/job_exec_detail.html");
    });
    $("#exec-status").click(function() {
        $("#content").load("html/history/job_exec_status.html");
    });
    $("#help").click(function() {
        $("#content").load("html/help.html");
    });
});
