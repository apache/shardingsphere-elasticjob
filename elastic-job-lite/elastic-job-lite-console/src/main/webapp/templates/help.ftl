<#import "tags/dashboard.ftl" as dashboard>
<h2>设计理念</h2>
<ol>
    <li>本控制台和Elastic Job并无直接关系，是通过读取Elastic Job的注册中心数据展现作业状态，或更新注册中心数据修改全局配置。</li>
    <li>控制台只能控制作业本身是否运行，但不能控制作业进程的启停，因为控制台和作业本身服务器是完全分布式的，控制台并不能控制作业服务器。</li>
</ol>
<h2>主要功能</h2>
<ol>
    <li>作业状态查看</li>
    <li>作业服务器状态查看</li>
    <li>快捷的修改作业设置</li>
    <li>控制作业暂停和恢复运行</li>
    <li>跨注册中心查看作业</li>
</ol>
<h2>不支持项</h2>
<ol>
    <li>添加作业。因为作业都是在首次运行时自动添加，使用控制台添加作业并无必要。</li>
    <li>删除作业。即使删除了Zookeeper信息也不能真正停止作业的运行，还会导致运行中的作业出问题。</li>
</ol>
<h2>操作问题</h2>
<ol>
    <li>Q：如何添加作业？<br />A：直接在作业服务器启动包含Elastic Job的作业进程即可。</li>
    <li>Q：如何删除作业？<br />A：关闭所有要删除的Elastic Job的运行进程，之后登录Zookeeper手工删除作业名称节点。Zookeeper的目录结构和参见使用文档。</li>
</ol>
<@dashboard.successDialog "success-dialog" />
<@dashboard.failureDialog "connect-reg-center-failure-dialog" "连接失败，请检查注册中心配置" />
<script src="lib/jquery/jquery-2.1.4.min.js"></script>
<script src="lib/bootstrap/js/bootstrap.min.js"></script>
<script src="js/common.js"></script>
<script src="js/dashboard.js"></script>
