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

package org.apache.shardingsphere.elasticjob.cloud.scheduler.mesos;

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
 * Lease queue.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class LeasesQueue {
    
    private static final LeasesQueue INSTANCE = new LeasesQueue();
    
    private final BlockingQueue<VirtualMachineLease> queue = new LinkedBlockingQueue<>();
    
    /**
     * Get instance.
     * 
     * @return singleton instance
     */
    public static LeasesQueue getInstance() {
        return INSTANCE;
    }
    
    /**
     * Offer resource to lease queue.
     *
     * @param offer resource
     */
    public void offer(final Protos.Offer offer) {
        queue.offer(new VMLeaseObject(offer));
    }
    
    /**
     * Dump all the resources from lease queue.
     * 
     * @return collection of resources
     */
    public List<VirtualMachineLease> drainTo() {
        List<VirtualMachineLease> result = new ArrayList<>(queue.size());
        queue.drainTo(result);
        return result;
    }
}
