<#import "tags/dashboard.ftl" as dashboard>
<h2>服务器IP地址：<span id="server-ip">${serverIp}</span></h2>
<table id="jobs" class="table table-hover">
    <thead>
        <tr>
            <th>作业名</th>
            <th>状态</th>
            <th>最近处理成功数</th>
            <th>最近处理失败数</th>
            <th>分片项</th>
            <th>主节点？</th>
            <th>操作</th>
        </tr>
    </thead>
    <tbody>
    </tbody>
</table>
<button id="stop-all-jobs-btn" class="btn btn-warning">全部暂停</button>
<button id="resume-all-jobs-btn" class="btn btn-success">全部恢复</button>
<span id="chosen-job-name" class="hide"></span>
<@dashboard.successDialog "success-dialog" />
<@dashboard.failureDialog "connect-reg-center-failure-dialog" "连接失败，请检查注册中心配置" />
<@dashboard.confirmDialog "stop-leader-confirm-dialog" "暂停主节点将导致其他作业节点一起暂停，确认要暂停全部作业节点吗？" />
<script src="lib/jquery/jquery-2.1.4.min.js"></script>
<script src="lib/bootstrap/js/bootstrap.min.js"></script>
<script src="js/common.js"></script>
<script src="js/dashboard.js"></script>
<script src="js/server_detail.js"></script>
