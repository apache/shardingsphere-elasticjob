$(function() {
    $(".custom-datepicker").daterangepicker({singleDatePicker : true, timePicker : true, timePicker24Hour : true, timePickerSeconds : true, autoUpdateInput : false});
    $(".custom-datepicker").on("apply.daterangepicker", function(event, picker) {
        $(this).val(picker.startDate.format("YYYY-MM-DD HH:mm:ss"));
    });
    $(".custom-datepicker").on("cancel.daterangepicker", function(event, picker) {
        $(this).val("");
    });
});

Date.prototype.format=function(fmt) {
    var date = {
    "M+" : this.getMonth() + 1,
    "d+" : this.getDate(),
    "h+" : this.getHours() % 12 == 0 ? 12 : this.getHours() % 12,
    "H+" : this.getHours(),
    "m+" : this.getMinutes(),
    "s+" : this.getSeconds(),
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
}

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
