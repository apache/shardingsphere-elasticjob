/**
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

package com.dangdang.ddframe.job.internal.env;

import java.net.InetAddress;
import java.net.UnknownHostException;

import com.dangdang.ddframe.job.exception.JobException;

/**
 * 获取真实本机网络的实现类类.
 * 
 * @author zhangliang
 */
public final class RealLocalHostService implements LocalHostService {
    
    @Override
    public String getIp() {
        return getLocalHost().getHostAddress();
    }
    
    @Override
    public String getHostName() {
        return getLocalHost().getHostName();
    }
    
    private static InetAddress getLocalHost() {
        InetAddress result;
        try {
            result = InetAddress.getLocalHost();
        } catch (final UnknownHostException ex) {
            throw new JobException(ex);
        }
        return result;
    }
}
