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

package org.apache.shardingsphere.elasticjob.infra.yaml;

import org.apache.shardingsphere.elasticjob.infra.yaml.fixture.FooYamlConfiguration;
import org.junit.Test;
import java.io.InputStream;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

public final class YamlEngineTest {
    
    private static final String YAML = "bar: bar\n"
            + "foo: foo\n"
            + "nest:\n"
            + "  bar: nest_bar\n"
            + "  foo: nest_foo\n";
    
    private static final String YAML_WITH_NULL = "foo: foo\n";
    
    private static final String PREFIX = "nest";
    
    private static final String PREFIX2 = "nest.bar";
    
    @Test
    public void assertMarshal() {
        FooYamlConfiguration actual = new FooYamlConfiguration();
        actual.setFoo("foo");
        actual.setBar("bar");
        FooYamlConfiguration nest = new FooYamlConfiguration();
        nest.setFoo("nest_foo");
        nest.setBar("nest_bar");
        actual.setNest(nest);
        assertThat(YamlEngine.marshal(actual), is(YAML));
    }
    
    @Test
    public void assertMarshalWithNullValue() {
        FooYamlConfiguration actual = new FooYamlConfiguration();
        actual.setFoo("foo");
        assertThat(YamlEngine.marshal(actual), is(YAML_WITH_NULL));
    }
    
    @Test
    public void assertUnmarshal() {
        FooYamlConfiguration actual = YamlEngine.unmarshal(YAML, FooYamlConfiguration.class);
        assertThat(actual.getFoo(), is("foo"));
        assertThat(actual.getBar(), is("bar"));
        assertThat(actual.getNest().getFoo(), is("nest_foo"));
        assertThat(actual.getNest().getBar(), is("nest_bar"));
    }
    
    @Test
    public void assertUnmarshalWithNullValue() {
        FooYamlConfiguration actual = YamlEngine.unmarshal(YAML_WITH_NULL, FooYamlConfiguration.class);
        assertThat(actual.getFoo(), is("foo"));
        assertNull(actual.getBar());
        assertNull(actual.getNest());
    }
    
    @Test
    public void assertUnmarshalWithPrefix() {
        InputStream configFileInput = Thread.currentThread().getContextClassLoader().getResourceAsStream("yaml-test.yaml");         
        FooYamlConfiguration actual = YamlEngine.unmarshal(PREFIX, configFileInput, FooYamlConfiguration.class);
        assertThat(actual.getFoo(), is("nest_foo"));
        assertThat(actual.getBar(), is("nest_bar"));
        assertThat(actual.getNest().getFoo(), is("nest_foo2"));
        assertThat(actual.getNest().getBar(), is("nest_bar2"));
    }
    
    @Test
    public void assertUnmarshalWithPrefixAndNullValue() {
        InputStream configFileInput = Thread.currentThread().getContextClassLoader().getResourceAsStream("yaml-test.yaml");
        FooYamlConfiguration actual = YamlEngine.unmarshal(PREFIX2, configFileInput, FooYamlConfiguration.class);
        assertNull(actual);
    }
}
