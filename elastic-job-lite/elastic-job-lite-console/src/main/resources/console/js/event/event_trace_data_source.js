$(function() {
    renderDataSources();
    validate();
    bindConnectButtons();
    bindDeleteButtons();
    dealDataSourceModal();
    handleFieldValidator();
    submitDataSource();
});

function renderDataSources() {
    $("#data-sources").bootstrapTable({
        url: "data_source",
        method: "get",
        cache: false,
        columns: 
        [{
            field: "name",
            title: "数据源名称"
        }, {
            field: "driver",
            title: "数据源驱动"
        }, {
            field: "url",
            title: "数据源连接地址"
        }, {
            field: "username",
            title: "连接用户名"
        }, {
            field: "operation",
            title: "操作",
            formatter: "generateOperationButtons"
        }]
    });
    renderDataSourceForDashboardNav();
}

function generateOperationButtons(val, row) {
    var operationTd;
    var name = row.name;
    if (row.activated) {
        $("#activated-data-source").text(name);
        operationTd = "<button disabled operation='connectDataSource' class='btn-xs' dataSourceName='" + name + "'>已连</button><button operation='deleteDataSource' class='btn-xs btn-danger' data-toggle='modal' id='delete-dialog' dataSourceName='" + name + "'>删除</button>";
    } else {
        operationTd = "<button operation='connectDataSource' class='btn-xs btn-primary' dataSourceName='" + name + "' data-loading-text='切换中...'>连接</button><button operation='deleteDataSource' class='btn-xs btn-danger' data-toggle='modal' id='delete-dialog' dataSourceName='" + name + "'>删除</button>";
    }
    return operationTd;
}

function bindConnectButtons() {
    $(document).on("click", "button[operation='connectDataSource']", function(event) {
        var btn = $(this).button("loading");
        var dataSourceName = $(event.currentTarget).attr("dataSourceName");
        var currentConnectBtn = $(event.currentTarget);
        $.ajax({
            url: "data_source/connect",
            type: "POST",
            data: JSON.stringify({"name" : dataSourceName}),
            contentType: "application/json",
            dataType: "json",
            success: function(data) {
                if (data) {
                    $("#activated-data-source").text(dataSourceName);
                    $("#data-sources").bootstrapTable("refresh");
                    renderDataSourceForDashboardNav();
                    showSuccessDialog();
                } else {
                    showFailureDialog("switch-data-source-failure-dialog");
                }
                btn.button("reset");
            }
        });
    });
}

function bindDeleteButtons() {
    $(document).on("click", "button[operation='deleteDataSource']", function(event) {
        $("#delete-confirm-dialog").modal();
        var dataSourceName = $(event.currentTarget).attr("dataSourceName");
        $(document).off("click", "#delete-confirm-dialog-confirm-btn");
        $(document).on("click", "#delete-confirm-dialog-confirm-btn", function() {
            $.ajax({
                url: "data_source/delete",
                type: "POST",
                data: JSON.stringify({"name" : dataSourceName}),
                contentType: "application/json",
                dataType: "json",
                success: function() {
                    $("#data-sources").bootstrapTable("refresh");
                    $("#delete-confirm-dialog").modal("hide");
                    $(".modal-backdrop").remove();
                    $("body").removeClass("modal-open");
                    renderDataSourceForDashboardNav();
                }
            });
        });
    });
}

function dealDataSourceModal() {
    $("#add-data-source").click(function() {
        $("#add-data-source-center").modal();
    });
    $("#close-add-data-source-form").click(function() {
        $("#add-data-source-center").on("hide.bs.modal", function () {
            $("#data-source-form")[0].reset();
        });
        $("#data-source-form").data("bootstrapValidator").resetForm();
    });
}

