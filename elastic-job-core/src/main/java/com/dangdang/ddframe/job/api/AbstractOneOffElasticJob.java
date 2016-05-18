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

import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;

/**
 * 简单的分布式作业.
 * 
 * <p>
 * 仅保证作业可被分布式定时调用, 不提供任何作业处理逻辑.
 * </p>
 * 
 * <p>
 * <strong>包结构调整, 作业类型全部迁移至plugin包. 未来版本将删除, 请从旧版本升级的程序升级.</strong>
 * </p>
 * @see com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob
 * 
 * @author zhangliang
 * @author caohao
 * 
 * @deprecated .
 */
@Deprecated
public abstract class AbstractOneOffElasticJob extends AbstractSimpleElasticJob { }
