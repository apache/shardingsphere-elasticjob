$(function() {
    renderRegCenters();
    bindConnectButtons();
    bindDeleteButtons();
    bindSubmitRegCenterForm();
});

function renderRegCenters() {
    $.get("registry_center", {}, function(data) {
        $("#regCenters tbody").empty();
        for (var i = 0;i < data.length;i++) {
            var baseTd = "<td>" + data[i].name + "</td><td>" + data[i].zkAddressList + "</td><td>" + data[i].namespace + "</td><td>" + data[i].digest + "</td>";
            var operationTd;
            if (true === data[i].activated) {
                $("#activated-reg-center").text(data[i].name);
                operationTd = "<td><button disabled operation='connect' class='btn' regName='" + data[i].name + "'>已连</button><button operation='delete' class='btn btn-danger' data-toggle='modal' data-target='#delete-confirm-dialog' regName='" + data[i].name + "'>删除</button></td>";
            } else {
                operationTd = "<td><button operation='connect' class='btn btn-primary' regName='" + data[i].name + "' data-loading-text='切换中...'>连接</button><button operation='delete' class='btn btn-danger' data-toggle='modal' data-target='#delete-confirm-dialog' regName='" + data[i].name + "'>删除</button></td>";
            }
            $("#regCenters tbody").append("<tr>" + baseTd + operationTd + "</tr>");
        }
        renderRegistryCenterForDashboardNav();
        renderJobsForDashboardNav();
        renderJServersForDashboardNav();
    });
}

function bindConnectButtons() {
    $(document).on("click", "button[operation='connect']", function(event) {
    	var btn = $(this).button("loading");
        var regName = $(event.currentTarget).attr("regName");
        var currentConnectBtn = $(event.currentTarget);
        $.post("registry_center/connect", {name : regName}, function (data) {
            if (data) {
                $("#activated-reg-center").text(regName);
                var connectButtons = $('button[operation="connect"]');
                connectButtons.text("连接");
                connectButtons.addClass("btn-primary");
                connectButtons.attr("disabled", false);
                currentConnectBtn.attr("disabled", true);
                currentConnectBtn.removeClass("btn-primary");
                currentConnectBtn.text("已连");
                renderJobsForDashboardNav();
                showSuccessDialog();
                renderRegistryCenterForDashboardNav();
                renderJServersForDashboardNav();
            } else {
                showFailureDialog("connect-reg-center-failure-dialog");
            }
            btn.button("reset");
        });
    });
}

function bindDeleteButtons() {
    $(document).on("click", "button[operation='delete']", function(event) {
        var regName = $(event.currentTarget).attr("regName");
        var tr = $(event.currentTarget).parent().parent();
        $(document).off("click", "#delete-confirm-dialog-confirm-btn");
        $(document).on("click", "#delete-confirm-dialog-confirm-btn", function(event) {
            $.post("registry_center/delete", {name : regName}, function (data) {
                tr.empty();
                $("#delete-confirm-dialog").modal("hide");
                renderRegistryCenterForDashboardNav();
            });
        });
    });
}

function bindSubmitRegCenterForm() {
    $("#add-reg-center-form").submit(function(event) {
        event.preventDefault();
        var name = $("#name").val();
        var zkAddressList = $("#zkAddressList").val();
        var namespace = $("#namespace").val();
        var digest = $("#digest").val();
        $.post("registry_center", {name: name, zkAddressList: zkAddressList, namespace: namespace, digest: digest}, function(data) {
            $("#add-reg-center").modal("hide");
            if (data) {
                var baseTd = "<td>" + name + "</td><td>" + zkAddressList + "</td><td>" + namespace + "</td><td>" + digest + "</td>";
                var operationTd;
                if (name != $("#activated-reg-center").text()) {
                    operationTd = "<td><button operation='connect' class='btn btn-primary' regName='" + name + "'>连接</button><button operation='delete' class='btn btn-danger' data-toggle='modal' data-target='#delete-confirm-dialog' regName='" + name + "'>删除</button></td>";
                } else {
                    operationTd = "<td><button disabled operation='connect' class='btn' regName='" + name + "'>已连</button><button operation='delete' class='btn btn-danger' data-toggle='modal' data-target='#delete-confirm-dialog' regName='" + name + "'>删除</button></td>";
                }
                
                $("#regCenters tbody").append("<tr>" + baseTd + operationTd + "</tr>");
                showSuccessDialog();
                renderRegistryCenterForDashboardNav();
            } else {
                showFailureDialog("add-reg-center-failure-dialog");
            }
        });
    });
}
