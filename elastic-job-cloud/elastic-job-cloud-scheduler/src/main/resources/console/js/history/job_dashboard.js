$(function() {
    renderPieChartSinceLastMinuteData();
    renderPieChartSinceLastHourData();
    renderPieChartSinceLastWeekData();
    renderJobTypePieChart();
    renderJobExecutionTypePieChart();
    renderStasticsJobsLineChart();
    renderRunningJobsAndTasksLineChart();
    renderRegisteredJobs();
});

function renderPieChartSinceLastMinuteData() {
    $.ajax({
        url: "/api/job/statistics/tasks/results/lastMinute",
        dataType: "json",
        success: function(jobData) {
            if(null !== jobData) {
                var chartName = "#total-jobs-lastMinute";
                var color = ["rgb(144,237,125)","red"];
                var jobResult = [["成功", jobData.successCount], ["失败", jobData.failedCount]];
                renderPieChart(chartName, "一分钟作业情况", color, jobResult);
            }
        }
    });
}

function renderPieChartSinceLastHourData() {
    $.ajax({
        url: "/api/job/statistics/tasks/results/lastHour",
        dataType: "json",
        success: function(jobData) {
            if(null !== jobData) {
                var chartName = "#total-jobs-lastHour";
                var color = ["rgb(144,237,125)", "red"];
                var jobResult = [["成功", jobData.successCount], ["失败", jobData.failedCount]];
                renderPieChart(chartName, "一小时作业情况", color, jobResult);
            }
        }
    });
}

function renderPieChartSinceLastWeekData() {
    $.ajax({
        url: "/api/job/statistics/tasks/results/lastWeek",
        dataType: "json",
        success: function(jobData) {
            if(null !== jobData) {
                var chartName = "#total-jobs-weekly";
                var color = ["rgb(144,237,125)", "red"];
                var jobResult = [["成功", jobData.successCount], ["失败", jobData.failedCount]];
                renderPieChart(chartName, "一周作业情况", color, jobResult);
            }
        }
    });
}

function renderJobTypePieChart() {
    $.ajax({
        url: "/api/job/statistics/jobs/type",
        dataType: "json",
        success: function(jobData) {
            if(null !== jobData) {
                var chartName = "#job-type";
                var color = ["rgb(144, 237, 125)", "rgb(247, 163, 92)", "rgb(67, 67, 72)"];
                var jobResult = [["DATAFLOW", jobData.dataflowJobCount], ["SIMPLE", jobData.simpleJobCount], ["SCRIPT", jobData.scriptJobCount]];
                renderPieChart(chartName, '作业类型', color, jobResult);
            }
        }
    });
}

function renderJobExecutionTypePieChart() {
    $.ajax({
        url: "/api/job/statistics/jobs/executionType",
        dataType: "json",
        success: function(jobData) {
            if(null !== jobData) {
                var chartName = "#job-execution-type";
                var color = ["rgb(144, 237, 125)", "rgb(124, 181, 236)"];
                var jobResult = [["TRANSIENT", jobData.transientJobCount], ["DAEMON", jobData.daemonJobCount]];
                renderPieChart(chartName, "作业执行类型", color, jobResult);
            }
        }
    });
}

function renderStasticsJobsLineChart() {
    $.ajax({
        url: "/api/job/statistics/tasks/results?since=last24hours",
        dataType: "json",
        success: function(jobData) {
            if(null !== jobData) {
                var chartName = "#statictis_jobs";
                var successData = [];
                var failData = [];
                for(var i = 0; i < jobData.length; i++) {
                    var dateTime = new Date(jobData[i].statisticsTime).getTime() + 1000*60*60*8;
                    successData.push([dateTime, jobData[i].successCount]);
                    failData.push([dateTime, jobData[i].failedCount]);
                }
                var resultData = [{type: "spline", name: "作业成功数", data: successData}, {type: "spline", name: "作业失败数", data: failData}];
                renderLineChart(chartName, "作业成功/失败数", resultData);
            }
        }
    });
}

