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

package org.apache.shardingsphere.elasticjob.lite.example.fixture.entity;

import java.io.Serializable;

public final class Foo implements Serializable {
    
    private static final long serialVersionUID = 2706842871078949451L;
    
    private final long id;
    
    private final String location;
    
    private Status status;
    
    public Foo(final long id, final String location, final Status status) {
        this.id = id;
        this.location = location;
        this.status = status;
    }
    
    public long getId() {
        return id;
    }
    
    public String getLocation() {
        return location;
    }
    
    public Status getStatus() {
        return status;
    }
    
    public void setStatus(final Status status) {
        this.status = status;
    }
    
    public String toString() {
        return String.format("id: %s, location: %s, status: %s", id, location, status);
    }
    
    public enum Status {
        TODO,
        COMPLETED
    }
}
