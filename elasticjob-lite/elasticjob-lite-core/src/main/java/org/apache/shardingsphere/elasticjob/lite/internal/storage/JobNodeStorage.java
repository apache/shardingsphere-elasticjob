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

package org.apache.shardingsphere.elasticjob.lite.internal.storage;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.api.transaction.CuratorOp;
import org.apache.curator.framework.api.transaction.TransactionOp;
import org.apache.curator.framework.recipes.cache.CuratorCache;
import org.apache.curator.framework.recipes.cache.CuratorCacheListener;
import org.apache.curator.framework.recipes.leader.LeaderLatch;
import org.apache.curator.framework.state.ConnectionStateListener;
import org.apache.shardingsphere.elasticjob.infra.exception.JobSystemException;
import org.apache.shardingsphere.elasticjob.reg.base.CoordinatorRegistryCenter;
import org.apache.shardingsphere.elasticjob.reg.exception.RegExceptionHandler;

import java.util.LinkedList;
import java.util.List;

/**
 * Job node storage.
 */
public final class JobNodeStorage {
    
    private final CoordinatorRegistryCenter regCenter;
    
    private final String jobName;
    
    private final JobNodePath jobNodePath;
    
    public JobNodeStorage(final CoordinatorRegistryCenter regCenter, final String jobName) {
        this.regCenter = regCenter;
        this.jobName = jobName;
        jobNodePath = new JobNodePath(jobName);
    }
    
    /**
     * Judge is job node existed or not.
     * 
     * @param node node
     * @return is job node existed or not
     */
    public boolean isJobNodeExisted(final String node) {
        return regCenter.isExisted(jobNodePath.getFullPath(node));
    }
    
    /**
     * Judge is job root node existed or not.
     *
     * @return is job root node existed or not
     */
    public boolean isJobRootNodeExisted() {
        return regCenter.isExisted("/" + jobName);
    }
    
    /**
     * Get job node data.
     * 
     * @param node node
     * @return data of job node
     */
    public String getJobNodeData(final String node) {
        return regCenter.get(jobNodePath.getFullPath(node));
    }
    
    /**
     * Get job node data from registry center directly.
     * 
     * @param node node
     * @return data of job node
     */
    public String getJobNodeDataDirectly(final String node) {
        return regCenter.getDirectly(jobNodePath.getFullPath(node));
    }
    
    /**
     * Get job node children keys.
     * 
     * @param node node
     * @return children keys
     */
    public List<String> getJobNodeChildrenKeys(final String node) {
        return regCenter.getChildrenKeys(jobNodePath.getFullPath(node));
    }
    
    /**
     * Get job root node data.
     *
     * @return data of job node
     */
    public String getJobRootNodeData() {
        return regCenter.get("/" + jobName);
    }
    
    /**
     * Create job node if needed.
     * 
     * <p>Do not create node if root node not existed, which means job is shutdown.</p>
     * 
     * @param node node
     */
    public void createJobNodeIfNeeded(final String node) {
        if (isJobRootNodeExisted() && !isJobNodeExisted(node)) {
            regCenter.persist(jobNodePath.getFullPath(node), "");
        }
    }
    
    /**
     * Remove job node if existed.
     * 
     * @param node node
     */
    public void removeJobNodeIfExisted(final String node) {
        if (isJobNodeExisted(node)) {
            regCenter.remove(jobNodePath.getFullPath(node));
        }
    }
        
    /**
     * Fill job node.
     *
     * @param node node
     * @param value data of job node
     */
    public void fillJobNode(final String node, final Object value) {
        regCenter.persist(jobNodePath.getFullPath(node), value.toString());
    }
    
    /**
     * Fill ephemeral job node.
     * 
     * @param node node
     * @param value data of job node
     */
    public void fillEphemeralJobNode(final String node, final Object value) {
        regCenter.persistEphemeral(jobNodePath.getFullPath(node), value.toString());
    }
    
    /**
     * Update job node.
     * 
     * @param node node
     * @param value data of job node
     */
    public void updateJobNode(final String node, final Object value) {
        regCenter.update(jobNodePath.getFullPath(node), value.toString());
    }
    
    /**
     * Replace data.
     * 
     * @param node node
     * @param value to be replaced data
     */
    public void replaceJobNode(final String node, final Object value) {
        regCenter.persist(jobNodePath.getFullPath(node), value.toString());
    }
    
    /**
     * Replace data to root node.
     *
     * @param value to be replaced data
     */
    public void replaceJobRootNode(final Object value) {
        regCenter.persist("/" + jobName, value.toString());
    }
    
    /**
     * Execute operator in transaction.
     * 
     * @param callback transaction execution callback
     */
    public void executeInTransaction(final TransactionExecutionCallback callback) {
        try {
            List<CuratorOp> operations = new LinkedList<>();
            CuratorFramework client = getClient();
            TransactionOp transactionOp = client.transactionOp();
            operations.add(transactionOp.check().forPath("/"));
            operations.addAll(callback.createCuratorOperators(transactionOp));
            client.transaction().forOperations(operations);
        //CHECKSTYLE:OFF
        } catch (final Exception ex) {
        //CHECKSTYLE:ON
            RegExceptionHandler.handleException(ex);
        }
    }
    
    /**
     * Execute in leader server.
     * 
     * @param latchNode node for leader latch
     * @param callback execute callback
     */
    public void executeInLeader(final String latchNode, final LeaderExecutionCallback callback) {
        try (LeaderLatch latch = new LeaderLatch(getClient(), jobNodePath.getFullPath(latchNode))) {
            latch.start();
            latch.await();
            callback.execute();
        //CHECKSTYLE:OFF
        } catch (final Exception ex) {
        //CHECKSTYLE:ON
            handleException(ex);
        }
    }
    
    private void handleException(final Exception ex) {
        if (ex instanceof InterruptedException) {
            Thread.currentThread().interrupt();
        } else {
            throw new JobSystemException(ex);
        }
    }
    
    /**
     * Add connection state listener.
     * 
     * @param listener connection state listener
     */
    public void addConnectionStateListener(final ConnectionStateListener listener) {
        getClient().getConnectionStateListenable().addListener(listener);
    }
    
    private CuratorFramework getClient() {
        return (CuratorFramework) regCenter.getRawClient();
    }
    
    /**
     * Add data listener.
     * 
     * @param listener data listener
     */
    public void addDataListener(final CuratorCacheListener listener) {
        CuratorCache cache = (CuratorCache) regCenter.getRawCache("/" + jobName);
        cache.listenable().addListener(listener);
    }
    
    /**
     * Get registry center time.
     * 
     * @return registry center time
     */
    public long getRegistryCenterTime() {
        return regCenter.getRegistryCenterTime(jobNodePath.getFullPath("systemTime/current"));
    }
}
