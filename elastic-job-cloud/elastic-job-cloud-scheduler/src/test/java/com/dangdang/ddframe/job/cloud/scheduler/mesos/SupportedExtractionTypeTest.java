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

package com.dangdang.ddframe.job.cloud.scheduler.mesos;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public final class SupportedExtractionTypeTest {
    
    @Test
    public void assertIsExtraction() {
        assertTrue(SupportedExtractionType.isExtraction("http://localhost:8080/test.tar"));
        assertTrue(SupportedExtractionType.isExtraction("http://localhost:8080/test.tar.gz"));
        assertTrue(SupportedExtractionType.isExtraction("http://localhost:8080/test.tar.bz2"));
        assertTrue(SupportedExtractionType.isExtraction("http://localhost:8080/test.tar.xz"));
        assertTrue(SupportedExtractionType.isExtraction("http://localhost:8080/test.gz"));
        assertTrue(SupportedExtractionType.isExtraction("http://localhost:8080/test.tgz"));
        assertTrue(SupportedExtractionType.isExtraction("http://localhost:8080/test.tbz2"));
        assertTrue(SupportedExtractionType.isExtraction("http://localhost:8080/test.txz"));
        assertTrue(SupportedExtractionType.isExtraction("http://localhost:8080/test.zip"));
        assertFalse(SupportedExtractionType.isExtraction("http://localhost:8080/test.sh"));
        assertFalse(SupportedExtractionType.isExtraction("http://localhost:8080/test"));
    }
}
