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

package com.dangdang.ddframe.job.console.repository.zookeeper.impl;

import java.nio.charset.Charset;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.CuratorFrameworkFactory.Builder;
import org.apache.curator.framework.api.ACLProvider;
import org.apache.curator.retry.RetryOneTime;
import org.apache.curator.utils.CloseableUtils;
import org.apache.zookeeper.KeeperException.NoNodeException;
import org.apache.zookeeper.KeeperException.NodeExistsException;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.data.ACL;
import org.springframework.stereotype.Repository;

import com.dangdang.ddframe.job.console.exception.JobConsoleException;
import com.dangdang.ddframe.job.console.repository.zookeeper.CuratorRepository;
import com.dangdang.ddframe.job.console.util.SessionCuratorClient;
import com.google.common.base.Strings;

@Repository
public class CuratorRepositoryImpl implements CuratorRepository {
    
    private static final int WAITING_SECONDS = 2;
    
    @Override
    public CuratorFramework connect(final String connectString, final String namespace, final String digest) {
        Builder builder = CuratorFrameworkFactory.builder().connectString(connectString).retryPolicy(new RetryOneTime(1000)).namespace(namespace);
        if (!Strings.isNullOrEmpty(digest)) {
            builder.authorization("digest", digest.getBytes())
                   .aclProvider(new ACLProvider() {
                       
                       @Override
                       public List<ACL> getDefaultAcl() {
                           return ZooDefs.Ids.CREATOR_ALL_ACL;
                       }
                       
                       @Override
                       public List<ACL> getAclForPath(final String path) {
                           return ZooDefs.Ids.CREATOR_ALL_ACL;
                       }
                   });
        }
        CuratorFramework client = builder.build();
        client.start();
        boolean established = false;
        try {
            established = client.blockUntilConnected(WAITING_SECONDS, TimeUnit.SECONDS);
        } catch (final InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
        if (established) {
            return client;
        }
        CloseableUtils.closeQuietly(client);
        return null;
    }
    
    @Override
    public boolean checkExists(final String znode) {
        try {
            return null != SessionCuratorClient.getCuratorClient().checkExists().forPath(znode);
        //CHECKSTYLE:OFF
        } catch (final Exception ex) {
        //CHECKSTYLE:ON
            throw new JobConsoleException(ex);
        }
    }
    
    @Override
    public String getData(final String znode) {
        try {
            if (checkExists(znode)) {
                return new String(SessionCuratorClient.getCuratorClient().getData().forPath(znode), Charset.forName("UTF-8"));
            } else {
                return null;
            }
        } catch (final NoNodeException ex) {
            return null;
        //CHECKSTYLE:OFF
        } catch (final Exception ex) {
        //CHECKSTYLE:ON
            throw new JobConsoleException(ex);
        }
    }
    
    @Override
    public List<String> getChildren(final String znode) {
        try {
            return SessionCuratorClient.getCuratorClient().getChildren().forPath(znode);
        //CHECKSTYLE:OFF
        } catch (final Exception ex) {
        //CHECKSTYLE:ON
            throw new JobConsoleException(ex);
        }
    }
    
    @Override
    public void create(final String znode) {
        try {
            SessionCuratorClient.getCuratorClient().create().forPath(znode, new String("").getBytes());
        } catch (final NodeExistsException ex) {
        //CHECKSTYLE:OFF
        } catch (final Exception ex) {
        //CHECKSTYLE:ON
            throw new JobConsoleException(ex);
        }
    }
    
    public void update(final String znode, final Object value) {
        try {
            SessionCuratorClient.getCuratorClient().inTransaction().check().forPath(znode).and().setData().forPath(znode, value.toString().getBytes(Charset.forName("UTF-8"))).and().commit();
        } catch (final NoNodeException ex) {
        //CHECKSTYLE:OFF
        } catch (final Exception ex) {
        //CHECKSTYLE:ON
            throw new JobConsoleException(ex);
        }
    }
    
    @Override
    public void delete(final String znode) {
        try {
            if (null != SessionCuratorClient.getCuratorClient().checkExists().forPath(znode)) {
                SessionCuratorClient.getCuratorClient().delete().forPath(znode);
            }
        } catch (final NoNodeException ex) {
        //CHECKSTYLE:OFF
        } catch (final Exception ex) {
        //CHECKSTYLE:ON
            throw new JobConsoleException(ex);
        }
    }
}
