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

package com.dangdang.ddframe.reg.fixture;

import lombok.Getter;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Getter
public class PlaceholderAnnotationBean {
    
    @Value("${/test/deep/nested}")
    private String regValue1;
    
    @Value("${/new}")
    private String regValue2;
    
    @Value("${withNamespace_1}")
    private String withNamespaceValue1;
    
    @Value("${withNamespace_2}")
    private String withNamespaceValue2;
    
    @Value("${withoutNamespace_1}")
    private String withoutNamespaceValue1;
    
    @Value("${withoutNamespace_2}")
    private String withoutNamespaceValue2;
}
