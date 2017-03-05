$(function() {
    renderRegCenters();
    bindConnectButtons();
    bindDeleteButtons();
    bindSubmitRegCenterForm();
});

function renderRegCenters() {
    $('#regCenters').bootstrapTable({
        url: 'registry_center',
        method: 'get',
        cache: false,
        columns: [
        {
            field: 'name',
            title: '注册中心名称'
        }, {
            field: 'zkAddressList',
            title: '连接地址'
        }, {
            field: 'namespace',
            title: '命名空间'
        },{
            field: 'digest',
            title: '登录凭证'
        },{
            field: 'oper',
            title: '操作',
            formatter: 'viewoper'
        }]
    });
    renderRegistryCenterForDashboardNav();
    renderJobsForDashboardNav();
    renderJServersForDashboardNav();
}

function viewoper(val, row){
    var operationTd,name = row.name;
    if (true === row.activated) {
        $("#activated-reg-center").text(name);
        operationTd = "<button disabled operation='connect' class='btn' regName='" + name + "'>已连</button><button operation='delete' class='btn btn-danger' data-toggle='modal' id='delete-dialog' regName='" + name + "'>删除</button>";
    } else {
        operationTd = "<button operation='connect' class='btn btn-primary' regName='" + name + "' data-loading-text='切换中...'>连接</button><button operation='delete' class='btn btn-danger' data-toggle='modal' id='delete-dialog' regName='" + name + "'>删除</button>";
    }
    return operationTd;
}

function bindConnectButtons() {
    $(document).on("click", "button[operation='connect']", function(event) {
        var btn = $(this).button("loading");
        var regName = $(event.currentTarget).attr("regName");
        var currentConnectBtn = $(event.currentTarget);
        $.ajax({
            url:"registry_center/connect",
            type:"POST",
            data: JSON.stringify({"name" : regName}),
            contentType:"application/json",
            dataType:"json",
            success: function(data){
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
            }
        });
    });
}

function bindDeleteButtons() {
    $(document).on("click", "button[operation='delete']", function(event) {
        $('#delete-confirm-dialog').modal();
        var regName = $(event.currentTarget).attr("regName");
        var tr = $(event.currentTarget).parent().parent();
        $(document).off("click", "#delete-confirm-dialog-confirm-btn");
        $(document).on("click", "#delete-confirm-dialog-confirm-btn", function() {
            $.ajax({
                url:"registry_center/delete",
                type:"POST",
                data: JSON.stringify({"name" : regName}),
                contentType:"application/json",
                dataType:"json",
                success: function(){
                    tr.empty();
                    $("#delete-confirm-dialog").modal("hide");
                    $(".modal-backdrop").remove();
                    $("body").removeClass('modal-open');
                    renderRegistryCenterForDashboardNav();
                }
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
        $.ajax({
            url:"registry_center",
            type:"POST",
            data: JSON.stringify({"name": name, "zkAddressList": zkAddressList, "namespace": namespace, "digest": digest}),
            contentType:"application/json",
            dataType:"json",
            success: function(data) {
                if (data) {
                    $('#add-reg-center').on('hide.bs.modal', function () {
                        $('#add-reg-center-form')[0].reset();
                    });
                    $("#add-reg-center").modal("hide");
                    $("#regCenters").bootstrapTable('refresh');
                    $(".modal-backdrop").remove();
                    $("body").removeClass('modal-open');
                    showSuccessDialog();
                    renderRegistryCenterForDashboardNav();
                } else {
                    showFailureDialog("add-reg-center-failure-dialog");
                }
            }
        });
    });
}

$('#add-register').click(function() {
    $('#add-reg-center').modal();
});
