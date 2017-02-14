$(function () {
    $('.custom-datepicker').daterangepicker({singleDatePicker: true, timePicker: true, timePicker24Hour: true, timePickerSeconds: true, autoUpdateInput: false});
    $('.custom-datepicker').on('apply.daterangepicker', function(ev, picker) {
        $(this).val(picker.startDate.format('YYYY-MM-DD HH:mm:ss'));
    });
    $('.custom-datepicker').on('cancel.daterangepicker', function(ev, picker) {
        $(this).val('');
    });
});
    
Date.prototype.format=function(fmt) {
    var o = {
    "M+" : this.getMonth()+1,
    "d+" : this.getDate(),
    "h+" : this.getHours()%12 == 0 ? 12 : this.getHours()%12,
    "H+" : this.getHours(),
    "m+" : this.getMinutes(),
    "s+" : this.getSeconds(),
    };
    if(/(y+)/.test(fmt)){
        fmt=fmt.replace(RegExp.$1, (this.getFullYear()+"").substr(4 - RegExp.$1.length));
    }
    for(var k in o){
        if(new RegExp("("+ k +")").test(fmt)){
            fmt = fmt.replace(RegExp.$1, (RegExp.$1.length==1) ? (o[k]) : (("00"+ o[k]).substr((""+ o[k]).length)));
        }
    }
    return fmt;
}
function dateTimeFormatter(value) {
    return new Date(value).format("yyyy-MM-dd HH:mm:ss");
}

function largeContextFormatter(value) {
    if(value != null && value.length > 50) {
        return "<div title='" + value+"'>" + value.substring(0,47) + "...</div>";
    } else {
        return value;
    }
}
