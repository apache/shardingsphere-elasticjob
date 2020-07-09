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
    $("#content-right").load("/html/app/apps_overview.html");
    refreshJobNavTag();
    refreshAppNavTag();
    $("#register-app").click(function() {
        $("#content-right").load("/html/app/apps_overview.html");
    });
    $("#register-job").click(function() {
        $("#content-right").load("/html/job/jobs_overview.html");
    });
    $("#status").click(function() {
        $("#content-right").load("/html/job/job_status.html", null, function(){
            $("table").bootstrapTable().on("all.bs.table", function() {
                doLocale();
            });
        });
    });
    $("#dashboard").click(function() {
        $("#content-right").load("/html/history/job_dashboard.html");
    });
    $("#exec-details").click(function() {
        $("#content-right").load("/html/history/job_exec_details.html");
    });
    $("#exec-status").click(function() {
        $("#content-right").load("/html/history/job_exec_status.html");
    });
    switchLanguage();
});
