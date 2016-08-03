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

package com.dangdang.example.elasticjob.fixture.entity;

import java.io.Serializable;

public final class Foo implements Serializable {
    
    private static final long serialVersionUID = 2706842871078949451L;
    
    private long id;
    
    private FooStatus status;
    
    public Foo(final long id, final FooStatus status) {
        this.id = id;
        this.status = status;
    }
    
    public long getId() {
        return id;
    }
    
    public void setId(final long id) {
        this.id = id;
    }
    
    public FooStatus getStatus() {
        return status;
    }
    
    public void setStatus(final FooStatus status) {
        this.status = status;
    }
    
    public String toString() {
        return String.format("id:%s, status:%s", id, status);
    }
}
