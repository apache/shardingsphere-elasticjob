<div class="content-wrapper">
  <!-- Content Header (Page header) -->
   <section class="content-header">
       <h1>作业详情</h1>
  </section>
  <!-- Main content -->
   <div class="box-body"><!-- /.box-header -->
        <div role="tabpanel" class="tab-pane active" onsubmit="return false;">
            <form id="job-settings-form" class="form-horizontal">
                <div class="form-group">
                    <label for="jobClass" class="col-sm-2 control-label"><i>*</i>作业实现类</label>
                    <div class="col-sm-9">
                        <input type="text" placeholder="yourJobClass" id="jobClass" name="jobClass" class="form-control" data-toggle="tooltip" data-placement="bottom" title="	作业实现类，需实现ElasticJob接口，脚本型作业不需要配置" required/>
                    </div>
                </div>
                <div class="form-group">
                    <div class="col-sm-4">
                    <label for="jobName" class="col-sm-6 control-label"><i>*</i>作业名称</label>
                        <div class="col-sm-6">
                            <input type="text" placeholder="yourJob" disabled = "disabled" id="jobName" name="jobName" class="form-control" data-toggle="tooltip" data-placement="bottom" title="作业名称" required/>
                        </div>
                    </div>
                    <div class="col-sm-4">
                    <label for="jobType" class="col-sm-6 control-label" ><i>*</i>实现类型</label>
                        <div class="col-sm-6">
                            <select id="jobType" name="jobType" class="form-control" data-toggle="tooltip" data-placement="bottom" >
                                <option value="SIMPLE" required>SIMPLE </option>
                                <option value="DATAFLOW">DATAFLOW</option>
                                <option value="SCRIPT">SCRIPT</option>
                            </select>
                        </div>
                    </div>
                   <div class="col-sm-3">
                    <label for="cpuCount" class="col-sm-6 control-label"><i>*</i>cpu数量</label>
                        <div class="col-sm-6">
                            <input type="text" value=0.001 min=0.001 id="cpuCount" name="cpuCount" class="form-control" data-toggle="tooltip"  data-placement="bottom" title="单片作业所需要的CPU数量，最小值为0.001" required/>
                        </div>
                    </div>
                </div>
                <div class="form-group">
                   <div class="col-sm-4">
                    <label for="cron" class="col-sm-6 control-label"><i>*</i>cron表达式</label>
                        <div class="col-sm-6">
                            <input type="text" placeholder="0/5 * * * * ?"  id="cron" name="cron" class="form-control" data-toggle="tooltip" data-placement="bottom"  title="作业启动时间的cron表达式。如：0/5 * * * * ?" required />
                        </div>
                    </div>
                    <div class="col-sm-4">
                    <label for="jobExecutionType" class="col-sm-6 control-label"><i>*</i>作业执行类型</label>
                        <div class="col-sm-6">
                            <select id="jobExecutionType" name="jobExecutionType" class="form-control" data-toggle="tooltip" data-placement="bottom" >
                                <option value="DAEMON">DAEMON</option>
                                <option value="TRANSIENT">TRANSIENT</option>
                            </select> 
                        </div>
                    </div>
                    <div class="col-sm-3">
                    <label for="memoryMB" class="col-sm-6 control-label" ><i>*</i>单片作业内存</label>
                        <div class="col-sm-6">
                            <input type="number" value=1  min=1 id="memoryMB" name="memoryMB" class="form-control" data-toggle="tooltip" data-placement="bottom" title="单片作业所需要的内存MB，最小值为1" required />
                        </div>
                    </div>
                </div>
                <div class="form-group">
                    <div class="col-sm-4">
                    <label for="bootstrapScript" class="col-sm-6 control-label"><i>*</i>启动脚本</label>
                        <div class="col-sm-6">
                            <input type="text" placeholder="bin/start.sh" id="bootstrapScript" name="bootstrapScript" class="form-control" data-toggle="tooltip" data-placement="bottom" title="启动脚本，如：bin\start.sh。" required />
                        </div>
                    </div>
                    <div class="col-sm-4">
                    <label for="beanName" class="col-sm-6 control-label">beanName</label>
                        <div class="col-sm-6">
                            <input type="text" placeholder="yourBeanName" id="beanName" name="beanName" class="form-control" data-toggle="tooltip" data-placement="bottom" title="Spring容器中配置的bean名称" />
                        </div>
                    </div>
                    <div class="col-sm-3">
                    <label for="shardingTotalCount" class="col-sm-6 control-label" ><i>*</i>作业分片总数</label>
                        <div class="col-sm-6">
                            <input type="number" min=1 max=100  value=1 id="shardingTotalCount" name="shardingTotalCount" class="form-control" data-toggle="tooltip" data-placement="bottom" title="作业分片总数" required />
                        </div>
                    </div>
                </div>
                <div class="form-group">
                    <div class="col-sm-4">
                    <label for="jobParameter" class="col-sm-6 control-label">作业自定义参数</label>
                        <div class="col-sm-6">
                            <input type="text" id="jobParameter" name="jobParameter" class="form-control" data-toggle="tooltip" data-placement="bottom" title="作业自定义参数，可通过传递该参数为作业调度的业务方法传参，用于实现带参数的作业例：每次获取的数据量、作业实例从数据库读取的主键。" />
                        </div>
                    </div>
                </div>
                <div class="form-group">
                    <div class="col-sm-4">
                    <label for="failover" class="col-sm-6 control-label">支持自动失效转移</label>
                        <div class="col-sm-6">
                            <input type="checkbox" id="failover" name="failover" data-toggle="tooltip" data-placement="bottom" title="只有开启监控作业执行时状态的情况下才可以开启失效转移" />
                        </div>
                    </div>
                    <div class="col-sm-4">
                    <label for="misfire" class="col-sm-6 control-label">支持misfire</label>
                        <div class="col-sm-6">
                            <input type="checkbox" id="misfire" name="misfire" data-toggle="tooltip" data-placement="bottom" title="是否开启任务错过重新执行" />
                        </div>
                    </div>
                    <div class="col-sm-4">
                    <label for="streamingProcess" hidden="hidden" id="streamingProcess" class="col-sm-6 control-label">支持流式处理数据</label>
                        <div class="col-sm-6" id="streamingProcess_box" hidden="hidden">
                            <input type="checkbox"id="streamingProcess_box" name="streamingProcess" data-toggle="tooltip" data-placement="bottom" title="DATAFLOW类型作业，是否流式处理数据如果流式处理数据, 则fetchData不返回空结果将持续执行作业，如果非流式处理数据, 则处理数据完成后作业结束。" />
                        </div>
                    </div>
                </div>
                <div class="form-group">
                    <label for="appURL" class="col-sm-2 control-label"><i>*</i>应用所在路径</label>
                    <div class="col-sm-9">
                        <input type="text" placeholder="http://file_host:8080/foo-job.tar.gz"  id="appURL" name="appURL" class="form-control" data-toggle="tooltip" data-placement="bottom" title="	必须是可以通过网络访问到的路径。" required />
                    </div>
                </div>
                 <div class="form-group">
                    <label for="applicationContext" class="col-sm-2 control-label">Spring配置文件相对路径及名称</label>
                    <div class="col-sm-9">
                        <input type="text" placeholder="META-INF\applicationContext.xml" id="applicationContext" name="applicationContext" class="form-control" data-toggle="tooltip" data-placement="bottom" title="	Spring方式配置Spring配置文件相对路径以及名称，如：META-INF\applicationContext.xml" />
                    </div>
                </div>
                <div class="form-group">
                    <label for="shardingItemParameters" class="col-sm-2 control-label">分片序列号/参数对照表</label>
                    <div class="col-sm-9">
                        <textarea id="shardingItemParameters" placeholder="0=a,1=b,2=c" name="shardingItemParameters" class="form-control" data-toggle="tooltip" data-placement="bottom" title="分片序列号和参数用等号分隔，多个键值对用逗号分隔，类似map。分片序列号从0开始，不可大于或等于作业分片总数。如：0=a,1=b,2=c"></textarea>
                    </div>
                </div>
               <div class="form-group">
                    <label for="scriptCommandLine" class="col-sm-2 control-label">作业执行脚本</label>
                    <div class="col-sm-9">
                        <textarea id="scriptCommandLine" name="scriptCommandLine" class="form-control" data-toggle="tooltip" data-placement="bottom" title="分片序列号和参数用等号分隔，多个键值对用逗号分隔，类似map。分片序列号从0开始，不可大于或等于作业分片总数。如：0=a,1=b,2=c"></textarea>
                    </div>
                </div>
                <div class="form-group">
                    <label for="description" class="col-sm-2 control-label">作业描述信息</label>
                    <div class="col-sm-9">
                        <textarea id="description" name="description" class="form-control"></textarea>
                    </div>
                </div>
                <div class="form-group" style="text-align: center;" >
                   <button type="button" class="btn btn-primary" id = "save_form">保存</button>
                   <button type="reset" class="btn btn-inverse">重置</button>
                </div>
            </form>
        </div><!-- role -->
    </div><!-- /.box-body -->
</div><!--end content-wrapper -->
    
<!-- add_job -->
<link rel="stylesheet" type="text/css" href="css/add_job.css">
<link rel="stylesheet" type="text/css" href="css/placeholder.css">
<script src="plugins/BootstrapValidator/js/bootstrapValidator.js"></script>
<script src="js/job_edit_base.js"></script>
<script src="js/modify_job.js"></script>