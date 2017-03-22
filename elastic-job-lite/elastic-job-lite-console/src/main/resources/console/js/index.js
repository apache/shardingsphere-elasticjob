$(function() {
    var jobName = getCurrentUrl("jobName");
    var serverIp = getCurrentUrl("serverIp");
    var statusPage = getCurrentUrl("statusPage");
    if (null !== statusPage) {
        $("#content").load("html/status/job/job_status_detail.html");
        $("#job").addClass("active");
    } else if (null !== serverIp) {
        $("#content").load("html/status/server/server_status_detail.html");
        $("#server").addClass("active");
    } else if (null !== jobName) {
        $("#content").load("html/job/jobs_overview.html");
        $("#job").addClass("active");
    } else {
        $("#content").load("html/global/registry_center.html");
        $("#settings").addClass("active");
    }
    $("#reg-center").click(function() {
        $("#content").load("html/global/registry_center.html");
    });
    $("#event-trace-data-source").click(function() {
        $("#content").load("html/global/event_trace_data_source.html");
    });
    $("#job-config").click(function() {
        $("#content").load("html/job/jobs_overview.html");
    });
    $("#job-status").click(function() {
        $("#content").load("html/status/job/jobs_status_overview.html");
    });
    $("#server-status").click(function() {
        $("#content").load("html/status/server/servers_status_overview.html");
    });
    $("#event-trace-history").click(function() {
        $("#content").load("html/history/job_event_trace_history.html");
    });
    $("#status-history").click(function() {
        $("#content").load("html/history/job_status_history.html");
    });
    $("#help").click(function() {
        $("#content").load("html/help/help.html");
    });
});
