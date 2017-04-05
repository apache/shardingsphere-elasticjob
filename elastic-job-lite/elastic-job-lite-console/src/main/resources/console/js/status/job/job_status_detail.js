$(function() {
    $("#job-name").text($("#index-job-name").text());
    renderShardingTable();
    renderBreadCrumbMenu();
});

function renderShardingTable() {
    var jobName = $("#job-name").text();
    $("#sharding").bootstrapTable({
        url: "/api/jobs/" + jobName + "/sharding",
        cache: false,
        columns: [
            {
                field: "item",
                title: "分片项"
            }, {
                field: "serverIp",
                title: "服务器IP"
            }, {
                field: "status",
                title: "状态",
                formatter: "shardingStatusFormatter"
            }, {
                field: "failover",
                title: "失效转移",
                formatter: "failoverFormatter"
            }]
    });
}

function shardingStatusFormatter(value, row) {
    switch(value) {
        case "RUNNING":
            return "<span class='label label-primary'>运行中</span>";
            break;
        case "COMPLETED":
            return "<span class='label label-success'>已完成</span>";
            break;
        case "DISABLED":
            return "<span class='label label-warning'>禁用中</span>";
            break;
        default:
            return "-";
            break;
    }
}

function failoverFormatter(value, row) {
    return value ? "是" : "-";
}

function renderBreadCrumbMenu() {
    $("#breadcrumb-job").click(function() {
        $("#content").load("html/status/job/jobs_status_overview.html");
    });
}