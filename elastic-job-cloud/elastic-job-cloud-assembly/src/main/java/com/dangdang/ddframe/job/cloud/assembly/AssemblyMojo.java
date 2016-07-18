/*
 * Copyright 1999-2015 dangdang.com.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package com.dangdang.ddframe.job.cloud.assembly;

import org.apache.maven.plugin.assembly.mojos.SingleAssemblyMojo;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.ResolutionScope;

/**
 * 云作业打包插件.
 * 
 * @author caohao
 */
@Mojo(name = AssemblyMojo.MOJO_NAME, requiresDependencyResolution = ResolutionScope.COMPILE, threadSafe = true, defaultPhase = LifecyclePhase.PACKAGE)
public class AssemblyMojo extends SingleAssemblyMojo {
    
    static final String MOJO_NAME = "assembly";
    
    private static final String DESCRIPTOR_REFERENCE = "elastic-job-cloud-assembly";
    
    private String finalName = "target";
    
    @Override
    public String[] getDescriptorReferences() {
        return new String[] {DESCRIPTOR_REFERENCE};
    }
    
    @Override
    public boolean isAssemblyIdAppended() {
        return false;
    }
    
    @Override
    public String getFinalName() {
        return finalName;
    }
    
    @Override
    public void setFinalName(final String finalName) {
        this.finalName = finalName;
    }
}
