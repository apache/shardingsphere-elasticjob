$(function() {
    $('.tree-toggle').click(function() {
        $(this).parent().children('ul.tree').toggle(200);
    });
    renderRegistryCenterForDashboardNav();
    bindConnectLink();
    renderJobsForDashboardNav();
    renderJServersForDashboardNav();
});

function renderRegistryCenterForDashboardNav() {
    $.get("registry_center", {}, function(data) {
        var activatedRegCenter = $("#activated-reg-center").text();
        var $registryCenterDimension = $("#registry-center-dimension");
        $registryCenterDimension.empty();
        for (var i = 0; i < data.length; i++) {
            var regName = data[i].name;
            var liContent = "<a href='#' reg-name='" + regName + "' data-loading-text='切换中...'>" + regName + "</a>";
            if (activatedRegCenter && activatedRegCenter === regName) {
                $registryCenterDimension.append("<li class='open'>" + liContent + "</li>");
            } else {
                $registryCenterDimension.append("<li>" + liContent + "</li>");
            }
        }
    });
}

function bindConnectLink() {
    $(document).on("click", "a[reg-name]", function(event) {
        event.preventDefault();
        var link = $(this).button("loading");
        var regName = $(event.currentTarget).attr("reg-name");
        $.post("registry_center/connect", {name : regName}, function (data) {
            if (data) {
                window.location = "overview";
            } else {
                link.button("reset");
                showFailureDialog("connect-reg-center-failure-dialog");
            }
        });
    });
}

function renderJobsForDashboardNav() {
    if ('未连接' === $("#activated-reg-center").text()) {
        return;
    }
    $.get("job/jobs", {}, function (data) {
        var currentJob = $("#job-name").text();
        var $jobsDimension = $("#jobs-dimension");
        $jobsDimension.empty();
        for (var i = 0; i < data.length; i++) {
            var liContent = "<a href='job_detail?jobName=" + data[i].jobName + "&jobType=" + data[i].jobType + "' data-placement='right' title='" + data[i].description + "'>" + data[i].jobName + "</a>";
            if (currentJob && currentJob === data[i].jobName) {
                $jobsDimension.append("<li class='open'>" + liContent + "</li>");
            } else {
                $jobsDimension.append("<li>" + liContent + "</li>");
            }
        }
    });
}

function renderJServersForDashboardNav() {
    if ('未连接' === $("#activated-reg-center").text()) {
        return;
    }
    $.get("server/servers", {}, function (data) {
        var currentIp = $("#server-ip").text();
        var $serversDimension = $("#servers-dimension");
        $serversDimension.empty();
        for (var i = 0; i < data.length; i++) {
            var liContent = "<a href='server_detail?serverIp=" + data[i].serverIp + "' data-placement='right' title='" + data[i].serverHostName + "'>" + data[i].serverIp + "</a>";
            if (currentIp && currentIp === data[i].serverIp) {
                $serversDimension.append("<li class='open'>" + liContent + "</li>");
            } else {
                $serversDimension.append("<li>" + liContent + "</li>");
            }
        }
    });
}
