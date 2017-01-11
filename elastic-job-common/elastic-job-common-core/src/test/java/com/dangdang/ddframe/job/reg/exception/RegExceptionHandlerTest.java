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

package com.dangdang.ddframe.job.reg.exception;

import org.apache.zookeeper.KeeperException.ConnectionLossException;
import org.apache.zookeeper.KeeperException.NoNodeException;
import org.apache.zookeeper.KeeperException.NodeExistsException;
import org.junit.Ignore;
import org.junit.Test;

public final class RegExceptionHandlerTest {
    
    @Test
    @Ignore
    // TODO throw InterruptedException will cause zookeeper TestingServer break. Ignore first, fix it later.
    public void assertHandleExceptionWithInterruptedException() {
        RegExceptionHandler.handleException(new InterruptedException());
    }
    
    @Test(expected = RegException.class)
    public void assertHandleExceptionWithOtherException() {
        RegExceptionHandler.handleException(new RuntimeException());
    }
    
    @Test
    public void assertHandleExceptionWithConnectionLossException() {
        RegExceptionHandler.handleException(new ConnectionLossException());
    }
    
    @Test
    public void assertHandleExceptionWithNoNodeException() {
        RegExceptionHandler.handleException(new NoNodeException());
    }
    
    @Test
    public void assertHandleExceptionWithNoNodeExistsException() {
        RegExceptionHandler.handleException(new NodeExistsException());
    }
    
    @Test
    public void assertHandleExceptionWithCausedConnectionLossException() {
        RegExceptionHandler.handleException(new RuntimeException(new ConnectionLossException()));
    }
    
    @Test
    public void assertHandleExceptionWithCausedNoNodeException() {
        RegExceptionHandler.handleException(new RuntimeException(new NoNodeException()));
    }
    
    @Test
    public void assertHandleExceptionWithCausedNoNodeExistsException() {
        RegExceptionHandler.handleException(new RuntimeException(new NodeExistsException()));
    }
}
