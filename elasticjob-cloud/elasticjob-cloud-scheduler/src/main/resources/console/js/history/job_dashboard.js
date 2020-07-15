/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

$(function() {
    doLocale();
    renderPieChartSinceLastMinuteData();
    renderPieChartSinceLastHourData();
    renderPieChartSinceLastWeekData();
    renderJobExecutionTypePieChart();
    renderStaticsJobsLineChart();
    renderRunningJobsAndTasksLineChart();
    renderRegisteredJobs();
});

function doLocale() {
    if ($("#content-right").hasClass("lang-en")) {
        i18n("en");
    } else {
        i18n("zh");
    }
    renderPieChartSinceLastMinuteData();
    renderPieChartSinceLastHourData();
    renderPieChartSinceLastWeekData();
    renderJobExecutionTypePieChart();
    renderStaticsJobsLineChart();
    renderRunningJobsAndTasksLineChart();
    renderRegisteredJobs();
}

function renderPieChartSinceLastMinuteData() {
    $.ajax({
        url: "/api/job/statistics/tasks/results/lastMinute",
        dataType: "json",
        success: function(jobData) {
            if(null !== jobData) {
                var chartName = "#total-jobs-lastMinute";
                var color = ["rgb(144,237,125)","red"];
                var jobResult = [[$.i18n.prop("execute-result-success"), jobData.successCount], [$.i18n.prop("execute-result-failure"), jobData.failedCount]];
                renderPieChart(chartName, $.i18n.prop("job-info-for-one-minute"), color, jobResult);
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
                var jobResult = [[$.i18n.prop("execute-result-success"), jobData.successCount], [$.i18n.prop("execute-result-failure"), jobData.failedCount]];
                renderPieChart(chartName, $.i18n.prop("job-info-for-one-hour"), color, jobResult);
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
                var jobResult = [[$.i18n.prop("execute-result-success"), jobData.successCount], [$.i18n.prop("execute-result-failure"), jobData.failedCount]];
                renderPieChart(chartName, $.i18n.prop("job-info-for-one-week"), color, jobResult);
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
                renderPieChart(chartName, $.i18n.prop("job-execution-type"), color, jobResult);
            }
        }
    });
}

function renderStaticsJobsLineChart() {
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
                var resultData = [{type: "spline", name: $.i18n.prop("job-success-count"), data: successData}, {type: "spline", name: $.i18n.prop("job-failure-count"), data: failData}];
                renderLineChart(chartName, $.i18n.prop("dashboard-succ-and-fail-count"), resultData);
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
                        var resultData = [{type: "spline", name: $.i18n.prop("task-running-count"), data: taskRunningData}, {type: "spline", name: $.i18n.prop("task-running-count"), data: jobRunningData}];
                        renderLineChart(chartName, $.i18n.prop("dashboard-job-task-running-count"), resultData);
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
                var resultData = [{ type: "spline", name: $.i18n.prop("dashboard-current-jobs-count"), data: registerData}];
                renderLineChart(chartName, $.i18n.prop("dashboard-current-jobs-count"), resultData);
            }
        }
    });
}

function renderPieChart(chartName, title, color, jobData) {
    $(chartName).highcharts({
        chart: {
            backgroundColor: "rgba(255, 255, 255, 0)"
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
            name: $.i18n.prop("sidebar-job"),
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
            resetZoom: $.i18n.prop("operation-reset"),
            resetZoomTitle: $.i18n.prop("operation-reset-scaling")
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
            text: document.ontouchstart === undefined ? $.i18n.prop("highchart-tooltip-info1") : $.i18n.prop("highchart-tooltip-info2")
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
