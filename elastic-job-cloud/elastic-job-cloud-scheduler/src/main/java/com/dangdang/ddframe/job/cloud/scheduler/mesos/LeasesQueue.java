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

package com.dangdang.ddframe.job.cloud.scheduler.mesos;

import com.netflix.fenzo.VirtualMachineLease;
import com.netflix.fenzo.plugins.VMLeaseObject;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.mesos.Protos;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * 资源预占队列.
 *
 * @author zhangliang
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class LeasesQueue {
    
    private static final LeasesQueue INSTANCE = new LeasesQueue();
    
    private final BlockingQueue<VirtualMachineLease> queue = new LinkedBlockingQueue<>();
    
    /**
     * 获取实例.
     * 
     * @return 单例对象
     */
    public static LeasesQueue getInstance() {
        return INSTANCE;
    }
    
    /**
     * 添加资源至队列预占.
     *
     * @param offer 资源
     */
    public void offer(final Protos.Offer offer) {
        queue.offer(new VMLeaseObject(offer));
    }
    
    /**
     * 出栈队列资源.
     * 
     * @return 队列资源集合
     */
    public List<VirtualMachineLease> drainTo() {
        List<VirtualMachineLease> result = new ArrayList<>(queue.size());
        queue.drainTo(result);
        return result;
    }
}
