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

package com.dangdang.ddframe.job.lite.spring.namespace.parser.script;

import com.dangdang.ddframe.job.api.type.script.api.ScriptJobConfiguration;
import com.dangdang.ddframe.job.lite.api.config.LiteJobConfiguration;
import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public final class ScriptJobConfigurationDtoTest {
    
    @Test
    public void assertToLiteJobConfiguration() {
        LiteJobConfiguration actual = new ScriptJobConfigurationDto("scriptJob", "0/1 * * * * ?", 10, "test.sh").toLiteJobConfiguration();
        assertThat(actual.getJobName(), is("scriptJob"));
        assertThat(((ScriptJobConfiguration) actual.getJobConfig()).getScriptCommandLine(), is("test.sh"));
    }
}
