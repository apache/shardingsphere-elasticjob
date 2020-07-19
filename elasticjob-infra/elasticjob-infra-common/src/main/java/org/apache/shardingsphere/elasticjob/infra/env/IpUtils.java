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

package org.apache.shardingsphere.elasticjob.infra.env;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.net.Inet6Address;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

/**
 * IP address utility.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class IpUtils {
    
    public static final String IP_REGEX = "((\\d|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])(\\.(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|\\d)){3})";
    
    private static final String PREFERRED_NETWORK_INTERFACE = "elasticjob.preferred.network.interface";
    
    private static volatile String cachedIpAddress;
    
    /**
     * Get IP address for localhost.
     * 
     * @return IP address for localhost
     */
    public static String getIp() {
        if (null != cachedIpAddress) {
            return cachedIpAddress;
        }
        NetworkInterface networkInterface = findNetworkInterface();
        if (null != networkInterface) {
            Enumeration<InetAddress> ipAddresses = networkInterface.getInetAddresses();
            while (ipAddresses.hasMoreElements()) {
                InetAddress ipAddress = ipAddresses.nextElement();
                if (isValidAddress(ipAddress)) {
                    cachedIpAddress = ipAddress.getHostAddress();
                    return cachedIpAddress;
                }
            }
        }
        throw new HostException("ip is null");
    }
    
    private static NetworkInterface findNetworkInterface() {
        Enumeration<NetworkInterface> interfaces;
        try {
            interfaces = NetworkInterface.getNetworkInterfaces();
        } catch (final SocketException ex) {
            throw new HostException(ex);
        }
        List<NetworkInterface> validNetworkInterfaces = new LinkedList<>();
        while (interfaces.hasMoreElements()) {
            NetworkInterface networkInterface = interfaces.nextElement();
            if (ignoreNetworkInterface(networkInterface)) {
                continue;
            }
            validNetworkInterfaces.add(networkInterface);
        }
        NetworkInterface result = null;
        for (NetworkInterface each : validNetworkInterfaces) {
            if (isPreferredNetworkInterface(each)) {
                result = each;
                break;
            }
        }
        if (null == result) {
            result = getFirstNetworkInterface(validNetworkInterfaces);
        }
        return result;
    }
    
    private static NetworkInterface getFirstNetworkInterface(final List<NetworkInterface> validNetworkInterfaces) {
        NetworkInterface result = null;
        for (NetworkInterface each : validNetworkInterfaces) {
            Enumeration<InetAddress> addresses = each.getInetAddresses();
            while (addresses.hasMoreElements()) {
                InetAddress inetAddress = addresses.nextElement();
                if (isValidAddress(inetAddress)) {
                    result = each;
                    break;
                }
            }
        }
        if (null == result && !validNetworkInterfaces.isEmpty()) {
            result = validNetworkInterfaces.get(0);
        }
        return result;
    }
    
    private static boolean isPreferredNetworkInterface(final NetworkInterface networkInterface) {
        String preferredNetworkInterface = System.getProperty(PREFERRED_NETWORK_INTERFACE);
        return Objects.equals(networkInterface.getDisplayName(), preferredNetworkInterface);
    }
    
    private static boolean ignoreNetworkInterface(final NetworkInterface networkInterface) {
        try {
            return null == networkInterface
                    || networkInterface.isLoopback()
                    || networkInterface.isVirtual()
                    || !networkInterface.isUp();
        } catch (final SocketException ex) {
            return true;
        }
    }
    
    private static boolean isValidAddress(final InetAddress inetAddress) {
        try {
            return !inetAddress.isLoopbackAddress() && !inetAddress.isAnyLocalAddress()
                    && !isIp6Address(inetAddress) && inetAddress.isReachable(100);
        } catch (final IOException ex) {
            return false;
        }
    }
    
    private static boolean isIp6Address(final InetAddress ipAddress) {
        return ipAddress instanceof Inet6Address;
    }
    
    /**
     * Get host name for localhost.
     * 
     * @return host name for localhost
     */
    public static String getHostName() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (final UnknownHostException ex) {
            return "unknown";
        }
    }
}
