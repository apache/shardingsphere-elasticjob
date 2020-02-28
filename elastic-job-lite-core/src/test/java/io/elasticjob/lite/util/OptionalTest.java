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

package io.elasticjob.lite.util;

import org.junit.Test;

import java.util.NoSuchElementException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public final class OptionalTest {
    
    @Test
    public void assertOfNullableForNull() {
        Optional<String> optional = Optional.ofNullable(null);
        assertFalse(optional.isPresent());
    }
    
    @Test
    public void assertOfNullableForNotNull() {
        Optional<String> optional = Optional.ofNullable("");
        assertTrue(optional.isPresent());
    }
    
    @Test
    public void assertIsPresent() {
        Optional<String> optional = Optional.of("");
        assertTrue(optional.isPresent());
    }
    
    @Test
    public void assertEmpty() {
        Optional<String> optional = Optional.empty();
        assertFalse(optional.isPresent());
    }
    
    @Test
    public void assertGetOf() {
        Optional<String> optional = Optional.of("get");
        assertEquals("get", optional.get());
    }
    
    @Test(expected = NoSuchElementException.class)
    public void assertGetEmpty() {
        Optional<String> optional = Optional.empty();
        assertNull(null, optional.get());
    }
    
    @Test(expected = NullPointerException.class)
    public void assertOfNull() {
        String value = "value";
        Optional<String> optional = Optional.of(null);
    }
}
