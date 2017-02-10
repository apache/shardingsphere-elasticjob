$(function(){
    bootstrapValidator();
    submitAppBootstrapValidator();
});
    
function bindSubmitAppSettingsForm() {
    $.ajax({
        type:'post',
        dataType:'json',
        data:JSON.stringify(dataAppInfo()),
        url:'/app',
        contentType: "application/json",
        success: function(data) {
            window.location="index.html?appName=appName"
        },
        error:function(XMLHttpRequest, textStatus, errorThrown){
            alert ("XMLHttpRequest.status="+XMLHttpRequest.status+"\ntextStatus="+textStatus+"\nerrorThrown=" + errorThrown);
        }
    });
}