function handleFieldValidator() {
    $("#username").focus(function() {
        $("#data-source-form").data("bootstrapValidator").enableFieldValidators("username", true);
    });
    $("#username").blur(function() {
        $("#data-source-form").data("bootstrapValidator").enableFieldValidators("username", "" === $("#username").val() ? false : true);
    });
    $("#password").focus(function() {
        $("#data-source-form").data("bootstrapValidator").enableFieldValidators("password", true);
    });
    $("#password").blur(function() {
        $("#data-source-form").data("bootstrapValidator").enableFieldValidators("password", "" === $("#password").val() ? false : true);
    });
}

function submitDataSource() {
    $("#add-data-source-btn").on("click", function(event) {
        if ("" === $("#username").val()) {
            $("#data-source-form").data("bootstrapValidator").enableFieldValidators("username", false);
        }
        if ("" === $("#password").val()) {
            $("#data-source-form").data("bootstrapValidator").enableFieldValidators("password", false);
        }
        var bootstrapValidator = $("#data-source-form").data("bootstrapValidator");
        bootstrapValidator.validate();
        if(bootstrapValidator.isValid()) {
            var name = $("#name").val();
            var driver = $("#driver").val();
            var url = $("#url").val();
            var username = $("#username").val();
            var password = $("#password").val();
            $.ajax({
                url: "data_source",
                type: "POST",
                data: JSON.stringify({"name": name, "driver": driver, "url": url, "username": username, "password": password}),
                contentType: "application/json",
                dataType: "json",
                success: function(data) {
                    if (data) {
                        $("#add-data-source-center").on("hide.bs.modal", function() {
                            $("#data-source-form")[0].reset();
                        });
                        $("#data-source-form").data("bootstrapValidator").resetForm();
                        $("#add-data-source-center").modal("hide");
                        $("#data-sources").bootstrapTable("refresh");
                        $(".modal-backdrop").remove();
                        $("body").removeClass("modal-open");
                        showSuccessDialog();
                        renderDataSourceForDashboardNav();
                    } else {
                        showFailureDialog("add-data-source-failure-dialog");
                    }
                }
            });
        }
    });
}

function validate() {
    $("#data-source-form").bootstrapValidator({
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
                        message: "数据源不能为空"
                    },
                    stringLength: {
                        max: 30,
                        message: "数据源长度不能超过30字符大小"
                    },
                    regexp: {
                        regexp: /^[^?!@#$%\^&*()'',.;:""\]\[<>\\`~{}|=+/]+$/,
                        message: "数据源包含非法字符"
                    }
                }
            },
            driver: {
                validators: {
                    notEmpty: {
                        message: "数据源驱动不能为空"
                    },
                    stringLength: {
                        max: 60,
                        message: "数据源驱动长度不能超过60字符大小"
                    },
                    regexp: {
                        regexp: /^[^?!@#$%\^&*()'';""\]\[<>\\`~{}|=+/]+$/,
                        message: "数据源驱动包含非法字符"
                    }
                }
            },
            url: {
                validators: {
                    notEmpty: {
                        message: "数据源驱动不能为空"
                    },
                    stringLength: {
                        max: 60,
                        message: "数据源连接地址长度不能超过60字符大小"
                    },
                    regexp: {
                        regexp: /^[^?!@#$%\^&*()'',;""\]\[<>\\`~{}|=+]+$/,
                        message: "数据源连接地址包含非法字符"
                    }
                }
            },
            username: {
                validators: {
                    stringLength: {
                        max: 20,
                        message: "用户名不能超过20字符大小"
                    },
                    regexp: {
                        regexp: /^[^?!@#$%\^&*()'',.;:""\]\[<>\\`~{}|=+/]+$/,
                        message: "用户名包含非法字符"
                    }
                }
            },
            password: {
                validators: {
                    stringLength: {
                        max: 20,
                        message: "口令不能超过20字符大小"
                    },
                    regexp: {
                        regexp: /^[^?!@#$%\^&*()'',.;:""\]\[<>\\`~{}|=+/]+$/,
                        message: "口令包含非法字符"
                    }
                }
            }
        }
    });
    $("#data-source-form").submit(function(event) {
        event.preventDefault();
    });
}
