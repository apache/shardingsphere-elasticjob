<#import "tags/dashboard.ftl" as dashboard>
<table id="regCenters" class="table table-hover">
    <thead>
        <tr>
            <th>注册中心名称</th>
            <th>连接地址</th>
            <th>命名空间</th>
            <th>登录凭证</th>
            <th>操作</th>
        </tr>
    </thead>
    <tbody>
    </tbody>
</table>
<button type="button" class="btn btn-success" data-toggle="modal" data-target="#add-reg-center">添加</button>
<div id="add-reg-center" class="modal fade" tabindex="-1" role="dialog">
    <div class="modal-dialog" role="document">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>
                <h4 class="modal-title" id="exampleModalLabel">添加注册中心</h4>
            </div>
            <form id="add-reg-center-form">
                <div class="modal-body">
                    <div class="form-group">
                        <label for="name" class="control-label">注册中心名称：</label>
                        <input type="text" class="form-control" id="name" name="name" required autofocus>
                    </div>
                    <div class="form-group">
                        <label for="zkAddressList" class="control-label">注册中心地址：</label>
                        <input type="text" class="form-control" id="zkAddressList" name="zkAddressList" placeholder="localhost:2181" required>
                    </div>
                    <div class="form-group">
                        <label for="namespace" class="control-label">命名空间：</label>
                        <input type="text" class="form-control" id="namespace" name="namespace">
                    </div>
                    <div class="form-group">
                        <label for="digest" class="control-label">登录凭证：</label>
                        <input type="text" class="form-control" id="digest" name="digest">
                    </div>
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-default" data-dismiss="modal">关闭</button>
                    <button id="add-reg-center-btn" type="submit" class="btn btn-primary">确认</button>
                </div>
            </form>
        </div>
    </div>
</div>
<@dashboard.successDialog "success-dialog" />
<@dashboard.failureDialog "connect-reg-center-failure-dialog" "连接失败，请检查注册中心配置" />
<@dashboard.confirmDialog "delete-confirm-dialog" "确认要删除吗？" />
<@dashboard.failureDialog "add-reg-center-failure-dialog" "注册中心名称重复" />
<script src="lib/jquery/jquery-2.1.4.min.js"></script>
<script src="lib/bootstrap/js/bootstrap.min.js"></script>
<script src="js/common.js"></script>
<script src="js/dashboard.js"></script>
<script src="js/registry_center.js"></script>
