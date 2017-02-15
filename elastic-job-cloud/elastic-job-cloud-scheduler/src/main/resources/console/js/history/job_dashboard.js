$(function () {
    getTasksResultSinceLastMinute();
    getTasksResultSinceLastHour();
    getTasksResultSinceLastWeek();
    getJobType();
    getJobExecutionType();
    getStatictisJobs();
    getRunningJobAndTaskSincelastWeek();
    getRegisteredJobs();
});
    
function getTasksResultSinceLastMinute() {
    var url = '/job/statistics/tasks/results/lastMinute',
        chartName = '#total_jobs_lastMinute',
        colorsArray = ['rgb(144,237,125)','red'],
        jobData = getChartData(url);
        var jobResult = [['成功', jobData.successCount],['失败', jobData.failedCount]];
    producePieChart(chartName,'一分钟作业情况',colorsArray,jobResult);
}
    
function getTasksResultSinceLastHour() {
    var url = '/job/statistics/tasks/results/lastHour',
        chartName = '#total_jobs_lastHour',
        colorsArray = ['rgb(144,237,125)','red'],
        jobData = getChartData(url);
        var jobResult = [['成功', jobData.successCount],['失败', jobData.failedCount]];
    producePieChart(chartName,'一小时作业情况',colorsArray,jobResult);
}
    
function getTasksResultSinceLastWeek() {
    var url = '/job/statistics/tasks/results/lastWeek',
        chartName = '#total_jobs_weekly',
        colorsArray = ['rgb(144,237,125)','red'],
        jobData = getChartData(url);
        var jobResult = [['成功', jobData.successCount],['失败', jobData.failedCount]];
    producePieChart(chartName,'一周作业情况',colorsArray,jobResult);
}
    
function getJobType() {
    var url = '/job/statistics/jobs/type',
        chartName = '#job_type',
        colorsArray = ['rgb(144, 237, 125)','rgb(247, 163, 92)','rgb(67, 67, 72)'],
        jobData = getChartData(url);
        var jobResult = [['DATAFLOW', jobData.dataflowJobCount],['SIMPLE', jobData.simpleJobCount],['SCRIPT',jobData.scriptJobCount]];
    producePieChart(chartName,'作业类型',colorsArray,jobResult);
}
    
function getJobExecutionType() {
    var url = '/job/statistics/jobs/executionType',
        chartName = '#job_execution_type',
        colorsArray = ['rgb(144, 237, 125)','rgb(124, 181, 236)'],
        jobData = getChartData(url);
        var jobResult = [['TRANSIENT', jobData.transientJobCount],['DAEMON', jobData.daemonJobCount]];
    producePieChart(chartName,'作业执行类型',colorsArray,jobResult);
}
    
function getStatictisJobs(){
    var url = '/job/statistics/tasks/results?since=last24hours',
        chartName = '#statictis_jobs',
        jobData = getChartData(url);
        var succData = [],failData = [];
        for(var i=0;i<jobData.length;i++){
          var dateTime = new Date(jobData[i].statisticsTime).getTime() + 1000*60*60*8;
          succData.push([dateTime,jobData[i].successCount]);
          failData.push([dateTime,jobData[i].failedCount])
        }
        resultData = [{type: 'spline',name: '作业成功数',data: succData}, {type: 'spline',name: '作业失败数',data: failData}];
    produceLineChart(chartName,'作业成功/失败数',resultData);
}
    
function getRunningJobAndTaskSincelastWeek(){
    var urlJob = '/job/statistics/jobs/running?since=lastWeek',
        urlTask = '/job/statistics/tasks/running?since=lastWeek',
        chartName = '#run_jobs',
        jobData = getChartData(urlJob),
        taskData =  getChartData(urlTask),
        jobRunningData = [], taskRunningData = [];
        for(var i=0;i<jobData.length;i++){
            var dateTime = new Date(jobData[i].statisticsTime).getTime() + 1000*60*60*8;
            jobRunningData.push([dateTime,jobData[i].runningCount]);
        }
        for(var i=0;i<taskData.length;i++){
            var dateTime = new Date(taskData[i].statisticsTime).getTime() + 1000*60*60*8;
            taskRunningData.push([dateTime,taskData[i].runningCount]);
        }
        resultData = [{type: 'spline',name: '任务运行数',data: taskRunningData}, {type: 'spline',name: '作业运行数',data: jobRunningData}];
    produceLineChart(chartName,'作业/任务运行数',resultData);
}
    
function getRegisteredJobs(){
    var url = '/job/statistics/jobs/register',
        chartName = '#import_jobs',
        jobData = getChartData(url);
        var registerData = [];
        for(var i=0;i<jobData.length;i++){
          var dateTime = new Date(jobData[i].statisticsTime).getTime() + 1000*60*60*8;
          registerData.push([dateTime,jobData[i].registeredCount]);
        }
        resultData = [{ type: 'spline',name: '接入作业数',data: registerData}];
    produceLineChart(chartName,'接入平台作业数',resultData);
}
    
function getChartData(url){
    var result = [];
    $.ajax({
        url: url,
        async: false,
        dataType: 'json',
        success: function (data) {
            if(null != data){
                result = data;
            }
        }
    });
    return result;
}
    
function producePieChart(chartName,title,colorsArray,jobData){
    $(chartName).highcharts({
        chart: {
            backgroundColor: 'rgba(255, 255, 255, 0)',
        },
        title: {
            text: title
        },
        plotOptions: {
            pie: {
                size: '60%',
                allowPointSelect: true,
                cursor: 'pointer',
                dataLabels: {
                    enabled: true,
                    format: '<b>{point.name}</b>:<br> {point.percentage:.1f} %',
                    distance: 5
                }
            }
        },
        colors: colorsArray,
        series: [{
            type: 'pie',
            name: '作业',
            data: jobData
        }],
        credits: {
            enabled: false
        }
    });
}
    
function produceLineChart(chartName,title,jobData){
    Highcharts.setOptions({
        lang: {
            resetZoom: '重置',
            resetZoomTitle: '重置缩放比例'
        }
    });
    $(chartName).highcharts({
        chart: {
            zoomType: 'x',
            resetZoomButton: {
                position:{
                    align: 'right',
                    verticalAlign: 'top',
                    x: 0,
                    y: -50
                }
            },
            backgroundColor: 'rgba(255, 255, 255, 0)'
        },
        credits:{  
            enabled:false  
        },
        title: {
            text: title
        },
        subtitle: {
            text: document.ontouchstart === undefined ? '鼠标拖动可以进行缩放' : '手势操作进行缩放'
        },
        tooltip:{
            shared:true,
            crosshairs:true,
            dateTimeLabelFormats: {
                millisecond: '%H:%M:%S.%L',
                second: '%Y-%m-%d %H:%M:%S',
                minute: '%Y-%m-%d %H:%M',
                hour: '%Y-%m-%d %H:%M',
                day: '%Y-%m-%d',
                week: '%m-%d',
                month: '%Y-%m',
                year: '%Y'
            }
        },
        xAxis: {
            type: 'datetime',
            dateTimeLabelFormats: {
                millisecond: '%H:%M:%S.%L',
                second: '%H:%M:%S',
                minute: '%H:%M',
                hour: '%H:%M',
                day: '%m-%d',
                week: '%m-%d',
                month: '%Y-%m',
                year: '%Y'
            } 
        },
        yAxis: {
            title: {
                text: ''
            },
            labels: {
                align: 'left',
                x: -10,
                y: 0
            }
        },
        series: jobData
    });
}