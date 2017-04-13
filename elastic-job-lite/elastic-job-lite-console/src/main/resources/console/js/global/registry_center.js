$(function() {
    renderRegCenters();
    validate();
    dealRegCenterModal();
    handleFieldValidator();
    submitRegCenter();
});

function renderRegCenters() {
    $("#reg-centers").bootstrapTable({
        url: "api/registry-center",
        cache: false,
        search: true,
        showRefresh: true,
        showColumns: true,
        columns: 
        [{
            field: "name",
            title: "注册中心名称",
            sortable: true
        }, {
            field: "zkAddressList",
            title: "连接地址",
            sortable: true
        }, {
            field: "namespace",
            title: "命名空间",
            sortable: true
        }, {
            field: "digest",
            title: "登录凭证"
        }, {
            field: "operation",
            title: "操作",
            formatter: "generateOperationButtons"
        }],
        onLoadSuccess: function() {
            bindButtons();
        },
        onSort: function(name, order) {
            $("#reg-centers").bootstrapTable("refresh");
        }
    });
    renderRegCenterForDashboardNav();
}

function generateOperationButtons(val, row) {
    var operationTd;
    var name = row.name;
    if (row.activated) {
        operationTd = "<button disabled operation='connect' class='btn-xs' regName='" + name + "'>已连</button>&nbsp;<button operation='delete' class='btn-xs btn-danger' data-toggle='modal' id='delete-dialog' regName='" + name + "'>删除</button>";
    } else {
        operationTd = "<button operation='connect' class='btn-xs btn-primary' regName='" + name + "' data-loading-text='切换中...'>连接</button>&nbsp;<button operation='delete' class='btn-xs btn-danger' data-toggle='modal' id='delete-dialog' regName='" + name + "'>删除</button>";
    }
    return operationTd;
}

function bindButtons() {
    bindConnectButtons();
    bindDeleteButtons();
}

function bindConnectButtons() {
    $("button[operation='connect']").click(function(event) {
        var btn = $(this).button("loading");
        var regName = $(event.currentTarget).attr("regName");
        $.ajax({
            url: "api/registry-center/connect",
            type: "POST",
            data: JSON.stringify({"name" : regName}),
            contentType: "application/json",
            dataType: "json",
            success: function(data) {
                if (data) {
                    $("#reg-centers").bootstrapTable("refresh");
                    renderRegCenterForDashboardNav();
                    refreshJobNavTag();
                    refreshServerNavTag();
                    showSuccessDialog();
                } else {
                    showFailureDialog("switch-reg-center-failure-dialog");
                }
                btn.button("reset");
            }
        });
    });
}

function bindDeleteButtons() {
    $("button[operation='delete']").click(function(event) {
        $("#delete-confirm-dialog").modal({backdrop: 'static', keyboard: true});
        var regName = $(event.currentTarget).attr("regName");
        $(document).off("click", "#delete-confirm-dialog-confirm-btn");
        $(document).on("click", "#delete-confirm-dialog-confirm-btn", function() {
            $.ajax({
                url: "api/registry-center",
                type: "DELETE",
                data: JSON.stringify({"name" : regName}),
                contentType: "application/json",
                dataType: "json",
                success: function() {
                    $("#reg-centers").bootstrapTable("refresh");
                    $("#delete-confirm-dialog").modal("hide");
                    $(".modal-backdrop").remove();
                    $("body").removeClass("modal-open");
                    renderRegCenterForDashboardNav();
                    refreshRegCenterNavTag();
                }
            });
        });
    });
}

function dealRegCenterModal() {
    $("#add-register").click(function() {
        $("#add-reg-center").modal({backdrop: 'static', keyboard: true});
    });
    $("#close-add-reg-form").click(function() {
        $("#add-reg-center").on("hide.bs.modal", function () {
            $("#reg-center-form")[0].reset();
        });
        $("#reg-center-form").data("bootstrapValidator").resetForm();
    });
}

