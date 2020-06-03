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
    $("#content").load("html/global/registry_center.html");
    $("#reg-center").click(function() {
        $("#content").load("html/global/registry_center.html");
    });
    $("#event-trace-data-source").click(function() {
        $("#content").load("html/global/event_trace_data_source.html");
    });
    $("#job-status").click(function() {
        $("#content").load("html/status/job/jobs_status_overview.html");
    });
    $("#server-status").click(function() {
        $("#content").load("html/status/server/servers_status_overview.html");
    });
    $("#event-trace-history").click(function() {
        $("#content").load("html/history/job_event_trace_history.html");
    });
    $("#status-history").click(function() {
        $("#content").load("html/history/job_status_history.html");
    });
    $("#help").click(function() {
        $("#content").load("html/help/help.html", null, function(){
            doLocale();
        });
    });
    switchLanguage();

    //初始化显示语言
    initLanguage();
});
