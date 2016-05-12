/*
 * Copyright 1999-2015 dangdang.com.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package com.dangdang.ddframe.job.api;

import com.dangdang.ddframe.job.internal.network.LocalIPFilter;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

/**
 * 作业配置信息.
 * 
 * @author zhangliang
 */
@Getter
@Setter
@RequiredArgsConstructor
public class JobConfiguration {
    
    /**
     * 作业名称.
     */
    private final String jobName;
    
    /**
     * 作业实现类名称.
     */
    private final Class<? extends ElasticJob> jobClass;
    
    /**
     * 作业分片总数.
     */
    private final int shardingTotalCount;
    
    /**
     * 作业启动时间的cron表达式.
     */
    private final String cron;
    
    /**
     * 分片序列号和个性化参数对照表.
     * 
     * <p>
     * 分片序列号和参数用等号分隔, 多个键值对用逗号分隔. 类似map.
     * 分片序列号从0开始, 不可大于或等于作业分片总数.
     * 如:
     * 0=a,1=b,2=c
     * </p>
     */
    private String shardingItemParameters = "";
    
    /**
     * 作业自定义参数.
     * 
     * <p>
     * 可以配置多个相同的作业, 但是用不同的参数作为不同的调度实例.
     * </p>
     */
    private String jobParameter = "";
    
    /**
     * 监控作业执行时状态.
     * 
     * <p>
     * 每次作业执行时间和间隔时间均非常短的情况, 建议不监控作业运行时状态以提升效率, 因为是瞬时状态, 所以无必要监控. 请用户自行增加数据堆积监控. 并且不能保证数据重复选取, 应在作业中实现幂等性. 也无法实现作业失效转移.
     * 每次作业执行时间和间隔时间均较长短的情况, 建议监控作业运行时状态, 可保证数据不会重复选取.
     * </p>
     */
    private boolean monitorExecution = true;
    
    /**
     * 统计作业处理数据数量的间隔时间.
     * 
     * <p>
     * 单位: 秒.
     * 只对处理数据流类型作业起作用.
     * </p>
     */
    private int processCountIntervalSeconds = 300;
    
    /**
     * 处理数据的并发线程数.
     * 
     * <p>
     * 只对高吞吐量处理数据流类型作业起作用.
     * </p>
     */
    private int concurrentDataProcessThreadCount = 1;
    
    /**
     * 每次抓取的数据量.
     * 
     * <p>
     * 可在不重启作业的情况下灵活配置抓取数据量.
     * </p>
     */
    private int fetchDataCount = 1;
    
    /**
     * 最大容忍的本机与注册中心的时间误差秒数.
     * 
     * <p>
     * 如果时间误差超过配置秒数则作业启动时将抛异常.
     * 配置为-1表示不检查时间误差.
     * </p>
     */
    private int maxTimeDiffSeconds = -1;
    
    /**
     * 是否开启失效转移.
     * 
     * <p>
     * 只有对monitorExecution的情况下才可以开启失效转移.
     * </p> 
     */
    private boolean failover;
    
    /**
     * 是否开启misfire.
     */
    private boolean misfire = true;
    
    /**
     * 作业辅助监控端口.
     */
    private int monitorPort = -1;
    
    /**
     * 作业分片策略实现类全路径.
     * 
     * <p>
     * 默认使用{@code com.dangdang.ddframe.job.plugin.sharding.strategy.AverageAllocationJobShardingStrategy}.
     * </p>
     */
    private String jobShardingStrategyClass = "";
    
    /**
     * 作业描述信息.
     */
    private String description = "";
    
    /**
     * 作业是否禁止启动.
     * 可用于部署作业时, 先禁止启动, 部署结束后统一启动.
     */
    private boolean disabled;
    
    /**
     * 本地配置是否可覆盖注册中心配置.
     * 如果可覆盖, 每次启动作业都以本地配置为准.
     */
    private boolean overwrite;

    /**
     *  任务执行服务器IP白名单
     *  <p>
     *      使用','分隔符分隔多个IP地址，IP地址可以后加'/子网掩码有效位数'来指定IP网段。
     *  </p>
     */
    private String allow = null;

    /**
     *  任务执行服务器IP黑名单，黑名单优先级高于白名单
     *  <p>
     *      使用','分隔符分隔多个IP地址，IP地址可以后加'/子网掩码有效位数'来指定IP网段。
     *  </p>
     */
    private String deny = null;

    /**
     *  初始化
     */
    public void init() {
        initDisabled();
    }

    /**
     *  根据黑白名单设置是否disabled
     */
    private void initDisabled() {
        boolean disabled = isDisabled();
        if (disabled) {
            //配置中设定的disabled优先级最高
            return;
        }
        LocalIPFilter ipFilter = new LocalIPFilter(getAllow(), getDeny());
        boolean allowed = ipFilter.isAllowed();
        boolean denied = ipFilter.isDenied();

        if (denied) {
            //如果在黑名单，设为disabled=true，优先级高于白名单
            setDisabled(true);
            return;
        }
        if (!allowed) {
            //不在白名单，设置disable=true
            setDisabled(true);
        }
        //没有设置disable=true && （不在黑名单 || 黑名单为空） && （在白名单 || 白名单为空）， 则disable=false
    }
}
