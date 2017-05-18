$(function() {
    authorityControl();
    renderRegCenters();
    validate();
    dealRegCenterModal();
    handleFieldValidator();
    submitRegCenter();
    bindButtons();
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
        }]
    });
    renderRegCenterForDashboardNav();
}

function generateOperationButtons(val, row) {
    var operationTd;
    var name = row.name;
    if (row.activated) {
        operationTd = "<button disabled operation='connect-reg-center' class='btn-xs' regName='" + name + "'>已连</button>&nbsp;<button operation='delete-reg-center' class='btn-xs btn-danger' data-toggle='modal' id='delete-dialog' regName='" + name + "'>删除</button>";
    } else {
        operationTd = "<button operation='connect-reg-center' class='btn-xs btn-info' regName='" + name + "' data-loading-text='切换中...'>连接</button>&nbsp;<button operation='delete-reg-center' class='btn-xs btn-danger' data-toggle='modal' id='delete-dialog' regName='" + name + "'>删除</button>";
    }
    return operationTd;
}

function bindButtons() {
    bindConnectButtons();
    bindDeleteButtons();
}

function bindConnectButtons() {
    $(document).off("click", "button[operation='connect-reg-center']");
    $(document).on("click", "button[operation='connect-reg-center']", function(event) {
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
                    showFailureDialog("操作未成功，原因：连接失败，请检查注册中心配置");
                }
                btn.button("reset");
            }
        });
    });
}

function bindDeleteButtons() {
    $(document).off("click", "button[operation='delete-reg-center']");
    $(document).on("click", "button[operation='delete-reg-center']", function(event) {
        showDeleteConfirmModal();
        var regName = $(event.currentTarget).attr("regName");
        $(document).off("click", "#confirm-btn");
        $(document).on("click", "#confirm-btn", function() {
            $.ajax({
                url: "api/registry-center",
                type: "DELETE",
                data: JSON.stringify({"name" : regName}),
                contentType: "application/json",
                dataType: "json",
                success: function() {
                    $("#reg-centers").bootstrapTable("refresh");
                    $("#confirm-dialog").modal("hide");
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
                        message: "注册中心名称不能为空"
                    },
                    stringLength: {
                        max: 50,
                        message: "注册中心名称长度不能超过50字符大小"
                    },
                    regexp: {
                        regexp: /^[\w\.-]+$/,
                        message: "注册中心名称只能使用数字、字母、下划线(_)、短横线(-)和点号(.)"
                    },
                    callback: {
                        message: "注册中心已经存在",
                        callback: function() {
                            var regName = $("#name").val();
                            var result = true;
                            $.ajax({
                                url: "api/registry-center",
                                contentType: "application/json",
                                async: false,
                                success: function(data) {
                                    for (var index = 0; index < data.length; index++) {
                                        if (regName === data[index].name) {
                                            result = false;
                                        }
                                    }
                                }
                            });
                            return result;
                        }
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
                        regexp: /^[\w\.-]+$/,
                        message: "命名空间只能使用数字、字母、下划线(_)、短横线(-)和点号(.)"
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
                        regexp: /^[\w\.-]+$/,
                        message: "登录凭证只能使用数字、字母、下划线(_)、短横线(-)和点号(.)"
                    }
                }
            }
        }
    });
    $("#reg-center-form").submit(function(event) {
        event.preventDefault();
    });
}
