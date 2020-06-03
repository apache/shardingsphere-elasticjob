/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.elasticjob.lite.lifecycle.api;

/**
 * 操作分片的API.
 */
public interface ShardingOperateAPI {
    
    /**
     * 禁用作业分片.
     * 
     * @param jobName 作业名称
     * @param item 分片项
     */
    void disable(String jobName, String item);
    
    /**
     * 启用作业分片.
     *
     * @param jobName 作业名称
     * @param item 分片项
     */
    void enable(String jobName, String item);
}
