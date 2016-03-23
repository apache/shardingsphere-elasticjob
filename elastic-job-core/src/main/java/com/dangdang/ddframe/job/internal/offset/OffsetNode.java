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

package com.dangdang.ddframe.job.internal.offset;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Elastic Job数据处理位置节点名称的常量类.
 * 
 * @author zhangliang
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class OffsetNode {
    
    static final String ROOT = "offset";
    
    private static final String ITEM = ROOT + "/%s";
    
    /**
     * 获取分片数据处理位置节点路径.
     * 
     * @param item 作业项
     * @return 分片数据处理位置节点路径
     */
    public static String getItemNode(final int item) {
        return String.format(ITEM, item);
    }
}
