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

package com.dangdang.ddframe.job.cloud.scheduler.mesos.fixture;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.mesos.Protos;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class OfferBuilder {
    
    public static Protos.Offer createOffer(final String offerId) {
        return Protos.Offer.newBuilder()
                .setId(Protos.OfferID.newBuilder().setValue(offerId))
                .setFrameworkId(Protos.FrameworkID.newBuilder().setValue("elastic-job-cloud-test").build())
                .setSlaveId(Protos.SlaveID.newBuilder().setValue("slave-" + offerId).build())
                .setHostname("localhost")
                .addResources(Protos.Resource.newBuilder().setName("cpus").setType(Protos.Value.Type.SCALAR).setScalar(Protos.Value.Scalar.newBuilder().setValue(100d).build()).build())
                .addResources(Protos.Resource.newBuilder().setName("mem").setType(Protos.Value.Type.SCALAR).setScalar(Protos.Value.Scalar.newBuilder().setValue(128000d).build()).build())
                .build();
    }
}
