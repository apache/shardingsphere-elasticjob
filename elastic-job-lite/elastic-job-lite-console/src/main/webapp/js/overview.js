$(function() {
    renderJobsOverview();
    renderServersOverview();
});
    
function renderJobsOverview() {
    $('#jobs-overview-tbl').bootstrapTable({
        url: 'job/jobs',
        method: 'get',
        cache: false,
        rowStyle: function (row, index) {
            var strclass = "";
            if ("OK" === row.status) {
                strclass = 'success';
            } else if ("MANUALLY_DISABLED" === row.status) {
                strclass = 'info';
            } else if ("PARTIAL_ALIVE" === row.status) {
                strclass = "warning";
            } else if ("ALL_CRASHED" === row.status) {
                strclass = "danger";
            } else {
                return {};
            }
            return { classes: strclass }
        },
        columns: [
        {
            field: 'jobName',
            title: '作业名'
        }, {
            field: 'status',
            title: '运行状态'
        }, {
            field: 'cron',
            title: 'cron表达式'
        },{
            field: 'description',
            title: '描述'
        }]
    });
}
    
function jobFormatter(val, row){
    var jobName = row.jobName;
    var jobType = row.jobtype;
    var result = "<a href='index.html?jobName=" + jobName + "&jobType=" + jobType + "'>" + jobName + "</a>";
    return result;
}
    
function renderServersOverview() {
    $('#servers-overview-tbl').bootstrapTable({
        url: 'server/servers',
        method: 'get',
        cache: false,
        rowStyle: function (row, index) {
            var strclass = "";
            if ("OK" === row.status) {
                strclass = 'success';//还有一个active
            } else if ("PARTIAL_ALIVE" === row.status) {
                strclass = 'warning';
            } else if ("ALL_CRASHED" === row.status) {
                strclass = "danger";
            } else {
                return {};
            }
            return { classes: strclass }
        },
        columns: [
        {
            field: 'serverIp',
            title: '服务器IP'
        }, {
            field: 'serverHostName',
            title: '服务器名'
        }, {
            field: 'status',
            title: '状态'
        }]
    });
}
    
function serverFormatter(val, row){
    var serverIp = row.serverIp;
    var result = "<a href='index.html?serverIp=" + serverIp + "'>" + serverIp + "</a>";
    return result;
}