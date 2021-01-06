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

package org.apache.shardingsphere.elasticjob.cloud.scheduler.mesos.fixture;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.mesos.Protos;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class OfferBuilder {

    /**
     * Create offer.
     * @param offerId offer id
     * @return Offer
     */
    public static Protos.Offer createOffer(final String offerId) {
        return Protos.Offer.newBuilder()
                .setId(Protos.OfferID.newBuilder().setValue(offerId))
                .setFrameworkId(Protos.FrameworkID.newBuilder().setValue("elasticjob-cloud-test").build())
                .setSlaveId(Protos.SlaveID.newBuilder().setValue("slave-" + offerId).build())
                .setHostname("localhost")
                .addResources(Protos.Resource.newBuilder().setName("cpus").setType(Protos.Value.Type.SCALAR).setScalar(Protos.Value.Scalar.newBuilder().setValue(100d).build()).build())
                .addResources(Protos.Resource.newBuilder().setName("mem").setType(Protos.Value.Type.SCALAR).setScalar(Protos.Value.Scalar.newBuilder().setValue(128000d).build()).build())
                .build();
    }
}
