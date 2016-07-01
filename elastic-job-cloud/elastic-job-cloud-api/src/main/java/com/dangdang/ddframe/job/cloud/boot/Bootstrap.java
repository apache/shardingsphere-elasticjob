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
 *
 */

package com.dangdang.ddframe.job.cloud.boot;

import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Properties;

/**
 * 云作业启动入口.
 *
 * @author caohao
 */
public final class Bootstrap {
    // CHECKSTYLE:OFF
    public static void main(final String[] args) throws ClassNotFoundException, IllegalAccessException, InstantiationException, NoSuchMethodException, InvocationTargetException, IOException {
    // CHECKSTYLE:ON
        String taskId = args[0];
        Properties properties = new Properties();
        properties.load(new FileInputStream("conf/job.properties"));
        Object object = properties.get("jobClassNames");
        String[] jobClassNames = object.toString().split(",");
        for (String jobClassName : jobClassNames) {
            Class<?> cloudElasticJobClass = Class.forName(jobClassName);
            Object cloudElasticJob = cloudElasticJobClass.getConstructor(String.class).newInstance(taskId);
            cloudElasticJobClass.getMethod("execute").invoke(cloudElasticJob);
        }
    }
}
