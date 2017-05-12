$(function() {
    renderSkin();
    controlSubMenuStyle();
});

function showSuccessDialog() {
    $("#success-dialog").modal("show");
    setTimeout('$("#success-dialog").modal("hide")', 2000);
}

function showFailDialog() {
    $("#fail-dialog").modal("show");
    setTimeout('$("#fail-dialog").modal("hide")', 2000);
}

function refreshJobNavTag() {
    $.ajax({
        url: "/api/job/jobs",
        cache: false,
        success: function(data) {
            $("#job-nav-tag").text(data.length);
        }
    });
}

function refreshAppNavTag() {
    $.ajax({
        url: "/api/app/list",
        cache: false,
        success: function(data) {
            $("#app-nav-tag").text(data.length);
        }
    });
}

var my_skins = [
    "skin-blue",
    "skin-black",
    "skin-red",
    "skin-yellow",
    "skin-purple",
    "skin-green",
    "skin-blue-light",
    "skin-black-light",
    "skin-red-light",
    "skin-yellow-light",
    "skin-purple-light",
    "skin-green-light"
];

function renderSkin() {
    $("[data-skin]").on("click", function(event) {
        event.preventDefault();
        changeSkin($(this).data("skin"));
    });
}

function changeSkin(skinClass) {
    $.each(my_skins, function(index) {
        $("body").removeClass(my_skins[index]);
    });
    $("body").addClass(skinClass);
}

function controlSubMenuStyle() {
    $(".sub-menu").click(function() {
        $(this).parent().parent().children().removeClass("active");
        $(this).parent().addClass("active");
    });
}

function selectAppStatus(appName) {
    var resultValue = null;
    $.ajax({
        type: "GET",
        async: false,
        url: "/api/app/" + appName + "/disable",
        contentType: "application/json",
        success: function(result) {
            resultValue = result;
        }
    });
    return resultValue;
}

function authorityControl() {
    $.ajax({
        type: "HEAD",
        url : "/",
        complete: function(xhr, data) {
            if ("guest" === xhr.getResponseHeader("identify")) {
                $("table").on("all.bs.table", function() {
                    $(".content-wrapper .btn-xs").not(".btn-info").attr("disabled", true);
                    $(".content-wrapper .btn-xs").not(".btn-info").removeClass().addClass("btn-xs");
                });
            }
            if ("" === $("#authority").text()) {
                $("#authority").text(xhr.getResponseHeader("identify"));
            }
        }
    });
}
