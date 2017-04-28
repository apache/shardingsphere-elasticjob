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

package com.dangdang.ddframe.job.lite.console;

import com.dangdang.ddframe.job.lite.console.filter.GlobalConfigurationFilter;
import com.dangdang.ddframe.job.lite.console.restful.JobOperationRestfulApi;
import com.dangdang.ddframe.job.restful.RestfulServer;
import com.dangdang.ddframe.job.security.WwwAuthFilter;
import com.google.common.base.Optional;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 界面启动器.
 *
 * @author caohao
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Slf4j
public final class ConsoleBootstrap {
    
    private static final String CONSOLE_PATH = "console";
    
    /**
     * 启动RESTful服务并加载页面.
     * 
     * @param args 启动参数
     * @throws Exception 启动服务器异常
     */
    //CHECKSTYLE:OFF
    public static void main(final String[] args) throws Exception {
    //CHECKSTYLE:ON
        int port = 8899;
        if (1 == args.length) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (final NumberFormatException ex) {
                log.warn("Wrong port format, using default port 8899 instead.");
            }
        }
        RestfulServer restfulServer = new RestfulServer(port);
        restfulServer.addFilter(GlobalConfigurationFilter.class, "*.html")
                     .addFilter(WwwAuthFilter.class, "/")
                     .addFilter(WwwAuthFilter.class, "*.html")
                     .start(JobOperationRestfulApi.class.getPackage().getName(), Optional.of(CONSOLE_PATH));
    }
}
