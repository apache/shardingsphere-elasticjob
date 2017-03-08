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

package com.dangdang.ddframe.job.lite.console.util;

import java.io.File;

public final class HomeFolder {
    
    private static final String USER_HOME = System.getProperty("user.home");
    
    private static final String FILE_SEPARATOR = System.getProperty("file.separator");
    
    private static final String CONSOLE_ROOT_FOLDER = ".elastic-job-console";
    
    private HomeFolder() {
    }
    
    public static String getFilePathInHomeFolder(final String fileName) {
        return String.format("%s%s", getHomeFolder(), fileName);
    }
    
    public static void createHomeFolderIfNotExisted() {
        File file = new File(getHomeFolder());
        if (!file.exists()) {
            file.mkdirs();
        }
    }
    
    private static String getHomeFolder() {
        return String.format("%s%s%s%s", USER_HOME, FILE_SEPARATOR, CONSOLE_ROOT_FOLDER, FILE_SEPARATOR);
    }
}
