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

package org.apache.shardingsphere.elasticjob.cloud.scheduler.util;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public final class IOUtilsTest {
    
    @Test
    public void assertToStringFromInputStream() throws Exception {
        byte[] b = "toStringFromInputStream".getBytes(StandardCharsets.UTF_8);
        InputStream in = new ByteArrayInputStream(b);
        assertThat(IOUtils.toString(in, null), is("toStringFromInputStream"));
    }
    
    @Test
    public void assertToStringFromInputStreamWithEncoding() throws Exception {
        byte[] b = "toStringFromInputStream".getBytes(StandardCharsets.UTF_8);
        InputStream in = new ByteArrayInputStream(b);
        assertThat(IOUtils.toString(in, "UTF-8"), is("toStringFromInputStream"));
    }
    
    @Test
    public void assertToStringFromReader() throws Exception {
        byte[] b = "toStringFromReader".getBytes(StandardCharsets.UTF_8);
        InputStream is = new ByteArrayInputStream(b);
        Reader inr = new InputStreamReader(is, StandardCharsets.UTF_8);
        assertThat(IOUtils.toString(inr), is("toStringFromReader"));
    }
}