function renderRunningJobsAndTasksLineChart() {
    $.ajax({
        url: "/api/job/statistics/jobs/running?since=lastWeek",
        dataType: "json",
        success: function(jobData) {
            $.ajax({
                url: "/api/job/statistics/tasks/running?since=lastWeek",
                dataType: "json",
                success: function(taskData) {
                    if(null !== taskData) {
                        var chartName = "#run-jobs";
                        var jobRunningData = [];
                        var taskRunningData = [];
                        for(var i = 0; i < jobData.length; i++) {
                            var dateTime = new Date(jobData[i].statisticsTime).getTime() + 1000 * 60 * 60 * 8;
                            jobRunningData.push([dateTime, jobData[i].runningCount]);
                        }
                        for(var i = 0; i < taskData.length; i++) {
                            var dateTime = new Date(taskData[i].statisticsTime).getTime() + 1000 * 60 * 60 * 8;
                            taskRunningData.push([dateTime, taskData[i].runningCount]);
                        }
                        var resultData = [{type: "spline", name: "任务运行数", data: taskRunningData}, {type: "spline", name: "作业运行数", data: jobRunningData}];
                        renderLineChart(chartName, "作业/任务运行数", resultData);
                    }
                }
            });
        }
    });
}

function renderRegisteredJobs() {
    $.ajax({
        url: "/api/job/statistics/jobs/register",
        dataType: "json",
        success: function(jobData) {
            if(null !== jobData) {
                var chartName = "#regist-jobs";
                var registerData = [];
                for(var i = 0; i < jobData.length; i++) {
                    var dateTime = new Date(jobData[i].statisticsTime).getTime() + 1000 * 60 * 60 * 8;
                    registerData.push([dateTime, jobData[i].registeredCount]);
                }
                var resultData = [{ type: "spline", name: "接入作业数", data: registerData}];
                renderLineChart(chartName, "接入平台作业数", resultData);
            }
        }
    });
}

function renderPieChart(chartName, title, color, jobData) {
    $(chartName).highcharts({
        chart: {
            backgroundColor: "rgba(255, 255, 255, 0)",
        },
        title: {
            text: title
        },
        plotOptions: {
            pie: {
                size: "60%",
                allowPointSelect: true,
                cursor: "pointer",
                dataLabels: {
                    enabled: true,
                    format: "<b>{point.name}</b>:<br> {point.percentage:.1f} % ",
                    distance: 5
                }
            }
        },
        colors: color,
        series: [{
            type: "pie",
            name: "作业",
            data: jobData
        }],
        credits: {
            enabled: false
        }
    });
}

function renderLineChart(chartName, title, jobData) {
    Highcharts.setOptions({
        lang: {
            resetZoom: "重置",
            resetZoomTitle: "重置缩放比例"
        }
    });
    $(chartName).highcharts({
        chart: {
            zoomType: "x",
            resetZoomButton: {
                position: {
                    align: "right",
                    verticalAlign: "top",
                    x: 0,
                    y: -50
                }
            },
            backgroundColor: "rgba(255, 255, 255, 0)"
        },
        credits: {
            enabled: false
        },
        title: {
            text: title
        },
        subtitle: {
            text: document.ontouchstart === undefined ? "鼠标拖动可以进行缩放" : "手势操作进行缩放"
        },
        tooltip: {
            shared: true,
            crosshairs: true,
            dateTimeLabelFormats: {
                millisecond: "%H:%M:%S.%L",
                second: "%Y-%m-%d %H:%M:%S",
                minute: "%Y-%m-%d %H:%M",
                hour: "%Y-%m-%d %H:%M",
                day: "%Y-%m-%d",
                week: "%m-%d",
                month: "%Y-%m",
                year: "%Y"
            }
        },
        xAxis: {
            type: "datetime",
            dateTimeLabelFormats: {
                millisecond: "%H:%M:%S.%L",
                second: "%H:%M:%S",
                minute: "%H:%M",
                hour: "%H:%M",
                day: "%m-%d",
                week: "%m-%d",
                month: "%Y-%m",
                year: "%Y"
            } 
        },
        yAxis: {
            title: {
                text: ""
            },
            labels: {
                align: "left",
                x: -10,
                y: 0
            }
        },
        series: jobData
    });
}
