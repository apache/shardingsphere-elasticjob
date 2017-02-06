$(function() {
    bootstrapValidator();
    submitBootstrapValidator();
    dataControl();
});
    
function bindSubmitJobSettingsForm() {
    $.ajax({
        type: 'put',
        dataType:'json',
        data:JSON.stringify(dataInfo()),
        url:'/job/update',
        contentType: "application/json",
        success: function(data) {
            window.location="index.html";
        },
        error:function(XMLHttpRequest, textStatus, errorThrown){
            alert ("XMLHttpRequest.status="+XMLHttpRequest.status+"\ntextStatus="+textStatus+"\nerrorThrown=" + errorThrown);
        }
    });
}