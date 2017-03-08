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

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public final class HomeFolderTest {
    
    private static final String HOME_FOLDER = System.getProperty("user.home") + System.getProperty("file.separator") + ".elastic-job-console" + System.getProperty("file.separator");
    
    @Test
    public void assertGetFilePathInHomeFolder() {
        assertThat(HomeFolder.getFilePathInHomeFolder("test_file"), is(HOME_FOLDER + "test_file"));
    }
}
