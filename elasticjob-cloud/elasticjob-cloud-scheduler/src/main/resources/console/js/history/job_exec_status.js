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
    $(".toolbar input").bind("keypress", function(event) {
        if("13" == event.keyCode) {
            $("#job-exec-status-table").bootstrapTable("refresh", {silent: true});
        }
    });
    $("#job-exec-status-table").bootstrapTable().on("all.bs.table", function() {
        doLocale();
    });
});

function queryParams(params) {
    return {
        per_page: params.pageSize, 
        page: params.pageNumber,
        q: params.searchText,
        sort: params.sortName,
        order: params.sortOrder,
        jobName: $("#job-name").val(),
        taskId: $("#task-id").val(),
        slaveId: $("#slave-id").val(),
        source: $("#source").val(),
        executionType: $("#execution-type").val(),
        state: $("#state").val(),
        startTime: $("#start-time").val(),
        endTime: $("#end-time").val()
    };
}

function splitRemarkFormatter(value, row) {
    var maxLength = 50;
    var replacement = "...";
    if(null != value && value.length > maxLength) {
        var valueDetail = value.substring(0 , maxLength - replacement.length) + replacement;
        value = value.replace(/\r\n/g,"<br/>").replace(/\n/g,"<br/>").replace(/\'/g, "\\'");
        var remarkHtml;
        if ("TASK_FAILED" === row.state || "TASK_ERROR" === row.state) {
            remarkHtml = '<a href="javascript: void(0);" style="color:#FF0000;" onClick="showHistoryMessage(\'' + value + '\')">' + valueDetail + '</a>';
        } else {
            remarkHtml = '<a href="javascript: void(0);" style="color:black;" onClick="showHistoryMessage(\'' + value + '\')">' + valueDetail + '</a>';
        }
        return remarkHtml;
    }
    return value;
}

function stateFormatter(value) {
    switch(value)
    {
        case "TASK_STAGING":
            return "<span class='label label-default' data-lang='status-staging'></span>";
        case "TASK_FAILED":
            return "<span class='label label-danger' data-lang='status-task-failed'></span>";
        case "TASK_FINISHED":
            return "<span class='label label-success' data-lang='status-task-finished'></span>";
        case "TASK_RUNNING":
            return "<span class='label label-primary' data-lang='status-running'></span>";
        case "TASK_ERROR":
            return "<span class='label label-danger' data-lang='status-task-error'></span>";
        case "TASK_KILLED":
            return "<span class='label label-warning' data-lang='status-task-killed'></span>";
        default:
            return "-";
    }
}
