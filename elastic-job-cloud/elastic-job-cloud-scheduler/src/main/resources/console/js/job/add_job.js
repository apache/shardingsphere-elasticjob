$(function(){
    bootstrapValidator();
    dataControl();
    submitBootstrapValidator();
});
    
function bindSubmitJobSettingsForm() {
    $.ajax({
        type: 'post',
        dataType:'json',
        data:JSON.stringify(dataInfo()),
        url:'/job/register',
        contentType: "application/json",
        success: function(data) {
            window.location="index.html";
        },
        error:function(XMLHttpRequest, textStatus, errorThrown){
            alert ("XMLHttpRequest.status="+XMLHttpRequest.status+"\ntextStatus="+textStatus+"\nerrorThrown=" + errorThrown);
        }
    });
}