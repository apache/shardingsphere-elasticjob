$(function() {
    renderRegCenterForDashboardNav();
    renderDataSourceForDashboardNav();
    switchRegCenter();
    switchDataSource();
    renderSkin();
    controlSubMenuStyle();
    refreshRegCenterNavTag();
    refreshEventTraceNavTag();
});

function renderRegCenterForDashboardNav() {
    $.get("api/registry-center", {}, function(data) {
        var index;
        var activatedRegCenterName;
        for (index = 0; index < data.length; index++) {
            if (data[index].activated) {
                activatedRegCenterName = data[index].name;
            }
        }
        var registryCenterDimension = $("#registry-center-dimension");
        registryCenterDimension.empty();
        for (index = 0; index < data.length; index++) {
            var regCenterName = data[index].name;
            var regCenterDisplayName;
            if (activatedRegCenterName && activatedRegCenterName === regCenterName) {
                regCenterDisplayName = "<b>" + regCenterName + "&nbsp;&nbsp;(已连接)</b>";
            } else {
                regCenterDisplayName = regCenterName;
            }
            registryCenterDimension.append("<li><a href='#' reg-name='" + regCenterName + "' data-loading-text='切换中...'>" + regCenterDisplayName + "</a></li>");
        }
        if (0 === data.length) {
            registryCenterDimension.hide();
        }
    });
    $(document).on("click", "#registry-center-dimension-link", function(event) {
        var $regCenterDimension = $("#registry-center-dimension");
        if ($regCenterDimension.children("li").length > 0) {
            $regCenterDimension.css("display", "");
        }
    });
}

function renderDataSourceForDashboardNav() {
    $.get("api/data-source", {}, function(data) {
        var index;
        var activatedDataSource;
        for (index = 0; index < data.length; index++) {
            if (data[index].activated) {
                activatedDataSource = data[index].name;
            }
        }
        var dataSourceDimension = $("#data-source-dimension");
        dataSourceDimension.empty();
        for (index = 0; index < data.length; index++) {
            var dataSourceName = data[index].name;
            var dataSourceDisplayName;
            if (activatedDataSource && activatedDataSource === dataSourceName) {
                dataSourceDisplayName = "<b>" + dataSourceName + "&nbsp;&nbsp;(已连接)</b>";
            } else {
                dataSourceDisplayName = dataSourceName;
            }
            dataSourceDimension.append("<li><a href='#' data-source-name='" + dataSourceName + "' data-loading-text='切换中...'>" + dataSourceDisplayName + "</a></li>");
        }
        if (0 === data.length) {
            dataSourceDimension.hide();
        }
    });
    $(document).on("click", "#data-source-dimension-link", function(event) {
        var $dataSourceDimension = $("#data-source-dimension");
        if ($dataSourceDimension.children("li").length > 0) {
            $dataSourceDimension.css("display", "");
        }
    });
}

function switchRegCenter() {
    $(document).on("click", "a[reg-name]", function(event) {
        var link = $(this).button("loading");
        var regCenterName = $(event.currentTarget).attr("reg-name");
        $.ajax({
            url: "api/registry-center/connect",
            type: "POST",
            data: JSON.stringify({"name" : regCenterName}),
            contentType: "application/json",
            dataType: "json",
            success: function(data) {
                if (data) {
                    showSuccessDialog();
                    $("#reg-centers").bootstrapTable("refresh");
                    renderRegCenterForDashboardNav();
                    refreshJobNavTag();
                    refreshServerNavTag();
                    $("#content").load("html/global/registry_center.html");
                    renderSidebarMenu($("#settings"));
                    $("#reg-center").parent().addClass("active");
                } else {
                    link.button("reset");
                    showFailureDialog("操作未成功，原因：连接失败，请检查注册中心配置");
                }
            }
        });
    });
}

function switchDataSource() {
    $(document).on("click", "a[data-source-name]", function(event) {
        event.preventDefault();
        var link = $(this).button("loading");
        var dataSourceName = $(event.currentTarget).attr("data-source-name");
        $.ajax({
            url: "api/data-source/connect",
            type: "POST",
            data: JSON.stringify({"name" : dataSourceName}),
            contentType: "application/json",
            dataType: "json",
            success: function(data) {
                if (data) {
                    showSuccessDialog();
                    $("#data-sources").bootstrapTable("refresh");
                    renderDataSourceForDashboardNav();
                    $("#content").load("html/global/event_trace_data_source.html");
                    renderSidebarMenu($("#settings"));
                    $("#event-trace-data-source").parent().addClass("active");
                } else {
                    link.button("reset");
                    showFailureDialog("操作未成功，原因：连接失败，请检查事件追踪数据源配置");
                }
            }
        });
    });
}

function renderSidebarMenu(div) {
    div.parent().children().removeClass("active");
    div.parent().children().children().children("li").removeClass("active");
    div.parent().children().children("ul").css("display","");
    div.addClass("active");
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

function refreshRegCenterNavTag() {
    $.ajax({
        url: "api/registry-center",
        cache: false,
        success: function(data) {
            $("#reg-nav-tag").text(data.length);
            if (data.length > 0) {
                for (var index = 0; index < data.length; index++) {
                    if (data[index].activated) {
                        refreshJobNavTag();
                        refreshServerNavTag();
                    } else {
                        $("#job-nav-tag").text("0");
                        $("#server-nav-tag").text("0");
                    }
                }
            } else {
                $("#job-nav-tag").text("0");
                $("#server-nav-tag").text("0");
            }
        }
    });
}

function refreshEventTraceNavTag() {
    $.ajax({
        url: "api/data-source",
        cache: false,
        success: function(data) {
            $("#event-trace-nav-tag").text(data.length);
        }
    });
}

function refreshJobNavTag() {
    $.ajax({
        url: "/api/jobs/count",
        cache: false,
        success: function(data) {
            $("#job-nav-tag").text(data);
        }
    });
}

function refreshServerNavTag() {
    $.ajax({
        url: "/api/servers/count",
        cache: false,
        success: function(data) {
            $("#server-nav-tag").text(data);
        }
    });
}
