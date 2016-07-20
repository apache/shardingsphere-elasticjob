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
import org.apache.maven.plugin.assembly.archive.task.AddDirectoryTask;
import org.apache.maven.plugin.assembly.format.AssemblyFormattingException;
import org.apache.maven.plugin.assembly.format.ReaderFormatter;
import org.apache.maven.plugin.assembly.model.FileSet;
import org.apache.maven.plugin.assembly.utils.AssemblyFormatUtils;
import org.apache.maven.plugin.assembly.utils.TypeConversionUtils;
import org.apache.maven.project.MavenProject;
import org.apache.maven.shared.utils.Os;
import org.codehaus.plexus.archiver.Archiver;
import org.codehaus.plexus.components.io.functions.InputStreamTransformer;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.logging.console.ConsoleLogger;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * 处理以{@code classpath:}前缀开头的FileSet.
 * 
 * <p>
 * 类中其他内容是拷贝{@code org.apache.maven.plugin.assembly.archive.task.AddFileSetsTask}. 
 * 仅仅重写了{@code getFileSetDirectory}方法, 用于读取classpath路径的文件.
 * maven-assembly-plugin仅支持将打包的{@code assembly.xml}文件放入公用的maven项目中, 但是{@code assembly.xml}所用到的依赖, 如脚本, 配置文件等, 并不能从公用的maven项目中读取.
 * 这个类的作用是将打入公用maven包的依赖文件, 写入target路径, 供maven-assembly-plugin读取.
 * 用于完成将{@code assembly.xml}及其依赖都放入公用的maven项目中.
 * </p>
 * 
 * @author zhangliang
 */
public class AddClasspathFileSetsTask {
    
    private final List<FileSet> fileSets;
    
    private Logger logger;
    
    private MavenProject project;
    
    private MavenProject moduleProject;
    
    public AddClasspathFileSetsTask(final List<FileSet> fileSets) {
        this.fileSets = fileSets;
    }
    
    /**
     * 执行打包任务.
     * 
     * @param archiver 归档对象
     * @param configSource 配置
     * @throws ArchiveCreationException 归档对象不能创建抛出的异常
     * @throws AssemblyFormattingException 打包描述文件格式
     */
    public void execute(final Archiver archiver, final AssemblerConfigurationSource configSource) throws ArchiveCreationException, AssemblyFormattingException {
        File archiveBaseDir = configSource.getArchiveBaseDirectory();
        if (null != archiveBaseDir) {
            if (!archiveBaseDir.exists()) {
                throw new ArchiveCreationException(String.format("The archive base directory '%s' does not exist", archiveBaseDir.getAbsolutePath()));
            } else if (!archiveBaseDir.isDirectory()) {
                throw new ArchiveCreationException(String.format("The archive base directory '%s' exists, but it is not a directory", archiveBaseDir.getAbsolutePath()));
            }
        }
        for (FileSet fileSet : fileSets) {
            addFileSet(fileSet, archiver, configSource, archiveBaseDir);
        }
    }
    
    private void addFileSet(final FileSet fileSet, final Archiver archiver, final AssemblerConfigurationSource configSource, final File archiveBaseDir) 
            throws AssemblyFormattingException, ArchiveCreationException {
        checkLogger();
        if (null == project) {
            project = configSource.getProject();
        }
        String destinationDirectory = fileSet.getOutputDirectory();
        if (null == destinationDirectory) {
            destinationDirectory = fileSet.getDirectory();
        }
        warnForPlatformSpecifics(destinationDirectory);
        destinationDirectory = AssemblyFormatUtils.getOutputDirectory(destinationDirectory, 
                configSource.getFinalName(), configSource, AssemblyFormatUtils.moduleProjectInterpolator(moduleProject), AssemblyFormatUtils.artifactProjectInterpolator(project));
        logEnvironment(fileSet, archiver, destinationDirectory);
        if (logger.isDebugEnabled()) {
            logger.debug(String.format("The archive base directory is '%s'", archiveBaseDir));
        }
        File fileSetDir = getFileSetDirectory(fileSet, configSource);
        if (fileSetDir.exists()) {
            InputStreamTransformer fileSetTransformers = ReaderFormatter.getFileSetTransformers(configSource, fileSet.isFiltered(), fileSet.getLineEnding());
            if (null == fileSetTransformers && logger.isDebugEnabled()) {
                logger.debug("NOT reformatting any files in " + fileSetDir);
            }
            if (fileSetDir.getPath().equals(File.separator)) {
                throw new AssemblyFormattingException(
                        String.format("Your assembly descriptor specifies a directory of %s, which is your *entire* file system.\nThese are not the files you are looking for", File.separator));
            }
            AddDirectoryTask task = new AddDirectoryTask(fileSetDir, fileSetTransformers);
            int dirMode = TypeConversionUtils.modeToInt(fileSet.getDirectoryMode(), logger);
            if (-1 != dirMode) {
                task.setDirectoryMode(dirMode);
            }
            int fileMode = TypeConversionUtils.modeToInt(fileSet.getFileMode(), logger);
            if (-1 != fileMode) {
                task.setFileMode(fileMode);
            }
            task.setUseDefaultExcludes(fileSet.isUseDefaultExcludes());
            List<String> excludes = fileSet.getExcludes();
            excludes.add("**/*.filtered");
            excludes.add("**/*.formatted");
            task.setExcludes(excludes);
            task.setIncludes(fileSet.getIncludes());
            task.setOutputDirectory(destinationDirectory);
            task.execute(archiver);
        }
    }
    
