$(function() {
    renderRegCenters();
    validate();
    bindConnectButtons();
    bindDeleteButtons();
    dealRegCenterModal();
    handleFieldValidator();
    submitRegCenter();
});

function renderRegCenters() {
    $("#reg-centers").bootstrapTable({
        url: "registry_center",
        method: "get",
        cache: false,
        columns: 
        [{
            field: "name",
            title: "注册中心名称"
        }, {
            field: "zkAddressList",
            title: "连接地址"
        }, {
            field: "namespace",
            title: "命名空间"
        }, {
            field: "digest",
            title: "登录凭证"
        }, {
            field: "operation",
            title: "操作",
            formatter: "generateOperationButtons"
        }]
    });
    renderRegCenterForDashboardNav();
}

function generateOperationButtons(val, row) {
    var operationTd;
    var name = row.name;
    if (row.activated) {
        $("#activated-reg-center").text(name);
        operationTd = "<button disabled operation='connect' class='btn-xs' regName='" + name + "'>已连</button><button operation='delete' class='btn-xs btn-danger' data-toggle='modal' id='delete-dialog' regName='" + name + "'>删除</button>";
    } else {
        operationTd = "<button operation='connect' class='btn-xs btn-primary' regName='" + name + "' data-loading-text='切换中...'>连接</button><button operation='delete' class='btn-xs btn-danger' data-toggle='modal' id='delete-dialog' regName='" + name + "'>删除</button>";
    }
    return operationTd;
}

function bindConnectButtons() {
    $(document).on("click", "button[operation='connect']", function(event) {
        var btn = $(this).button("loading");
        var regName = $(event.currentTarget).attr("regName");
        $.ajax({
            url: "registry_center/connect",
            type: "POST",
            data: JSON.stringify({"name" : regName}),
            contentType: "application/json",
            dataType: "json",
            success: function(data) {
                if (data) {
                    $("#activated-reg-center").text(regName);
                    $("#reg-centers").bootstrapTable("refresh");
                    renderRegCenterForDashboardNav();
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
    $(document).on("click", "button[operation='delete']", function(event) {
        $("#delete-confirm-dialog").modal();
        var regName = $(event.currentTarget).attr("regName");
        $(document).off("click", "#delete-confirm-dialog-confirm-btn");
        $(document).on("click", "#delete-confirm-dialog-confirm-btn", function() {
            $.ajax({
                url: "registry_center/delete",
                type: "POST",
                data: JSON.stringify({"name" : regName}),
                contentType: "application/json",
                dataType: "json",
                success: function() {
                    $("#reg-centers").bootstrapTable("refresh");
                    $("#delete-confirm-dialog").modal("hide");
                    $(".modal-backdrop").remove();
                    $("body").removeClass("modal-open");
                    renderRegCenterForDashboardNav();
                }
            });
        });
    });
}

function dealRegCenterModal() {
    $("#add-register").click(function() {
        $("#add-reg-center").modal();
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
                url: "registry_center",
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
                        showSuccessDialog();
                        renderRegCenterForDashboardNav();
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
                        max: 30,
                        message: "注册中心长度不能超过30字符大小"
                    },
                    regexp: {
                        regexp: /^[^?!@#$%\^&*()'',.;:""\]\[<>\\`~{}|=+/]+$/,
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
                        max: 60,
                        message: "注册中心地址长度不能超过60字符大小"
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
                        max: 30,
                        message: "命名空间长度不能超过30字符大小"
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
