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

package com.dangdang.ddframe.job.cloud.scheduler.restful;

import org.junit.Test;
import org.unitils.util.ReflectionUtils;

import static com.dangdang.ddframe.job.cloud.scheduler.restful.RestfulTestsUtil.sentRequest;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class CloudOperationRestfulApiTest extends AbstractCloudRestfulApiTest {
    
    @Test
    public void assertExplicitReconcile() throws Exception {
        ReflectionUtils.setFieldValue(new CloudOperationRestfulApi(), "lastReconcileTime", 0);
        assertThat(sentRequest("http://127.0.0.1:19000/operate/reconcile/explicit", "POST", ""), is(204));
        assertThat(sentRequest("http://127.0.0.1:19000/operate/reconcile/explicit", "POST", ""), is(500));
        ReflectionUtils.setFieldValue(new CloudOperationRestfulApi(), "lastReconcileTime", 0);
        assertThat(sentRequest("http://127.0.0.1:19000/operate/reconcile/explicit?taskId=unknown", "POST", ""), is(500));
    }
    
    @Test
    public void assertImplicitReconcile() throws Exception {
        ReflectionUtils.setFieldValue(new CloudOperationRestfulApi(), "lastReconcileTime", 0);
        assertThat(sentRequest("http://127.0.0.1:19000/operate/reconcile/implicit", "POST", ""), is(204));
        assertThat(sentRequest("http://127.0.0.1:19000/operate/reconcile/implicit", "POST", ""), is(500));
    }
}
