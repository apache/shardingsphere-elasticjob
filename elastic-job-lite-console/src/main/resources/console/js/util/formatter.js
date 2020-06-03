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
    $customDatepicker = $(".custom-datepicker");
    $customDatepicker.daterangepicker({singleDatePicker : true, timePicker : true, timePicker24Hour : true, timePickerSeconds : true, autoUpdateInput : false});
    $customDatepicker.on("apply.daterangepicker", function(event, picker) {
        $(this).val(picker.startDate.format("YYYY-MM-DD HH:mm:ss"));
    });
    $customDatepicker.on("cancel.daterangepicker", function(event, picker) {
        $(this).val("");
    });
});

Date.prototype.format = function(fmt) {
    var date = {
    "M+" : this.getMonth() + 1,
    "d+" : this.getDate(),
    "h+" : this.getHours() % 12 == 0 ? 12 : this.getHours() % 12,
    "H+" : this.getHours(),
    "m+" : this.getMinutes(),
    "s+" : this.getSeconds()
    };
    if(/(y+)/.test(fmt)) {
        fmt=fmt.replace(RegExp.$1, (this.getFullYear() + "").substr(4 - RegExp.$1.length));
    }
    for(var each in date) {
        if(new RegExp("(" + each + ")").test(fmt)) {
            fmt = fmt.replace(RegExp.$1, (RegExp.$1.length == 1) ? (date[each]) : (("00" + date[each]).substr(("" + date[each]).length)));
        }
    }
    return fmt;
};

function dateTimeFormatter(value) {
    if (null == value) {
        return "";
    }
    return new Date(value).format("yyyy-MM-dd HH:mm:ss");
}

function showHistoryMessage(value) {
    $("#history-message").html(value);
    $("#history-message-modal").modal("show");
}
