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

package com.dangdang.ddframe.job.util.json;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import org.junit.Test;

import java.io.IOException;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public final class GsonFactoryTest {
    
    @Test
    public void assertGetGson() {
        assertThat(GsonFactory.getGson(), is(GsonFactory.getGson()));
    }
    
    @Test
    public void assertRegisterTypeAdapter() {
        Gson beforeRegisterGson = GsonFactory.getGson();
        GsonFactory.registerTypeAdapter(GsonFactoryTest.class, new TypeAdapter() {
            
            @Override
            public Object read(final JsonReader in) throws IOException {
                return null;
            }
            
            @Override
            public void write(final JsonWriter out, final Object value) throws IOException {
                out.jsonValue("test");
            }
        });
        assertThat(beforeRegisterGson.toJson(new GsonFactoryTest()), is("{}"));
        assertThat(GsonFactory.getGson().toJson(new GsonFactoryTest()), is("test"));
    }
}
