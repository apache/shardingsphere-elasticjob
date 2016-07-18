/*
 * Copyright 1999-2015 dangdang.com.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package com.dangdang.ddframe.reg.spring.placeholder;

import com.dangdang.ddframe.test.AbstractZookeeperJUnit4SpringContextTests;
import org.junit.Before;
import org.junit.Test;
import org.springframework.test.context.ContextConfiguration;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

@ContextConfiguration(locations = "classpath:META-INF/reg/placeholder/placeholderBean.xml")
public final class PlaceholderResolvedForNoPlaceholderConfigurerTest extends AbstractZookeeperJUnit4SpringContextTests {
    
    private PlaceholderResolved placeholderResolved;
    
    @Before
    public void setUp() {
        placeholderResolved = new PlaceholderResolved(applicationContext);
    }
    
    @Test
    public void assertNoPlaceholderConfigurer() {
        assertThat(placeholderResolved.getResolvePlaceholderText("any"), is("any"));
    }
}
