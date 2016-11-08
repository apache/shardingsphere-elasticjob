<#import "tags/dashboard.ftl" as dashboard>
<h2>作业总览</h2>
<table id="jobs-overview-tbl" class="table table-hover">
    <thead>
        <tr>
            <th>作业名</th>
            <th>运行状态</th>
            <th>cron表达式</th>
            <th>描述</th>
        </tr>
    </thead>
    <tbody>
    </tbody>
</table>
<h2>服务器总览</h2>
<table id="servers-overview-tbl" class="table table-hover">
    <thead>
        <tr>
            <th>服务器IP</th>
            <th>服务器名</th>
            <th>状态</th>
        </tr>
    </thead>
    <tbody>
    </tbody>
</table>
<@dashboard.successDialog "success-dialog" />
<@dashboard.failureDialog "connect-reg-center-failure-dialog" "连接失败，请检查注册中心配置" />
<script src="lib/jquery/jquery-2.1.4.min.js"></script>
<script src="lib/bootstrap/js/bootstrap.min.js"></script>
<script src="js/common.js"></script>
<script src="js/dashboard.js"></script>
<script src="js/overview.js"></script>
