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
    $(".toolbar input").bind("keypress", function(event) {
        if("13" == event.keyCode) {
            $("#dag-history-table").bootstrapTable("refresh", {silent: true});
        }
    });

    $("#dag-history-table").on("all.bs.table", function() {
        doLocale();
    });

    validate()
});


function queryParams(params) {
    return {
        per_page: params.pageSize,
        page: params.pageNumber,
        q: params.searchText,
        sort: "execDate,execTime",
        order: "desc",
        groupName: $("#tFlowCode").val(),
        batchNo: $("#tBatchNo").val(),
        startTime: $("#tStartTime").val(),
        endTime: $("#tEndTime").val(),
    };
}

