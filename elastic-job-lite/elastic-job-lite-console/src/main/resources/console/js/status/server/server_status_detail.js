$(function() {
    $("#server-ip").text($("#index-server-ip").text());
    renderJobs();
    bindTriggerButtons();
    bindShutdownButtons();
    bindRemoveButtons();
    renderBreadCrumbMenu();
});

function renderJobs() {
    var ip = $("#server-ip").text();
    var instanceId = $("#server-instance-id").text();
    $("#jobs").bootstrapTable({
        url: "/api/servers/" + ip + "/instances/" + instanceId,
        cache: false,
        columns: 
        [{
            field: "jobName",
            title: "作业名",
            sortable: "true"
        }, {
            field: "status",
            title: "状态",
            sortable: "true",
            formatter: "statusFormatter"
        }, {
            field: "sharding",
            title: "分片项"
        },{
            field: "operation",
            title: "操作",
            formatter: "generateOperationButtons"
        }]
    });
}

function statusFormatter(value) {
    switch(value) {
        case "RUNNING":
            return "<span class='label label-primary'>运行中</span>";
            break;
        case "READY":
            return "<span class='label label-info'>准备中</span>";
            break;
        case "DISABLED":
            return "<span class='label label-warning'>禁用中</span>";
            break;
        case "CRASHED":
            return "<span class='label label-danger'>宕机</span>";
            break;
        case "SHUTDOWN":
            return "<span class='label label-danger'>停止</span>";
            break;
    }
}

function generateOperationButtons(val, row){
    var triggerButton = "<button operation='trigger' class='btn-xs btn-success' job-name='" + row.jobName + "'>触发</button>";
    var shutdownButton = "<button operation='shutdown' class='btn-xs btn-danger' job-name='" + row.jobName + "'>关闭</button>";
    var removeButton = "<button operation='remove' class='btn-xs btn-danger' job-name='" + row.jobName + "'>删除</button>";
    var operationTd = triggerButton + "&nbsp;";
    if ("DISABLED" !== row.status && "CRASHED" !== row.status && "SHUTDOWN" !== row.status) {
        operationTd = triggerButton  + "&nbsp;";
    }
    if ("SHUTDOWN" !== row.status) {
        operationTd = operationTd  + "&nbsp;" + shutdownButton;
    }
    if ("SHUTDOWN" === row.status || "CRASHED" === row.status) {
        operationTd = removeButton;
    }
    return operationTd;
}

function bindTriggerButtons() {
    $(document).on("click", "button[operation='trigger'][data-toggle!='modal']", function(event) {
        $.ajax({
            url: "/api/jobs/trigger",
            type: "POST",
            data: JSON.stringify({jobName : $(event.currentTarget).attr("job-name"), ip : $("#server-ip").text(), instanceId : $("#server-instance-id").text()}),
            contentType: "application/json",
            dataType: "json",
            success: function(){
                $("#jobs").bootstrapTable("refresh");
                showSuccessDialog();
            }
        });
    });
    $(document).on("click", "button[operation='trigger'][data-toggle='modal']", function(event) {
        $("#chosen-job-name").text($(event.currentTarget).attr("job-name"));
    });
}

function bindShutdownButtons() {
    $(document).on("click", "button[operation='shutdown']", function(event) {
        $.ajax({
            url: "/api/jobs/shutdown",
            type: "POST",
            data: JSON.stringify({jobName : $(event.currentTarget).attr("job-name"), ip : $("#server-ip").text(), instanceId : $("#server-instance-id").text()}),
            contentType: "application/json",
            dataType: "json",
            success: function(){
                $("#jobs").bootstrapTable("refresh");
                showSuccessDialog();
            }
        });
    });
    $(document).on("click", "button[operation='shutdown']", function(event) {
        $("#chosen-job-name").text($(event.currentTarget).attr("job-name"));
    });
}

function bindRemoveButtons() {
    $(document).on("click", "button[operation='remove']", function(event) {
        $.ajax({
            url: "/api/jobs/remove",
            type: "POST",
            data: JSON.stringify({jobName : $(event.currentTarget).attr("job-name"), ip : $("#server-ip").text(), instanceId : $("#server-instance-id").text()}),
            contentType: "application/json",
            dataType: "json",
            success: function(data){
                if (data.length > 0) {
                    showFailureDialog("remove-job-failure-dialog");
                } else {
                    showSuccessDialog();
                }
                $("#jobs").bootstrapTable("refresh");
            }
        });
    });
    $(document).on("click", "button[operation='remove']", function(event) {
        $("#chosen-job-name").text($(event.currentTarget).attr("job-name"));
    });
}

function renderBreadCrumbMenu() {
    $("#breadcrumb-server").click(function() {
        $("#content").load("html/status/server/servers_status_overview.html");
    });
}
