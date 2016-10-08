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

package com.dangdang.ddframe.job.lite.lifecycle.domain;

import org.junit.Test;

import java.util.Collections;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public final class ServerBriefStatusTest {
    
    @Test
    public void assertGetServerBriefStatusForAllCrashed() {
        assertThat(ServerBriefInfo.ServerBriefStatus.getServerBriefStatus(Collections.<String>emptyList(), Collections.singletonList("localhost"), "localhost"),
                is(ServerBriefInfo.ServerBriefStatus.ALL_CRASHED));
    }
    
    @Test
    public void assertGetServerBriefStatusForOk() {
        assertThat(ServerBriefInfo.ServerBriefStatus.getServerBriefStatus(Collections.singletonList("localhost"), Collections.<String>emptyList(), "localhost"),
                is(ServerBriefInfo.ServerBriefStatus.OK));
    }
    
    @Test
    public void assertGetServerBriefStatusForPartialAlive() {
        assertThat(ServerBriefInfo.ServerBriefStatus.getServerBriefStatus(Collections.singletonList("localhost"), Collections.singletonList("localhost"), "localhost"),
                is(ServerBriefInfo.ServerBriefStatus.PARTIAL_ALIVE));
    }
}
