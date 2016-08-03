/*
 *
 *  * Copyright 1999-2015 dangdang.com.
 *  * <p>
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  * 
 *  *      http://www.apache.org/licenses/LICENSE-2.0
 *  * 
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *  * </p>
 *  
 */

package com.dangdang.example.elasticjob.utils;

import com.dangdang.example.elasticjob.core.main.JobMain;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermissions;

public class ScriptCommandLineHelper {
    
    public static String buildScriptCommandLine() {
        if (System.getProperties().getProperty("os.name").contains("Windows")) {
            return Paths.get(JobMain.class.getResource("/script/demo.bat").getPath().substring(1)).toString();
        } else {
            Path result = Paths.get(JobMain.class.getResource("/script/demo.sh").getPath());
            changeFilePermissions(result);
            return result.toString();
        }
    }
    
    private static void changeFilePermissions(final Path path) {
        try {
            Files.setPosixFilePermissions(path, PosixFilePermissions.fromString("rwxr-xr-x"));
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }
}
