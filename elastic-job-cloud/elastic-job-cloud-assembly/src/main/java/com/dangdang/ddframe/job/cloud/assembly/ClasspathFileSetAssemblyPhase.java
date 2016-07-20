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

package com.dangdang.ddframe.job.cloud.assembly;

import org.apache.maven.plugin.assembly.AssemblerConfigurationSource;
import org.apache.maven.plugin.assembly.archive.ArchiveCreationException;
import org.apache.maven.plugin.assembly.archive.phase.AssemblyArchiverPhase;
import org.apache.maven.plugin.assembly.archive.phase.FileSetAssemblyPhase;
import org.apache.maven.plugin.assembly.archive.phase.PhaseOrder;
import org.apache.maven.plugin.assembly.format.AssemblyFormattingException;
import org.apache.maven.plugin.assembly.model.Assembly;
import org.apache.maven.plugin.assembly.model.FileSet;
import org.codehaus.plexus.archiver.Archiver;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.logging.AbstractLogEnabled;

import java.util.ArrayList;
import java.util.List;

/**
 * 注册处理以{@code classpath:}前缀开头的FileSet.
 * 
 * @author zhangliang
 */
@Component(role = AssemblyArchiverPhase.class, hint = "classpath-file-sets")
public class ClasspathFileSetAssemblyPhase extends AbstractLogEnabled implements AssemblyArchiverPhase, PhaseOrder {
    
    private static final String CLASSPATH_PREFIX = "classpath:";
    
    @Override
    public void execute(final Assembly assembly, final Archiver archiver, final AssemblerConfigurationSource configSource) throws ArchiveCreationException, AssemblyFormattingException {
        List<FileSet> fileSets = assembly.getFileSets();
        if (null == fileSets || fileSets.isEmpty()) {
            return;
        }
        List<FileSet> classpathFileSets = new ArrayList<>(fileSets.size());
        for (FileSet each : fileSets) {
            if (isClasspathDirectory(each.getDirectory())) {
                String directory = each.getDirectory();
                each.setDirectory(directory.substring(CLASSPATH_PREFIX.length(), directory.length()).trim());
                classpathFileSets.add(each);
            }
        }
        if (classpathFileSets.isEmpty()) {
            return;
        }
        AddClasspathFileSetsTask task = new AddClasspathFileSetsTask(classpathFileSets);
        task.setLogger(getLogger());
        task.execute(archiver, configSource);
    }
    
    private boolean isClasspathDirectory(final String directory) {
        return null != directory && directory.startsWith(CLASSPATH_PREFIX);
    }
    
    @Override
    public int order() {
        return new FileSetAssemblyPhase().order() + 1;
    }
}
