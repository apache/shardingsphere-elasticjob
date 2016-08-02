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
 */

package com.dangdang.ddframe.job.cloud.example;

import com.dangdang.ddframe.job.api.exception.JobExecutionEnvironmentException;
import com.dangdang.ddframe.job.cloud.api.Bootstrap;

public class Main {
    
    // CHECKSTYLE:OFF
    public static void main(final String[] args) throws JobExecutionEnvironmentException {
    // CHECKSTYLE:ON
        Bootstrap.execute(args);
    }
}