    private void logEnvironment(final FileSet fileSet, final Archiver archiver, final String destinationDirectory) {
        if (logger.isDebugEnabled()) {
            String filePerms = Integer.toString(archiver.getOverrideFileMode(), 8) + (null == fileSet.getLineEnding() ? "" : " lineEndings: " + fileSet.getLineEnding());
            logger.debug(String.format("ClasspathFileSet[%s] dir perms: %s file perms: ", destinationDirectory, Integer.toString(archiver.getOverrideDirectoryMode(), 8), filePerms));
        }
    }
    
    private void warnForPlatformSpecifics(final String destinationDirectory) {
        if (Os.isFamily(Os.FAMILY_WINDOWS)) {
            if (isLinuxRootReference(destinationDirectory) && logger.isErrorEnabled()) {
                logger.error(String.format("OS=Windows and the assembly descriptor contains a *nix-specific root-relative-reference(starting with slash) %s", destinationDirectory));
            } else if (isWindowsPath(destinationDirectory) && logger.isWarnEnabled()) {
                logger.warn(String.format(
                        "The assembly descriptor contains a *nix-specific root-relative-reference (starting with slash). This is non-portable and will fail on windows %s", destinationDirectory));
            }
        } else {
            if (isWindowsPath(destinationDirectory) && logger.isErrorEnabled()) {
                logger.error(String.format("OS=Non-Windows and the assembly descriptor contains a windows-specific directory reference (with a drive letter) %s", destinationDirectory));
            } else if (isLinuxRootReference(destinationDirectory) && logger.isWarnEnabled()) {
                logger.warn(String.format("The assembly descriptor contains a filesystem-root relative reference, which is not cross platform compatible %s", destinationDirectory));
            }
        }
    }
    
    private static boolean isLinuxRootReference(final String destinationDirectory) {
        return null != destinationDirectory && destinationDirectory.startsWith("/");
    }
    
    private static boolean isWindowsPath(final String destinationDirectory) {
        return null != destinationDirectory && destinationDirectory.length() >= 2 && destinationDirectory.charAt(1) == ':';
    }
    
    private File getFileSetDirectory(final FileSet fileSet, final AssemblerConfigurationSource configSource) {
        String sourceDirectory = fileSet.getDirectory();
        Path assemblyPath = makeAssemblyFolder(configSource, sourceDirectory);
        String jarPath = getJarPath(sourceDirectory);
        if (null == jarPath) {
            return assemblyPath.toFile();
        }
        Enumeration<JarEntry> jarEntries;
        try {
            jarEntries = new JarFile(jarPath).entries();
        } catch (final IOException ex) {
            throw new AssemblyException(String.format("cannot fount jar file '%s'", jarPath), ex);
        }
        while (jarEntries.hasMoreElements()) {
            JarEntry jarEntry = jarEntries.nextElement();
            String jarEntryName = jarEntry.getName();
            if (jarEntryName.startsWith(sourceDirectory) && !jarEntry.isDirectory()) {
                InputStream resourceAsStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(jarEntryName);
                Path target = Paths.get(configSource.getOutputDirectory().getAbsolutePath(), jarEntryName);
                try {
                    Files.copy(resourceAsStream, target, StandardCopyOption.REPLACE_EXISTING);
                } catch (final IOException ex) {
                    logger.error("copy file error", ex);
                    throw new AssemblyException(ex);
                }
            }
        }
        return assemblyPath.toFile();
    }
    
    private Path makeAssemblyFolder(final AssemblerConfigurationSource configSource, final String sourceDirectory) {
        Path result = Paths.get(configSource.getOutputDirectory().getAbsolutePath(), sourceDirectory);
        if (result.toFile().exists()) {
            result.toFile().delete();
        }
        result.toFile().getAbsoluteFile().mkdirs();
        return result;
    }
    
    private String getJarPath(final String sourceDirectory) {
        URL url = Thread.currentThread().getContextClassLoader().getResource(sourceDirectory);
        if (null == url) {
            return null;
        }
        String result = url.getPath();
        if (!result.contains("!")) {
            throw new AssemblyException(String.format("The Directory '%s' cannot fount in jar file.", sourceDirectory));
        }
        if (!result.startsWith("file:/")) {
            throw new AssemblyException(String.format("The Directory '%s' cannot fount in jar file.", sourceDirectory));
        }
        return result.substring(0, result.indexOf('!')).substring("file:".length());
    }
    
    private void checkLogger() {
        if (null == logger) {
            logger = new ConsoleLogger(Logger.LEVEL_INFO, "AddClasspathFileSetsTask-internal");
        }
    }
    
    public void setLogger(final Logger logger) {
        this.logger = logger;
    }
    
    public void setProject(final MavenProject project) {
        this.project = project;
    }
    
    public void setModuleProject(final MavenProject moduleProject) {
        this.moduleProject = moduleProject;
    }
}
