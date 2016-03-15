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

package com.dangdang.ddframe.job.console.util;

import org.apache.curator.framework.CuratorFramework;

public final class SessionCuratorClient {
    
    private static ThreadLocal<CuratorFramework> curatorClient = new ThreadLocal<>();
    
    private SessionCuratorClient() {
    }
    
    public static CuratorFramework getCuratorClient() {
        return curatorClient.get();
    }
    
    public static void setCuratorClient(final CuratorFramework client) {
        curatorClient.set(client);
    }
    
    public static void clear() {
        curatorClient.remove();
    }
}
