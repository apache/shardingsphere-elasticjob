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

package io.elasticjob.lite.spring.test;

import io.elasticjob.lite.reg.exception.RegExceptionHandler;
import io.elasticjob.lite.util.concurrent.BlockUtils;
import org.apache.curator.test.TestingServer;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.support.AbstractTestExecutionListener;

import java.io.File;
import java.io.IOException;

public final class EmbedZookeeperTestExecutionListener extends AbstractTestExecutionListener {
    
    private static volatile TestingServer testingServer;
    
    @Override
    public void beforeTestClass(final TestContext testContext) throws Exception {
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
            Runtime.getRuntime().addShutdownHook(new Thread() {
                
                @Override
                public void run() {
                    try {
                        BlockUtils.sleep(2000L);
                        testingServer.close();
                    } catch (final IOException ex) {
                        RegExceptionHandler.handleException(ex);
                    }
                }
            });
        }
    }
}
