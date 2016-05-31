$(function() {
    renderJobsOverview();
    renderServersOverview();
});

function renderJobsOverview() {
    $.get("job/jobs", {}, function (data) {
        $("#jobs-overview-tbl tbody").empty();
        for (var i = 0;i < data.length;i++) {
            var status = data[i].status;
            var baseTd = "<td>" + "<a href='job_detail?jobName=" + data[i].jobName + "&jobType=" + data[i].jobType + "'>" + data[i].jobName + "</a>" + "</td><td>" + status + "</td><td>" + data[i].cron + "</td><td>" + data[i].description + "</td>";
            var trClass = "";
            if ("OK" === status) {
                trClass = "success";
            } else if ("MANUALLY_DISABLED" === status) {
                trClass = "info";
            } else if ("PARTIAL_ALIVE" === status) {
                trClass = "warning";
            } else if ("ALL_CRASHED" === status) {
                trClass = "danger";
            }
            $("#jobs-overview-tbl tbody").append("<tr class='" + trClass + "'>" + baseTd + "</tr>");
        }
    });
}

function renderServersOverview() {
    $.get("server/servers", {}, function (data) {
        $("#servers-overview-tbl tbody").empty();
        for (var i = 0;i < data.length;i++) {
            var status = data[i].status;
            var baseTd = "<td>" + "<a href='server_detail?serverIp=" + data[i].serverIp + "'>" + data[i].serverIp + "</a>" + "</td><td>" + data[i].serverHostName + "</td><td>" + status + "</td>";
            var trClass = "";
            if ("OK" === status) {
                trClass = "success";
            } else if ("PARTIAL_ALIVE" === status) {
                trClass = "warning";
            } else if ("ALL_CRASHED" === status) {
                trClass = "danger";
            }
            $("#servers-overview-tbl tbody").append("<tr class='" + trClass + "'>" + baseTd + "</tr>");
        }
    });
}
