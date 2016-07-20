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

import com.dangdang.ddframe.job.lite.api.config.impl.ScriptJobConfiguration;
import org.junit.Test;
import org.mockito.internal.matchers.apachecommons.ReflectionEquals;

import static com.dangdang.ddframe.job.lite.spring.util.JobConfigurationDtoHelper.buildJobConfigurationBuilder;
import static com.dangdang.ddframe.job.lite.spring.util.JobConfigurationDtoHelper.buildJobConfigurationDto;
import static org.junit.Assert.assertThat;

public final class ScriptJobConfigurationDtoTest {
    
    @Test
    public void testBuildScriptJobConfigurationDtoWithCustomizedProperties() {
        ScriptJobConfigurationDto jobConfigurationDto = createScriptJobConfigurationDto();
        String scriptCommandLine = "update_test.sh";
        jobConfigurationDto.setScriptCommandLine(scriptCommandLine);
        ScriptJobConfiguration.ScriptJobConfigurationBuilder builder = (ScriptJobConfiguration.ScriptJobConfigurationBuilder) buildJobConfigurationBuilder(createScriptJobConfigurationBuilder());
        builder.scriptCommandLine(scriptCommandLine);
        assertThat(buildJobConfigurationDto(jobConfigurationDto), new ReflectionEquals(builder.build()));
    }
    
    @Test
    public void testBuildScriptJobConfigurationDtoWithDefaultProperties() {
        assertThat(createScriptJobConfigurationDto().toJobConfiguration(), new ReflectionEquals(createScriptJobConfigurationBuilder().build()));
    }
    
    private ScriptJobConfigurationDto createScriptJobConfigurationDto() {
        return new ScriptJobConfigurationDto("scriptJob", 10, "0/1 * * * * ?", "test.sh");
    }
    
    private ScriptJobConfiguration.ScriptJobConfigurationBuilder createScriptJobConfigurationBuilder() {
        return new ScriptJobConfiguration.ScriptJobConfigurationBuilder("scriptJob", 10, "0/1 * * * * ?", "test.sh");
    }
}
