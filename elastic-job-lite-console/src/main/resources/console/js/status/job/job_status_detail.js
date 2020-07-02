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
    $("#job-name").text($("#index-job-name").text());
    authorityControl();
    renderShardingTable();
    renderBreadCrumbMenu();
    bindButtons();
});

function renderShardingTable() {
    var jobName = $("#job-name").text();
    $("#sharding").bootstrapTable({
        url: "/api/jobs/" + jobName + "/sharding",
        cache: false,
        search: true,
        showRefresh: true,
        showColumns: true
    }).on("all.bs.table", function() {
        doLocale();
    });
}

function shardingStatusFormatter(value, row) {
    switch(value) {
        case "DISABLED":
            return "<span class='label label-warning' data-lang='status-disabled'></span>";
            break;
        case "RUNNING":
            return "<span class='label label-primary' data-lang='status-running'></span>";
            break;
        case "SHARDING_FLAG":
            return "<span class='label label-info' data-lang='' data-lang='status-sharding-flag'></span>";
            break;
        default:
            return "<span class='label label-default' data-lang='status-staging'></span>";
            break;
    }
}

function failoverFormatter(value, row) {
    return value ? "Y" : "-";
}

function generateOperationButtons(val, row) {
    var disableButton = "<button operation='disable-sharding' class='btn-xs btn-warning' job-name='" + row.jobName + "' item='" + row.item + "' data-lang='operation-disable'></button>";
    var enableButton = "<button operation='enable-sharding' class='btn-xs btn-success' job-name='" + row.jobName + "' item='" + row.item + "' data-lang='operation-enable'></button>";
    if ("DISABLED" === row.status) {
        return enableButton;
    } else {
        return disableButton;
    }
}

function bindButtons() {
    bindDisableButton();
    bindEnableButton();
}

function bindDisableButton() {
    $(document).off("click", "button[operation='disable-sharding']");
    $(document).on("click", "button[operation='disable-sharding']", function(event) {
        var jobName = $("#index-job-name").text();
        var item = $(event.currentTarget).attr("item");
        $.ajax({
            url: "/api/jobs/" + jobName + "/sharding/" + item + "/disable",
            type: "POST",
            success: function() {
                showSuccessDialog();
                $("#sharding").bootstrapTable("refresh");
            }
        });
    });
}

function bindEnableButton() {
    $(document).off("click", "button[operation='enable-sharding']");
    $(document).on("click", "button[operation='enable-sharding']", function(event) {
        var jobName = $("#index-job-name").text();
        var item = $(event.currentTarget).attr("item");
        $.ajax({
            url: "/api/jobs/" + jobName + "/sharding/" + item + "/enable",
            type: "POST",
            success: function () {
                showSuccessDialog();
                $("#sharding").bootstrapTable("refresh");
            }
        });
    });
}

function renderBreadCrumbMenu() {
    $("#breadcrumb-job").click(function() {
        $("#content").load("html/status/job/jobs_status_overview.html", null, function(){
            doLocale();
        });
    });
}
