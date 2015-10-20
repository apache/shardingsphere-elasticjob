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
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;

import com.dangdang.ddframe.job.exception.JobException;
import com.dangdang.ddframe.job.internal.util.NetUtils;
import lombok.extern.slf4j.Slf4j;

/**
 * 获取真实本机网络的实现类类.
 * 
 * @author zhangliang
 */
@Slf4j
public final class RealLocalHostService implements LocalHostService {

    @Override
    public String getIp() {
        return NetUtils.getLocalHost();
    }

    @Override
    public String getHostName() {
        return NetUtils.getHostName(getIp());
    }

}
