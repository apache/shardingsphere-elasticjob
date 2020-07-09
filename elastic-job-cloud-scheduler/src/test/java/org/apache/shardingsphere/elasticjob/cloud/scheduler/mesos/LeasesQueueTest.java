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

import org.apache.shardingsphere.elasticjob.cloud.scheduler.mesos.fixture.OfferBuilder;
import org.hamcrest.core.Is;
import org.junit.Assert;
import org.junit.Test;

public final class LeasesQueueTest {
    
    private LeasesQueue leasesQueue = LeasesQueue.getInstance();
    
    @Test
    public void assertOperate() {
        Assert.assertTrue(leasesQueue.drainTo().isEmpty());
        leasesQueue.offer(OfferBuilder.createOffer("offer_1"));
        leasesQueue.offer(OfferBuilder.createOffer("offer_2"));
        Assert.assertThat(leasesQueue.drainTo().size(), Is.is(2));
    }
}
