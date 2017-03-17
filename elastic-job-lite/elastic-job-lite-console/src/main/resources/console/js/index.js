$(function() {
    var jobName = getCurrentUrl("jobName");
    var serverIp = getCurrentUrl("serverIp");
    if (null !== jobName) {
        $("#content").load("html/job/job_detail.html");
        renderDashboardNav();
    } else if (null !== serverIp) {
        $("#content").load("html/server/server_detail.html");
        renderDashboardNav();
    } else {
        $("#content").load("html/reg/registry_center.html");
    }
    $("#registry-center").click(function() {
        $("#content").load("html/reg/registry_center.html");
        renderDashboardNav();
    });
    $("#exec-detail").click(function() {
        $("#content").load("html/event/job_exec_detail.html");
    });
    $("#exec-status").click(function() {
        $("#content").load("html/event/job_exec_status.html");
    });
    $("#registry-center-dimension").click(function() {
        $("#content").load("html/reg/registry_center.html");
        renderDashboardNav();
    });
    $("#data-source").click(function() {
        $("#content").load("html/event/data_source.html");
    });
    $("#data-source-dimension").click(function() {
        $("#content").load("html/event/data_source.html");
    });
    $("#overview").click(function() {
        $("#content").load("html/overview/overview.html");
        renderDashboardNav();
    });
    $("#jobs-dimension").click(function() {
        $("#content").load("html/job/job_detail.html");
    });
    $("#servers-dimension").click(function() {
        $("#content").load("html/server/server_detail.html");
    });
    $("#help").click(function() {
        $("#content").load("html/help.html");
    });
});

function renderDashboardNav() {
    renderJobsForDashboardNav();
    renderJServersForDashboardNav();
}
