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

package io.elasticjob.lite.fixture.util;

import io.elasticjob.lite.api.dataflow.DataflowJob;
import io.elasticjob.lite.config.JobCoreConfiguration;
import io.elasticjob.lite.config.LiteJobConfiguration;
import io.elasticjob.lite.config.dataflow.DataflowJobConfiguration;
import io.elasticjob.lite.config.simple.SimpleJobConfiguration;
import io.elasticjob.lite.fixture.TestSimpleJob;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.unitils.util.ReflectionUtils;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class JobConfigurationUtil {
    
    public static void setFieldValue(final Object config, final String fieldName, final Object fieldValue) {
        try {
            ReflectionUtils.setFieldValue(config, config.getClass().getDeclaredField(fieldName), fieldValue);
        } catch (final NoSuchFieldException ex) {
            throw new RuntimeException(ex);
        }
    }
    
    public static LiteJobConfiguration createSimpleLiteJobConfiguration() {
        return LiteJobConfiguration.newBuilder(new SimpleJobConfiguration(JobCoreConfiguration.newBuilder("test_job", "0/1 * * * * ?", 3).build(), TestSimpleJob.class.getCanonicalName())).build();
    }
    
    public static LiteJobConfiguration createSimpleLiteJobConfiguration(final boolean overwrite) {
        return LiteJobConfiguration.newBuilder(new SimpleJobConfiguration(JobCoreConfiguration.newBuilder("test_job", "0/1 * * * * ?", 3).build(), 
                TestSimpleJob.class.getCanonicalName())).overwrite(overwrite).build();
    }
    
    public static LiteJobConfiguration createDataflowLiteJobConfiguration() {
        return LiteJobConfiguration.newBuilder(
                new DataflowJobConfiguration(JobCoreConfiguration.newBuilder("test_job", "0/1 * * * * ?", 3).build(), DataflowJob.class.getCanonicalName(), false)).build();
    }
}
