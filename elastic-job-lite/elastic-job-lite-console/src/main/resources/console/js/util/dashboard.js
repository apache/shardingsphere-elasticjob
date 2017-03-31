$(function() {
    renderRegCenterForDashboardNav();
    renderDataSourceForDashboardNav();
    switchRegCenter();
    switchDataSource();
    renderSkin();
    controlSubMenuStyle();
    controlDropdownMenuStyle();
    refreshRegCenterNavTag();
    refreshEventTraceNavTag();
});

function renderRegCenterForDashboardNav() {
    $.get("api/registry-center", {}, function(data) {
        var index;
        for (index = 0; index < data.length; index++) {
            if (data[index].activated) {
                $("#activated-reg-center").text(data[index].name);
            }
        }
        var activatedRegCenter = $("#activated-reg-center").text();
        var $registryCenterDimension = $("#registry-center-dimension");
        $registryCenterDimension.empty();
        for (index = 0; index < data.length; index++) {
            var regName = data[index].name;
            var liContent = "<a href='#' reg-name='" + regName + "' data-loading-text='切换中...'>" + regName + "</a>";
            if (activatedRegCenter && activatedRegCenter === regName) {
                $registryCenterDimension.append("<li class='open'>" + liContent + "</li>");
            } else {
                $registryCenterDimension.append("<li>" + liContent + "</li>");
            }
        }
    });
}

function renderDataSourceForDashboardNav() {
    $.get("api/data-source", {}, function(data) {
        var index;
        for (index = 0; index < data.length; index++) {
            if (data[index].activated) {
                $("#activated-data-source").text(data[index].name);
            }
        }
        var activatedDataSource = $("#activated-data-source").text();
        var $dataSourceDimension = $("#data-source-dimension");
        $dataSourceDimension.empty();
        for (index = 0; index < data.length; index++) {
            var dataSourceName = data[index].name;
            var liContent = "<a href='#' data-source-name='" + dataSourceName + "' data-loading-text='切换中...'>" + dataSourceName + "</a>";
            if (activatedDataSource && activatedDataSource === dataSourceName) {
                $dataSourceDimension.append("<li class='open'>" + liContent + "</li>");
            } else {
                $dataSourceDimension.append("<li>" + liContent + "</li>");
            }
        }
    });
}

function switchRegCenter() {
    $(document).on("click", "a[reg-name]", function(event) {
        var link = $(this).button("loading");
        var regName = $(event.currentTarget).attr("reg-name");
        $.ajax({
            url: "api/registry-center/connect",
            type: "POST",
            data: JSON.stringify({"name" : regName}),
            contentType: "application/json",
            dataType: "json",
            success: function(data) {
                if (data) {
                    showSuccessDialog();
                    $("#reg-centers").bootstrapTable("refresh");
                    renderRegCenterForDashboardNav();
                    refreshJobNavTag();
                } else {
                    link.button("reset");
                    showFailureDialog("switch-reg-center-failure-dialog");
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
                } else {
                    link.button("reset");
                    showFailureDialog("switch-data-source-failure-dialog");
                }
            }
        });
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

function controlDropdownMenuStyle() {
    $("a.dropdown-toggle").click(function() {
        if (0 === $(this).parent().children("ul").children("li").length) {
            $(this).parent().children("ul").hide();
        }
    });
}

function refreshJobNavTag() {
    $.ajax({
        url: "/api/jobs/config",
        cache: false,
        success: function(data) {
            $("#job-nav-tag").text(data.length);
        }
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
                    } else {
                        $("#job-nav-tag").text("0");
                    }
                }
            } else {
                $("#job-nav-tag").text("0");
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
