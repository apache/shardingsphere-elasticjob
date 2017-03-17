$(function() {
    $(".nav-header").click(function() {
        $(this).parent().children("" + $(this).hash + "").toggle(200);
    });
    renderRegCenterForDashboardNav();
    renderDataSourceForDashboardNav();
    switchRegCenter();
    switchDataSource();
});

function renderRegCenterForDashboardNav() {
    $.get("registry_center", {}, function(data) {
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
    $.get("data_source", {}, function(data) {
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
        event.preventDefault();
        var link = $(this).button("loading");
        var regName = $(event.currentTarget).attr("reg-name");
        $.ajax({
            url: "registry_center/connect",
            type: "POST",
            data: JSON.stringify({"name" : regName}),
            contentType: "application/json",
            dataType: "json",
            success: function(data) {
                if (data) {
                    showSuccessDialog();
                    $("#reg-centers").bootstrapTable("refresh");
                    renderRegCenterForDashboardNav();
                    renderJobsForDashboardNav();
                    renderJServersForDashboardNav();
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
            url: "data_source/connect",
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

function renderJobsForDashboardNav() {
    if ("未连接" === $("#activated-reg-center").text()) {
        return;
    }
    $.get("job/jobs", {}, function (data) {
        var currentJob = $("#job-name").text();
        var $jobsDimension = $("#jobs-dimension");
        $jobsDimension.empty();
        for (var i = 0; i < data.length; i++) {
            var liContent = "<a href='index.html?jobName=" + data[i].jobName + "&jobType=" + data[i].jobType + "' data-placement='right' title='" + data[i].description + "'>" + data[i].jobName + "</a>";
            if (currentJob && currentJob === data[i].jobName) {
                $jobsDimension.append("<li class='open'>" + liContent + "</li>");
            } else {
                $jobsDimension.append("<li>" + liContent + "</li>");
            }
        }
    });
}

function renderJServersForDashboardNav() {
    if ("未连接" === $("#activated-reg-center").text()) {
        return;
    }
    $.get("server/servers", {}, function (data) {
        var currentIp = $("#server-ip").text();
        var $serversDimension = $("#servers-dimension");
        $serversDimension.empty();
        for (var i = 0; i < data.length; i++) {
            var liContent = "<a href='index.html?serverIp=" + data[i].serverIp + "' data-placement='right' title='" + data[i].serverHostName + "'>" + data[i].serverIp + "</a>";
            if (currentIp && currentIp === data[i].serverIp) {
                $serversDimension.append("<li class='open'>" + liContent + "</li>");
            } else {
                $serversDimension.append("<li>" + liContent + "</li>");
            }
        }
    });
}
