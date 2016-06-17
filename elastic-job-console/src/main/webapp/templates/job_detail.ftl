<#import "tags/dashboard.ftl" as dashboard>
<div>
    <h1>作业名称：<span id="job-name">${jobName}</span></h1>
    <ul class="nav nav-tabs" role="tablist">
        <li id="settings_tab" role="presentation" class="active"><a href="#settings" aria-controls="settings" role="tab" data-toggle="tab">作业设置</a></li>
        <li id="servers_tab" role="presentation"><a href="#servers" aria-controls="servers" role="tab" data-toggle="tab">作业服务器</a></li>
        <li id="execution_info_tab" role="presentation"><a href="#execution_info" aria-controls="execution_info" role="tab" data-toggle="tab">作业运行状态</a></li>
    </ul>
    <div class="tab-content">
        <div role="tabpanel" class="tab-pane active" id="settings">
            <form id="job-settings-form" class="form-horizontal">
                <div class="form-group">
                    <label for="jobClass" class="col-sm-2 control-label">作业实现类</label>
                    <div class="col-sm-9">
                        <input type="text" id="jobClass" name="jobClass" class="form-control" disabled />
                    </div>
                </div>
                <div class="form-group">
                    <label for="jobType" class="col-sm-2 control-label">作业类型</label>
                    <div class="col-sm-9">
                        <input type="text" id="jobType" name="jobType" class="form-control" disabled />
                    </div>
                </div>
                <div class="form-group">
                    <label for="shardingTotalCount" class="col-sm-2 control-label">作业分片总数</label>
                    <div class="col-sm-1">
                        <input type="number" id="shardingTotalCount" name="shardingTotalCount" class="form-control" data-toggle="tooltip" data-placement="bottom" title="修改此值将触发重分片" required />
                    </div>
                    
                    <label for="jobParameter" class="col-sm-2 control-label">自定义参数</label>
                    <div class="col-sm-2">
                        <input type="text" id="jobParameter" name="jobParameter" class="form-control" data-toggle="tooltip" data-placement="bottom" title="可以配置多个相同的作业，但是用不同的参数作为不同的调度实例" />
                    </div>
                    
                    <label for="cron" class="col-sm-2 control-label">cron表达式</label>
                    <div class="col-sm-2">
                        <input type="text" id="cron" name="cron" class="form-control" data-toggle="tooltip" data-placement="bottom" title="作业启动时间的cron表达式" required />
                    </div>
                </div>
                <#if jobType == "DATA_FLOW">
                    <div class="form-group">
                        <label for="concurrentDataProcessThreadCount" class="col-sm-2 control-label">处理数据的并发线程数</label>
                        <div class="col-sm-1">
                            <input type="number" id="concurrentDataProcessThreadCount" name="concurrentDataProcessThreadCount" class="form-control" data-toggle="tooltip" data-placement="bottom" title="只对高吞吐量处理数据流类型作业起作用" />
                        </div>
                        
                        <label for="processCountIntervalSeconds" class="col-sm-2 control-label">统计处理数据量的间隔秒数</label>
                        <div class="col-sm-2">
                            <input type="number" id="processCountIntervalSeconds" name="processCountIntervalSeconds" class="form-control" data-toggle="tooltip" data-placement="bottom" title="只对处理数据流类型作业起作用" />
                        </div>
                        
                        <label for="fetchDataCount" class="col-sm-2 control-label">每次抓取的数据量</label>
                        <div class="col-sm-2">
                            <input type="number" id="fetchDataCount" name="fetchDataCount" class="form-control" data-toggle="tooltip" data-placement="bottom" title="可在不重启作业的情况下灵活配置抓取数据量" />
                        </div>
                    </div>
                    <div class="form-group">
                        <label for="fetchDataCount" class="col-sm-2 control-label">是否流式处理数据</label>
                        <div class="col-sm-2">
                            <input type="checkbox" id="streamingProcess" name="streamingProcess" data-toggle="tooltip" data-placement="bottom" title="如果流式处理数据, 则fetchData不返回空结果将持续执行作业; 如果非流式处理数据, 则处理数据完成后作业结束" />
                        </div>
                    </div>
                </#if>
                <div class="form-group">
                    <label for="maxTimeDiffSeconds" class="col-sm-2 control-label">最大容忍的本机与注册中心的时间误差秒数</label>
                    <div class="col-sm-1">
                        <input type="number" id="maxTimeDiffSeconds" name="maxTimeDiffSeconds" class="form-control" data-toggle="tooltip" data-placement="bottom" title="如果时间误差超过配置秒数则作业启动时将抛异常。配置为-1表示不检查时间误差。" />
                    </div>
                    
                    <label for="monitorPort" class="col-sm-2 control-label">监听作业端口</label>
                    <div class="col-sm-1">
                        <input type="number" id="monitorPort" name="monitorPort" class="form-control" data-toggle="tooltip" data-placement="bottom" title="抓取作业注册信息监听服务端口。配置为-1表示不启用监听服务。" />
                    </div>
                </div>
                
                <div class="form-group">
                    <label for="monitorExecution" class="col-sm-2 control-label">监控作业执行时状态</label>
                    <div class="col-sm-2">
                        <input type="checkbox" id="monitorExecution" name="monitorExecution" data-toggle="tooltip" data-placement="bottom" title="每次作业执行时间和间隔时间均非常短的情况，建议不监控作业运行时状态以提升效率，因为是瞬时状态，所以无必要监控。请用户自行增加数据堆积监控。并且不能保证数据重复选取，应在作业中实现幂等性。也无法实现作业失效转移。每次作业执行时间和间隔时间均较长短的情况，建议监控作业运行时状态，可保证数据不会重复选取。" />
                    </div>
                    
                    <label for="failover" class="col-sm-2 control-label">支持自动失效转移</label>
                    <div class="col-sm-2">
                        <input type="checkbox" id="failover" name="failover" data-toggle="tooltip" data-placement="bottom" title="只有开启监控作业执行时状态的情况下才可以开启失效转移" />
                    </div>
                    
                    <label for="failover" class="col-sm-2 control-label">支持misfire</label>
                    <div class="col-sm-2">
                        <input type="checkbox" id="misfire" name="misfire" data-toggle="tooltip" data-placement="bottom" title="是否开启任务错过重新执行" />
                    </div>
                </div>
                
                <div class="form-group">
                    <label for="shardingItemParameters" class="col-sm-2 control-label">分片序列号/参数对照表</label>
                    <div class="col-sm-9">
                        <textarea id="shardingItemParameters" name="shardingItemParameters" class="form-control" data-toggle="tooltip" data-placement="bottom" title="分片序列号和参数用等号分隔，多个键值对用逗号分隔，类似map。分片序列号从0开始，不可大于或等于作业分片总数。如：0=a,1=b,2=c"></textarea>
                    </div>
                </div>
                
                <div class="form-group">
                    <label for="jobShardingStrategyClass" class="col-sm-2 control-label">作业分片策略实现类全路径</label>
                    <div class="col-sm-9">
                        <input type="text" id="jobShardingStrategyClass" name="jobShardingStrategyClass" class="form-control" data-toggle="tooltip" data-placement="bottom" title="默认使用按照IP地址顺序分片策略，可参照文档定制化分片策略" />
                    </div>
                </div>
                
                <div class="form-group">
                    <label for="description" class="col-sm-2 control-label">作业描述信息</label>
                    <div class="col-sm-9">
                        <textarea id="description" name="description" class="form-control"></textarea>
                    </div>
                </div>
                <#if jobType == "SCRIPT">
                <div class="form-group">
                    <label for="scriptCommandLine" class="col-sm-2 control-label">脚本作业全路径</label>
                    <div class="col-sm-9">
                        <input type="text" id="scriptCommandLine" name="scriptCommandLine" class="form-control" data-toggle="tooltip" data-placement="bottom" title="执行脚本的全路径名称，可以包含参数" />
                    </div>
                </div>
                </#if>
                <button type="reset" class="btn btn-inverse">重置</button>
                <button type="submit" class="btn btn-primary">更新</button>
            </form>
        </div>
        <div role="tabpanel" class="tab-pane" id="servers">
            <table id="servers" class="table table-hover">
                <thead>
                    <tr>
                        <th>IP地址</th>
                        <th>机器名</th>
                        <th>状态</th>
                        <th>最近处理成功数</th>
                        <th>最近处理失败数</th>
                        <th>分片项</th>
                        <th>操作</th>
                    </tr>
                </thead>
                <tbody>
                </tbody>
            </table>
            <button id="trigger-all-jobs-btn" class="btn btn-success">全部触发</button>
            <button id="pause-all-jobs-btn" class="btn btn-warning">全部暂停</button>
            <button id="resume-all-jobs-btn" class="btn btn-success">全部恢复</button>
        </div>
        <div role="tabpanel" class="tab-pane" id="execution_info">
            <table id="execution" class="table table-hover">
                <thead>
                    <tr>
                        <th>分片项</th>
                        <th>状态</th>
                        <th>失效转移执行</th>
                        <th>上次作业开始时间</th>
                        <th>上次作业完成时间</th>
                        <th>下次作业运行时间</th>
                    </tr>
                </thead>
                <tbody>
                </tbody>
            </table>
        </div>
    </div>
</div>
<@dashboard.successDialog "success-dialog" />
<@dashboard.failureDialog "connect-reg-center-failure-dialog" "连接失败，请检查注册中心配置" />
<script src="lib/jquery/jquery-2.1.4.min.js"></script>
<script src="lib/bootstrap/js/bootstrap.min.js"></script>
<script src="js/common.js"></script>
<script src="js/dashboard.js"></script>
<script src="js/job_detail.js"></script>
