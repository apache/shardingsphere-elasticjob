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

package com.dangdang.ddframe.job.fixture;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermission;
import java.util.HashSet;
import java.util.Set;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ScriptElasticJobHelper {
    
    public static String buildScriptCommandLine() {
        try {
            if (System.getProperties().getProperty("os.name").contains("Windows")) {
                return Paths.get(ScriptElasticJobHelper.class.getResource("/script/test.bat").getPath().substring(1)).toString();
            } else {
                Path result = Paths.get(ScriptElasticJobHelper.class.getResource("/script/test.sh").getPath());
                changeFilePermissions(result);
                return result.toString();
            } 
        } catch (final IOException ex) {
            throw new RuntimeException(ex);
        }
    }
    
    private static void changeFilePermissions(final Path path) throws IOException {
        Set<PosixFilePermission> permissionsSet = new HashSet<>();
        permissionsSet.add(PosixFilePermission.OWNER_READ);
        permissionsSet.add(PosixFilePermission.OWNER_EXECUTE);
        Files.setPosixFilePermissions(path, permissionsSet);
    }
}
