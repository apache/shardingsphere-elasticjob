$(function() {
    renderJobsOverview();
    renderServersOverview();
});

function renderJobsOverview() {
    $("#jobs-overview-tbl").bootstrapTable({
        url: "/api/job/jobs",
        method: "get",
        cache: false,
        rowStyle: function (row, index) {
            var strclass = "";
            if ("OK" === row.status) {
                strclass = "success";
            } else if ("MANUALLY_DISABLED" === row.status) {
                strclass = "info";
            } else if ("PARTIAL_ALIVE" === row.status) {
                strclass = "warning";
            } else if ("ALL_CRASHED" === row.status) {
                strclass = "danger";
            } else {
                return {};
            }
            return { classes: strclass }
        },
        columns: 
        [{
            field: "jobName",
            title: "作业名",
            formatter: "jobFormatter"
        }, {
            field: "status",
            title: "运行状态"
        }, {
            field: "cron",
            title: "cron表达式"
        }, {
            field: "description",
            title: "描述"
        }]
    });
}

function jobFormatter(val, row) {
    return "<a href='index.html?jobName=" + row.jobName + "&jobType=" + row.jobtype + "'>" + row.jobName + "</a>";
}

function renderServersOverview() {
    $("#servers-overview-tbl").bootstrapTable({
        url: "/api/server/servers",
        method: "get",
        cache: false,
        rowStyle: function (row, index) {
            var strclass = "";
            if ("OK" === row.status) {
                strclass = "success";
            } else if ("PARTIAL_ALIVE" === row.status) {
                strclass = "warning";
            } else if ("ALL_CRASHED" === row.status) {
                strclass = "danger";
            } else {
                return {};
            }
            return { classes: strclass }
        },
        columns: 
        [{
            field: "serverIp",
            title: "服务器IP",
            formatter: "serverFormatter"
        }, {
            field: "serverHostName",
            title: "服务器名"
        }, {
            field: "status",
            title: "状态"
        }]
    });
}

function serverFormatter(val, row) {
    return "<a href='index.html?serverIp=" + row.serverIp + "'>" + row.serverIp + "</a>";
}
