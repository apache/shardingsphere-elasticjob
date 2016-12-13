/*
 * Copyright 1999-2015 dangdang.com.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package com.dangdang.ddframe.job.reg.base;

/**
 * 选举候选人.
 * 保证{@link #startLeadership()}与{@link #stopLeadership()}方法在同一个线程内交替运行,
 * 且不会出现并发执行的情况.
 * 
 * @author gaohongtao
 */
public interface ElectionCandidate {
    
    /**
     * 开始领导状态.
     * @throws Exception 抛出异常后,将调用{@link #stopLeadership()}进行清理
     */
    void startLeadership() throws Exception;
    
    /**
     * 终止领导状态.
     * 实现该方法时不应该抛出任何异常
     */
    void stopLeadership();
}
