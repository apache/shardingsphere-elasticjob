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
    $("[data-mask]").inputmask();
    $(".toolbar input").bind("keypress", function(event) {
        if("13" == event.keyCode) {
            $("#job-exec-details-table").bootstrapTable("refresh", {silent: true});
        }
    });
    $("#job-exec-details-table").on("all.bs.table", function() {
        doLocale();
    });
});

function queryParams(params) {
    var sortName = "success" === params.sortName ? "isSuccess" : params.sortName;
    return {
        per_page: params.pageSize,
        page: params.pageNumber,
        q: params.searchText,
        sort: sortName,
        order: params.sortOrder,
        jobName: $("#job-name").val(),
        startTime: $("#start-time").val(),
        endTime: $("#end-time").val(),
        ip: $("#ip").val(),
        isSuccess: $('input[name = "isSuccess"]:checked ').val()
    };
}

function successFormatter(value) {
    switch(value)
    {
    case true:
        return "<span class='label label-success' data-lang='execute-result-success'></span>";
      case false:
          return "<span class='label label-danger' data-lang='execute-result-failure'></span>";
      default:
        return "<span class='label label-danger' data-lang='execute-result-null'></span>";
    }
}

function splitFormatter(value) {
    var maxLength = 50;
    var replacement = "...";
    if(null != value && value.length > maxLength) {
        var vauleDetail = value.substring(0 , maxLength - replacement.length) + replacement;
        value = value.replace(/\r\n/g,"<br/>").replace(/\n/g,"<br/>").replace(/\'/g, "\\'");
        return '<a href="javascript: void(0);" style="color:#FF0000;" onClick="showHistoryMessage(\'' + value + '\')">' + vauleDetail + '</a>';
    }
    return value;
}
