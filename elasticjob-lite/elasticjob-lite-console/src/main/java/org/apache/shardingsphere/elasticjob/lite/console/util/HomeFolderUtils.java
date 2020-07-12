/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.shardingsphere.elasticjob.lite.console.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.io.File;

/**
 * Home folder Utils.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class HomeFolderUtils {
    
    private static final String USER_HOME = System.getProperty("user.home");
    
    private static final String CONSOLE_ROOT_FOLDER = ".elasticjob-console";
    
    /**
     * Get file path in home folder.
     * 
     * @param fileName file name
     * @return file path in home folder
     */
    public static String getFilePathInHomeFolder(final String fileName) {
        return String.format("%s%s", getHomeFolder(), fileName);
    }
    
    /**
     * Create home folder if not existed.
     */
    public static void createHomeFolderIfNotExisted() {
        File file = new File(getHomeFolder());
        if (!file.exists()) {
            file.mkdirs();
        }
    }
    
    private static String getHomeFolder() {
        return String.format("%s%s%s%s", USER_HOME, File.separator, CONSOLE_ROOT_FOLDER, File.separator);
    }
}
