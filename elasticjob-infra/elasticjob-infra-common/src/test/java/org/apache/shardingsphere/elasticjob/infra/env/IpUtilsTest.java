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

import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class IpUtilsTest {
    
    @Test
    public void assertGetIp() {
        assertNotNull(IpUtils.getIp());
    }
    
    @Test
    @SneakyThrows
    public void assertPreferredNetworkInterface() {
        System.setProperty(IpUtils.PREFERRED_NETWORK_INTERFACE, "eth0");
        Method declaredMethod = IpUtils.class.getDeclaredMethod("isPreferredNetworkInterface", NetworkInterface.class);
        declaredMethod.setAccessible(true);
        NetworkInterface mockNetworkInterface = mock(NetworkInterface.class);
        when(mockNetworkInterface.getDisplayName()).thenReturn("eth0");
        boolean result = (boolean) declaredMethod.invoke("isPreferredNetworkInterface", mockNetworkInterface);
        assertTrue(result);
        System.clearProperty(IpUtils.PREFERRED_NETWORK_INTERFACE);
    }
    
    @Test
    @SneakyThrows
    public void assertPreferredNetworkAddress() {
        Method declaredMethod = IpUtils.class.getDeclaredMethod("isPreferredAddress", InetAddress.class);
        declaredMethod.setAccessible(true);
        InetAddress inetAddress = mock(InetAddress.class);
        System.setProperty(IpUtils.PREFERRED_NETWORK_IP, "192.168");
        when(inetAddress.getHostAddress()).thenReturn("192.168.0.100");
        assertTrue((boolean) declaredMethod.invoke("isPreferredAddress", inetAddress));
        when(inetAddress.getHostAddress()).thenReturn("10.10.0.100");
        assertFalse((boolean) declaredMethod.invoke("isPreferredAddress", inetAddress));
        System.clearProperty(IpUtils.PREFERRED_NETWORK_IP);
        System.setProperty(IpUtils.PREFERRED_NETWORK_IP, "10.10.*");
        when(inetAddress.getHostAddress()).thenReturn("10.10.0.100");
        assertTrue((boolean) declaredMethod.invoke("isPreferredAddress", inetAddress));
        when(inetAddress.getHostAddress()).thenReturn("10.0.0.100");
        assertFalse((boolean) declaredMethod.invoke("isPreferredAddress", inetAddress));
        System.clearProperty(IpUtils.PREFERRED_NETWORK_IP);
    }
    
    @Test
    @SneakyThrows
    public void assertGetFirstNetworkInterface() {
        InetAddress address1 = mock(Inet4Address.class);
        when(address1.isLoopbackAddress()).thenReturn(false);
        when(address1.isAnyLocalAddress()).thenReturn(false);
        when(address1.isReachable(100)).thenReturn(true);
        when(address1.getHostAddress()).thenReturn("10.10.0.1");
        Vector<InetAddress> addresses1 = new Vector<>();
        addresses1.add(address1);
        InetAddress address2 = mock(Inet4Address.class);
        when(address2.isLoopbackAddress()).thenReturn(false);
        when(address2.isAnyLocalAddress()).thenReturn(false);
        when(address2.isReachable(100)).thenReturn(true);
        when(address2.getHostAddress()).thenReturn("192.168.99.100");
        Vector<InetAddress> addresses2 = new Vector<>();
        addresses2.add(address2);
        NetworkInterface networkInterface1 = mock(NetworkInterface.class);
        NetworkInterface networkInterface2 = mock(NetworkInterface.class);
        when(networkInterface1.getInetAddresses()).thenReturn(addresses1.elements());
        when(networkInterface2.getInetAddresses()).thenReturn(addresses2.elements());
        when(networkInterface1.getDisplayName()).thenReturn("eth1");
        when(networkInterface2.getDisplayName()).thenReturn("eth2");
        Method declaredMethod = IpUtils.class.getDeclaredMethod("getFirstNetworkInterface", List.class);
        declaredMethod.setAccessible(true);
        List<NetworkInterface> validNetworkInterfaces = Arrays.asList(networkInterface1, networkInterface2);
        assertThat(declaredMethod.invoke("getFirstNetworkInterface", validNetworkInterfaces), is(networkInterface2));
        System.setProperty(IpUtils.PREFERRED_NETWORK_INTERFACE, "eth1");
        assertThat(declaredMethod.invoke("getFirstNetworkInterface", validNetworkInterfaces), is(networkInterface1));
        System.clearProperty(IpUtils.PREFERRED_NETWORK_INTERFACE);
        System.setProperty(IpUtils.PREFERRED_NETWORK_IP, "10.10.*");
        assertThat(declaredMethod.invoke("getFirstNetworkInterface", validNetworkInterfaces), is(networkInterface1));
        System.clearProperty(IpUtils.PREFERRED_NETWORK_IP);
    }
    
    @Test
    @SneakyThrows
    public void assertGetHostName() {
        assertNotNull(IpUtils.getHostName());
        Field field = IpUtils.class.getDeclaredField("cachedHostName");
        field.setAccessible(true);
        String hostName = (String) field.get(null);
        assertThat(hostName, is(IpUtils.getHostName()));
    }
}