function handleFieldValidator() {
    $("#digest").focus(function() {
        $("#reg-center-form").data("bootstrapValidator").enableFieldValidators("digest", true);
    });
    $("#digest").blur(function() {
        $("#reg-center-form").data("bootstrapValidator").enableFieldValidators("digest", "" === $("#digest").val() ? false : true);
    });
    $("#namespace").focus(function() {
        $("#reg-center-form").data("bootstrapValidator").enableFieldValidators("namespace", true);
    });
    $("#namespace").blur(function() {
        $("#reg-center-form").data("bootstrapValidator").enableFieldValidators("namespace", "" === $("#namespace").val() ? false : true);
    });
}

function submitRegCenter() {
    $("#add-reg-center-btn").on("click", function(event) {
        if ("" === $("#digest").val()) {
            $("#reg-center-form").data("bootstrapValidator").enableFieldValidators("digest", false);
        }
        if ("" === $("#namespace").val()) {
            $("#reg-center-form").data("bootstrapValidator").enableFieldValidators("namespace", false);
        }
        var bootstrapValidator = $("#reg-center-form").data("bootstrapValidator");
        bootstrapValidator.validate();
        if(bootstrapValidator.isValid()) {
            var name = $("#name").val();
            var zkAddressList = $("#zk-address-list").val();
            var namespace = $("#namespace").val();
            var digest = $("#digest").val();
            $.ajax({
                url: "api/registry-center",
                type: "POST",
                data: JSON.stringify({"name": name, "zkAddressList": zkAddressList, "namespace": namespace, "digest": digest}),
                contentType: "application/json",
                dataType: "json",
                success: function(data) {
                    if (data) {
                        $("#add-reg-center").on("hide.bs.modal", function() {
                            $("#reg-center-form")[0].reset();
                        });
                        $("#reg-center-form").data("bootstrapValidator").resetForm();
                        $("#add-reg-center").modal("hide");
                        $("#reg-centers").bootstrapTable("refresh");
                        $(".modal-backdrop").remove();
                        $("body").removeClass("modal-open");
                        renderRegCenterForDashboardNav();
                        refreshRegCenterNavTag();
                    } else {
                        showFailureDialog("add-reg-center-failure-dialog");
                    }
                }
            });
        }
    });
}

function validate() {
    $("#reg-center-form").bootstrapValidator({
        message: "This value is not valid",
        feedbackIcons: {
            valid: "glyphicon glyphicon-ok",
            invalid: "glyphicon glyphicon-remove",
            validating: "glyphicon glyphicon-refresh"
        },
        fields: {
            name: {
                validators: {
                    notEmpty: {
                        message: "注册中心不能为空"
                    },
                    stringLength: {
                        max: 50,
                        message: "注册中心长度不能超过50字符大小"
                    },
                    regexp: {
                        regexp: /^([a-zA-Z0-9_]+(-|\.))*[a-zA-Z0-9_]+$/,
                        message: "注册中心包含非法字符"
                    }
                }
            },
            zkAddressList: {
                validators: {
                    notEmpty: {
                        message: "注册中心地址不能为空"
                    },
                    stringLength: {
                        max: 100,
                        message: "注册中心地址长度不能超过100字符大小"
                    },
                    regexp: {
                        regexp: /^[^?!@#$%\^&*()'';""\]\[<>\\`~{}|=+/]+$/,
                        message: "注册中心地址包含非法字符"
                    }
                }
            },
            namespace: {
                validators: {
                    stringLength: {
                        max: 50,
                        message: "命名空间长度不能超过50字符大小"
                    },
                    regexp: {
                        regexp: /^[^?!@#$%\^&*()'',.;:""\]\[<>\\`~{}|=+/]+$/,
                        message: "命名空间包含非法字符"
                    }
                }
            },
            digest: {
                validators: {
                    stringLength: {
                        max: 20,
                        message: "登录凭证长度不能超过20字符大小"
                    },
                    regexp: {
                        regexp: /^[^?!@#$%\^&*()'',.;:""\]\[<>\\`~{}|=+/]+$/,
                        message: "登录凭证包含非法字符"
                    }
                }
            }
        }
    });
    $("#reg-center-form").submit(function(event) {
        event.preventDefault();
    });
}
