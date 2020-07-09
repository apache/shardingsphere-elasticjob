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

function i18n(lang) {
    jQuery.i18n.properties({
        name : 'message',
        path : '/i18n/',
        mode : 'map',
        language : lang,
        cache: true,
        encoding: 'UTF-8',
        callback : function() {
            for (var i in $.i18n.map) {
                $('[data-lang="'+i+'"]').html($.i18n.prop(i));
            }
        }
    });
}

function doLocale() {
    if ($("#content-right").hasClass("lang-en")) {
        i18n("en");
    } else {
        i18n("zh");
    }
}

function switchLanguage() {
    $("#lang-zh").click(function() {
        $("#content-right").removeClass("lang-en").addClass("lang-zh");
        doLocale();
    });
    $("#lang-en").click(function() {
        $("#content-right").removeClass("lang-zh").addClass("lang-en");
        doLocale();
    });
}

function tooltipLocale(){
    for (var i = 0; i < $("[data-toggle='tooltip']").length; i++) {
        var object = $("[data-toggle='tooltip']")[i];
        $(object).attr('title',$.i18n.prop("placeholder-" + object.getAttribute("id"))).tooltip('fixTitle');
    }
}
