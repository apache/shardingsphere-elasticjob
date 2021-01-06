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

package org.apache.shardingsphere.elasticjob.lite.spring.namespace.test;

import org.apache.curator.test.TestingServer;
import org.apache.shardingsphere.elasticjob.reg.exception.RegExceptionHandler;
import org.apache.shardingsphere.elasticjob.infra.concurrent.BlockUtils;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.support.AbstractTestExecutionListener;

import java.io.File;
import java.io.IOException;

public final class EmbedZookeeperTestExecutionListener extends AbstractTestExecutionListener {
    
    private static volatile TestingServer testingServer;
    
    @Override
    public void beforeTestClass(final TestContext testContext) {
        startEmbedTestingServer();
    }
    
    private static void startEmbedTestingServer() {
        if (null != testingServer) {
            return;
        }
        try {
            testingServer = new TestingServer(3181, new File(String.format("target/test_zk_data/%s/", System.nanoTime())));
            // CHECKSTYLE:OFF
        } catch (final Exception ex) {
            // CHECKSTYLE:ON
            RegExceptionHandler.handleException(ex);
        } finally {
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                try {
                    BlockUtils.sleep(2000L);
                    testingServer.close();
                } catch (final IOException ex) {
                    RegExceptionHandler.handleException(ex);
                }
            }));
        }
    }
}
