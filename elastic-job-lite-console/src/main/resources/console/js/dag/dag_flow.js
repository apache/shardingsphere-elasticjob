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
            $("#dag-flow-table").bootstrapTable("refresh", {silent: true});
        }
    });

    $("#dag-flow-table").on("all.bs.table", function() {
        doLocale();
    });

    validate()
});

function queryParams(params) {
    var sortName = "success" === params.sortName ? "isSuccess" : params.sortName;
    return {
        per_page: params.pageSize,
        page: params.pageNumber,
        q: params.searchText,
        sort: sortName,
        order: params.sortOrder,
        flowCode: $("#flow-code").val()
    };
}


function generateOperationButtons(val, row, index) {
    var operationTd="";
    var id = index;
    console.log(index)
    console.log(row)
    operationTd += "<a href='javascript:;' class='btn text-primary' onclick=\"detailById('" + row + "','" + id + "')\" title='Detail'><span class='glyphicon glyphicon-pencil'></span></a>";
    operationTd += "<a href='javascript:;' class='btn text-success' onclick=\"startDagById('" + row + "','" + id + "')\" title='Start'><span class='glyphicon glyphicon-play'></span></a>";
    operationTd += "<a href='javascript:;' class='btn text-danger' onclick=\"stopDagById('" + row + "','" + id + "')\" title='Stop'><span class='glyphicon glyphicon-stop'></span></a>";
    operationTd += "<a href='javascript:;' class='btn text-warning' onclick=\"pauseDagById('" + row + "','" + id + "')\" title='Pause'><span class='glyphicon glyphicon-pause'></span></a>";
    operationTd += "<a href='javascript:;' class='btn text-success' onclick=\"resumeDagById('" + row + "','" + id + "')\" title='Resume'><span class='glyphicon glyphicon-refresh'></span></a>";
    operationTd += "<a href='javascript:;' class='btn text-purple' onclick=\"reRunFailDagById('" + row + "','" + id + "')\" title='RunFails'><span class='glyphicon glyphicon-repeat'></span></a>";
    return operationTd;
}

function startDagById(row, index) {
    dagOperate("Start", "", "", row, index)
}
function stopDagById(row, index) {
    dagOperate("Stop",  "", "", row, index)
}
function pauseDagById(row, index) {
    dagOperate("Pause", "", "", row, index)
}
function resumeDagById(row, index) {
    dagOperate("Resume", "", "", row, index)
}
function reRunFailDagById(row, index) {
    dagOperate("RunFails", "", "", row, index)
}
function dagOperate(operate , succMsg, failMsg, row, index) {
    var data = JSON.stringify($("#dag-flow-table").bootstrapTable('getData'));
    var data_json = JSON.parse(data);
    var flowCode = data_json[index].flowCode
    console.log(operate + " Dag " + flowCode)
    $.ajax({
        url: '/api/dag/' + flowCode + '/' + operate,
        type: "POST",
        data: JSON.stringify({"flowCode": flowCode}),
        contentType: "application/json",
        dataType: "json",
        success: function(data) {
            if (data.success) {
                console.log(operate + " DAG Success" )
                alert(operate + " DAG Success " + succMsg )
            } else {
                console.log(operate + " DAG Fail")
                alert(operate + " DAG Fail " + failMsg + " " + data.error )
            }
        },
        error: function (res) {
            alert("DAG " + operate + " Error " + res);
        }
    });
}

function detailById(row, index) {
    var data = JSON.stringify($("#dag-flow-table").bootstrapTable('getData'));
    var data_json = JSON.parse(data);
    var flowCode = data_json[index].flowCode

    $.ajax({
        type: 'GET',
        url: "api/dag/detail/" + flowCode,
        contentType: "application/json",
        dataType: "json",
        // async:false,
        success: function (data) {
            console.log(data)
            $("#dag-detail-table").bootstrapTable({data: data.rows})
            $("#dag-detail-modal").modal({backdrop: 'static', keyboard: true});
        },
        error : function (err) {
            alert("Query dag details error：" +  err)
        }
    });
}

function validate() {
    $("#add-dag-flow-form").bootstrapValidator({
        message: "This value is not valid",
        feedbackIcons: {
            valid: "glyphicon glyphicon-ok",
            invalid: "glyphicon glyphicon-remove",
            validating: "glyphicon glyphicon-refresh"
        },
        fields: {
            addFlowCode : {
                validators: {
                    notEmpty: {
                        message: $.i18n.prop("dag-flow-input-not-null")
                    }
                }
            },
            addFlowName : {
                validators: {
                    notEmpty: {
                        message: $.i18n.prop("dag-flow-input-not-null")
                    }
                }
            },
            addFlowVersion : {
                validators: {
                    notEmpty: {
                        message: $.i18n.prop("dag-flow-input-not-null")
                    }
                }
            },
            addFlowAppCode : {
                validators: {
                    notEmpty: {
                        message: $.i18n.prop("dag-flow-input-not-null")
                    }
                }
            },
            addFlowStatus : {
                validators: {
                    notEmpty: {
                        message: $.i18n.prop("dag-flow-input-not-null")
                    }
                }
            }
        }
    });
    $("#add-dag-flow-form").submit(function(event) {
        event.preventDefault();
    });
}

