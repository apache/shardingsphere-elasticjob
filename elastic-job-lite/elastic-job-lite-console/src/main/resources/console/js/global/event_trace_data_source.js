$(function() {
    authorityControl();
    renderDataSources();
    validate();
    dealDataSourceModal();
    handleFieldValidator();
    submitDataSource();
    bindButtons();
    bindConnectionTest();
});

function renderDataSources() {
    $("#data-sources").bootstrapTable({
        url: "api/data-source",
        cache: false,
        search: true,
        showRefresh: true,
        showColumns: true,
        columns: 
        [{
            field: "name",
            title: "数据源名称",
            sortable: true
        }, {
            field: "driver",
            title: "数据库驱动",
            sortable: true
        }, {
            field: "url",
            title: "数据库连接地址",
            sortable: true
        }, {
            field: "username",
            title: "数据库用户名"
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
        operationTd = "<button disabled operation='connect-datasource' class='btn-xs' dataSourceName='" + name + "'>已连</button>&nbsp;<button operation='delete-datasource' class='btn-xs btn-danger' data-toggle='modal' id='delete-dialog' dataSourceName='" + name + "'>删除</button>";
    } else {
        operationTd = "<button operation='connect-datasource' class='btn-xs btn-info' dataSourceName='" + name + "' data-loading-text='切换中...'>连接</button>&nbsp;<button operation='delete-datasource' class='btn-xs btn-danger' data-toggle='modal' id='delete-dialog' dataSourceName='" + name + "'>删除</button>";
    }
    return operationTd;
}

function bindButtons() {
    bindConnectButtons();
    bindDeleteButtons();
}

function bindConnectButtons() {
    $(document).off("click", "button[operation='connect-datasource']");
    $(document).on("click", "button[operation='connect-datasource']", function(event) {
        var btn = $(this).button("loading");
        var dataSourceName = $(event.currentTarget).attr("dataSourceName");
        $.ajax({
            url: "api/data-source/connect",
            type: "POST",
            data: JSON.stringify({"name" : dataSourceName}),
            contentType: "application/json",
            dataType: "json",
            success: function(data) {
                if (data) {
                    $("#data-sources").bootstrapTable("refresh");
                    renderDataSourceForDashboardNav();
                    showSuccessDialog();
                } else {
                    showFailureDialog("操作未成功，原因：连接失败，请检查事件追踪数据源配置");
                }
                btn.button("reset");
            }
        });
    });
}

function bindDeleteButtons() {
    $(document).off("click", "button[operation='delete-datasource']");
    $(document).on("click", "button[operation='delete-datasource']", function(event) {
        showDeleteConfirmModal();
        var dataSourceName = $(event.currentTarget).attr("dataSourceName");
        $(document).off("click", "#confirm-btn");
        $(document).on("click", "#confirm-btn", function() {
            $.ajax({
                url: "api/data-source",
                type: "DELETE",
                data: JSON.stringify({"name" : dataSourceName}),
                contentType: "application/json",
                dataType: "json",
                success: function() {
                    $("#data-sources").bootstrapTable("refresh");
                    $("#confirm-dialog").modal("hide");
                    $(".modal-backdrop").remove();
                    $("body").removeClass("modal-open");
                    renderDataSourceForDashboardNav();
                    refreshEventTraceNavTag();
                }
            });
        });
    });
}

function dealDataSourceModal() {
    $("#add-data-source").click(function() {
        $("#add-data-source-center").modal({backdrop: 'static', keyboard: true});
    });
    $("#close-add-data-source-form").click(function() {
        $("#add-data-source-center").on("hide.bs.modal", function () {
            $("#data-source-form")[0].reset();
        });
        $("#data-source-form").data("bootstrapValidator").resetForm();
    });
}

function handleFieldValidator() {
    $("#password").focus(function() {
        $("#data-source-form").data("bootstrapValidator").enableFieldValidators("password", true);
    });
    $("#password").blur(function() {
        $("#data-source-form").data("bootstrapValidator").enableFieldValidators("password", "" === $("#password").val() ? false : true);
    });
}

function submitDataSource() {
    $("#add-data-source-btn").on("click", function(event) {
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
                url: "api/data-source",
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
                        renderDataSourceForDashboardNav();
                        refreshEventTraceNavTag();
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
                        message: "数据源名称不能为空"
                    },
                    stringLength: {
                        max: 50,
                        message: "数据源名称长度不能超过50字符大小"
                    },
                    regexp: {
                        regexp: /^[\w\.-]+$/,
                        message: "数据源名称只能使用数字、字母、下划线(_)、短横线(-)和点号(.)"
                    },
                    callback: {
                        message: "数据源已经存在",
                        callback: function() {
                            var dataSourceName = $("#name").val();
                            var result = true;
                            $.ajax({
                                url: "api/data-source",
                                contentType: "application/json",
                                async: false,
                                success: function(data) {
                                    for (var index = 0; index < data.length; index++) {
                                        if (dataSourceName === data[index].name) {
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
            url: {
                validators: {
                    notEmpty: {
                        message: "数据库URL不能为空"
                    },
                    stringLength: {
                        max: 200,
                        message: "数据库URL长度不能超过200字符大小"
                    }
                }
            },
            username: {
                validators: {
                    notEmpty: {
                        message: "数据库用户名不能为空"
                    },
                    stringLength: {
                        max: 50,
                        message: "数据库用户名不能超过50字符大小"
                    },
                    regexp: {
                        regexp: /^[\w\.-]+$/,
                        message: "数据库用户名只能使用数字、字母、下划线(_)、短横线(-)和点号(.)"
                    }
                }
            },
            password: {
                validators: {
                    stringLength: {
                        max: 50,
                        message: "数据库口令不能超过50字符大小"
                    },
                    regexp: {
                        regexp: /^[\w\.-]+$/,
                        message: "数据库口令只能使用数字、字母、下划线(_)、短横线(-)和点号(.)"
                    }
                }
            }
        }
    });
    $("#data-source-form").submit(function(event) {
        event.preventDefault();
    });
}

function bindConnectionTest() {
    $("#connect-test").on("click", function() {
        var name = $("#name").val();
        var driver = $("#driver").val();
        var url = $("#url").val();
        var username = $("#username").val();
        var password = $("#password").val();
        $.ajax({
            url: "api/data-source/connectTest",
            type: "POST",
            data: JSON.stringify({"name": name, "driver": driver, "url": url, "username": username, "password": password}),
            contentType: "application/json",
            dataType: "json",
            success: function(data) {
                if (data) {
                    showInfoDialog("事件追踪数据源测试连接成功!");
                } else {
                    showFailureDialog("事件追踪数据源测试连接失败!");
                }
            }
        });
    });
}
