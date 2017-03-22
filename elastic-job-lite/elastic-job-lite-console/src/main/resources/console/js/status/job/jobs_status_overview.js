$(function() {
    renderJobsOverview();
    bindStatusButtons();
});

function renderJobsOverview() {
    $("#jobs-status-overview").bootstrapTable({
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
            title: "作业名"
        }, {
            field: "status",
            title: "运行状态"
        }, {
            field: "description",
            title: "描述"
        }, {
            fidle: "operation",
            title: "操作",
            formatter: "generateOperationButtons"
        }]
    });
}

function generateOperationButtons(val, row) {
    return "<button operation='job-status' class='btn-xs btn-info' jobName='" + row.jobName + "'>状态</button>";
}

function bindStatusButtons() {
    $(document).on("click", "button[operation='job-status'][data-toggle!='modal']", function(event) {
        var jobName = $(event.currentTarget).attr("jobName");
        window.location = "index.html?jobName=" + jobName + "&statusPage=show";
    });
}
