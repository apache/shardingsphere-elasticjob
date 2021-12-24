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

import java.io.CharArrayWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * IO utils.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class IOUtils {
    
    /**
     * Convert InputStream to String.
     *
     * @param inputStream input stream
     * @param encoding encoding
     * @return result of the String type
     * @throws IOException IOException
     */
    public static String toString(final InputStream inputStream, final String encoding) throws IOException {
        return (null == encoding) ? toString(new InputStreamReader(inputStream, StandardCharsets.UTF_8)) : toString(new InputStreamReader(inputStream, encoding));
    }
    
    /**
     * Convert Reader to String.
     *
     * @param reader reader
     * @return result of the String type
     * @throws IOException IOException
     */
    public static String toString(final Reader reader) throws IOException {
        CharArrayWriter charArrayWriter = new CharArrayWriter();
        copy(reader, charArrayWriter);
        return charArrayWriter.toString();
    }
    
    private static void copy(final Reader input, final Writer output) throws IOException {
        char[] buffer = new char[4096];
        for (int length; (length = input.read(buffer)) >= 0;) {
            output.write(buffer, 0, length);
        }
    }
}
