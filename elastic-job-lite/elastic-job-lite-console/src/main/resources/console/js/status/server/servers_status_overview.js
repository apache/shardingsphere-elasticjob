$(function() {
    renderServersOverview();
    bindStatusButtons();
});

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
            title: "服务器IP"
        }, {
            field: "serverHostName",
            title: "服务器名"
        }, {
            field: "status",
            title: "状态"
        }, {
            fidle: "operation",
            title: "操作",
            formatter: "generateOperationButtons"
        }]
    });
}

function generateOperationButtons(val, row) {
    return "<button operation='server-status' class='btn-xs btn-info' serverIp='" + row.serverIp + "'>状态</button>";
}

function bindStatusButtons() {
    $(document).on("click", "button[operation='server-status'][data-toggle!='modal']", function(event) {
        var serverIp = $(event.currentTarget).attr("serverIp");
        window.location = "index.html?serverIp=" + serverIp;
    });
}
