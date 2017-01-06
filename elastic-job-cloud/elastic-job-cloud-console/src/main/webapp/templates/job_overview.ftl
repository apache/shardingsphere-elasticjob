<div class="content-wrapper">
    <!-- Content Header (Page header) -->
    <section class="content-header">
        <h1>
                            作业定义
        </h1>
    </section>
    <section class="content">
        <div class="row">
            <div class="box box-info">
                <div id="jobExecDetailToolbar">
                    <div class="form-inline" role="form">
                        <div class="form-group">
                            <td>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a href="add_job"  type="button" class="label label-warning" style="width:80px;font-size:14px; padding: 6px;">增加</a></td>
                        </div>
                    </div>
                </div>
                    <table id="JobExecDetailTable" 
                        data-show-refresh="true"
                        data-show-toggle="true"
                        data-striped="true"
                        data-toggle="table"
                        data-url="/job/jobs"
                        data-flat="true"
                        data-click-to-select="true"
                        data-search="true"
                        data-strict-search="false"
                        data-query-params="queryParams"
                        data-query-params-type="notLimit"
                        data-pagination="true"
                        data-page-list="[10, 20, 50, 100]"
                        data-show-columns="true"
                        data-toolbar="#jobExecDetailToolbar">
                        <thead>
                            <tr>
                                <th data-field="jobName"  data-sortable="true">作业名称</th>
                                <th data-field="jobClass" data-sortable="true">作业实现类</th>
                                <th data-field="shardingTotalCount" data-sortable="true">作业分片总数</th>
                                <th data-field="cron" data-sortable="true">cron表达式</th>
                                <th data-field="appURL" data-formatter="viewOper">操作</th>
                            </tr>
                        </thead>
                    </table>
            </div><!-- /.box -->
            <div class="modal" id="detail-data" tabindex="-1" role="dialog" aria-labelledby="detailModalLabel" aria-hidden="true">
                <div class="modal-dialog">
                    <div class="modal-content">
                        <div class="modal-header">
                            <h4 class="modal-title">作业详情</h4>
                        </div>
                    <div class="modal-body">
                        <div class="col-sm-12">
                            <div class="row">
                                <div class="col-sm-3">
                                    <label for="jobClass" class="control-label">作业实现类</label>
                                </div>
                                <input type="text" id="jobClass" name="jobClass" class="col-sm-9" disabled="true"> 
                            </div><!--end row -->
                            <div class="row">
                                <div class="col-sm-3">
                                    <label for="jobName" class="control-label">作业名称</label>
                                </div>
                                    <input type="text" id="jobName" name="jobName" class="col-sm-3" disabled="true">
                                <div class="col-sm-3">
                                    <label for="cron" class="control-label">cron表达式</label>
                                </div>
                                    <input type="text" id="cron" name="cron" class="col-sm-3" disabled="true">
                            </div><!--end row -->
                            <div class="row">
                                <div class="col-sm-3">
                                    <label for="jobType" class="control-label">实现类型</label>
                                </div>
                                    <input type="text" id="jobType" name="jobType" class="col-sm-3" disabled="true">
                                <div class="col-sm-3">
                                    <label for="cpuCount" class="control-label">cpu数量</label>
                                </div>
                                    <input type="text" id="cpuCount" name="cpuCount" class="col-sm-3" disabled="true">
                            </div><!--end row -->
                            <div class="row">
                                <div class="col-sm-3">
                                    <label for="jobExecutionType" class="control-label">作业执行类型</label>
                                </div>
                                    <input type="text" id="jobExecutionType" name="jobExecutionType" class="col-sm-3" disabled="true">
                                <div class="col-sm-3">
                                    <label for="memoryMB" class="control-label">单片作业内存</label>
                                </div>
                                    <input type="text" id="memoryMB" name="memoryMB" class="col-sm-3" disabled="true">
                            </div><!--end row -->
                            <div class="row">
                                <div class="col-sm-3">
                                    <label for="bootstrapScript" class="control-label">启动脚本</label>
                                </div>
                                    <input type="text" id="bootstrapScript" name="bootstrapScript" class="col-sm-3" disabled="true">
                                <div class="col-sm-3">
                                    <label for="beanName" class="control-label">beanName</label>
                                </div>
                                    <input type="text" id="beanName" name="beanName" class="col-sm-3" disabled="true">
                                </div><!--end row -->
                            <div class="row">
                                <div class="col-sm-3">
                                    <label for="shardingTotalCount" class="control-label">作业分片总数</label>
                                </div>
                                    <input type="text" id="shardingTotalCount" name="shardingTotalCount" class="col-sm-3" disabled="true">
                                <div class="col-sm-3">
                                <label for="jobParameter" class="control-label">作业自定义参数</label>
                                </div>
                                    <input type="text" id="jobParameter" name="jobParameter" class="col-sm-3" disabled="true">
                            </div><!--end row -->
                            <div class="row">
                                <div class="col-sm-3">
                                    <label for="failover" class="control-label">自动失效转移</label>
                                </div>
                                    <input type="text" id="failover" name="failover" class="col-sm-3" disabled="true">
                                <div class="col-sm-3">
                                    <label for="misfire" class="control-label">支持misfire</label>
                                </div>
                                    <input type="text" id="misfire" name="misfire" class="col-sm-3" disabled="true">
                            </div><!--end row -->
                            <div class="row">
                                <div class="col-sm-3">
                                <label for="streamingProcess" class="control-label">流式处理数据</label>
                                </div>
                                    <input type="text" id="streamingProcess" name="streamingProcess" class="col-sm-3" disabled="true">
                                </div><!--end row -->
                            <div class="row">
                                <div class="col-sm-3">
                                    <label for="appURL" class="control-label">应用所在路径</label>
                                </div>
                                    <input type="text" id="appURL" name="appURL" class="col-sm-9" disabled="true">
                            </div><!--end row -->
                            <div class="row">
                                <div class="col-sm-3">
                                    <label for="applicationContext" class="control-label">Spring配置文件相对路径及名称</label>
                                </div>
                                    <input type="text" id="applicationContext" name="applicationContext" class="col-sm-9" disabled="true">
                            </div><!--end row -->
                            <div class="row">
                                <div class="col-sm-3">
                                    <label for="shardingItemParameters" class="control-label">分片序列号/参数对照表</label>
                                </div>
                                    <input type="text" id="shardingItemParameters" name="shardingItemParameters" class="col-sm-9" disabled="true">
                            </div><!--end row -->
                            <div class="row">
                                <div class="col-sm-3">
                                    <label for="scriptCommandLine" class="control-label">作业执行脚本</label>
                                </div>
                                <input type="text" id="scriptCommandLine" name="scriptCommandLine" class="col-sm-9" disabled="true">
                            </div><!--end row -->
                            <div class="row">
                                <div class="col-sm-3">
                                    <label for="description" class="control-label">作业描述信息</label>
                                </div>
                                    <input type="text" id="description" name="description" class="col-sm-9" disabled="true">
                            </div><!--end row -->
                        </div>
                    </div>
                    <div class="modal-footer">
                        <button type="button" class="btn btn-default" data-dismiss="modal" onClick="closeModal()">关闭</button>
                    </div>
                </div>
            </div>
        </div>
    </div>
    </section><!-- /.content -->
</div><!-- /.box -->
    
<style>
    input{border:none;background-color:white;}
</style>
    
<script src="js/job_overview.js"></script>