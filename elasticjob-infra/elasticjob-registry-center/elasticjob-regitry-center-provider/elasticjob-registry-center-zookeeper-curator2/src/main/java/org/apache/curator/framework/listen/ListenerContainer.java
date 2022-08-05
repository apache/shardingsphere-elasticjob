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

package org.apache.curator.framework.listen;

import java.util.Map;
import java.util.concurrent.Executor;
import java.util.function.Function;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Maps;
import com.google.common.util.concurrent.MoreExecutors;

/**
 * Abstracts an object that has listeners.
 */
public class ListenerContainer<T> implements Listenable<T> {
    private final Logger log = LoggerFactory.getLogger(getClass());

    private final Map<T, ListenerEntry<T>> listeners = Maps.newConcurrentMap();

    @Override
    public void addListener(final T listener) {
        addListener(listener, MoreExecutors.newDirectExecutorService());
    }

    @Override
    public void addListener(final T listener, final Executor executor) {
        listeners.put(listener, new ListenerEntry<T>(listener, executor));
    }

    @Override
    public void removeListener(final T listener) {
        listeners.remove(listener);
    }

    /**
     * Remove all listeners.
     */
    public void clear() {
        listeners.clear();
    }

    /**
     * Return the number of listeners.
     *
     * @return number
     */
    public int size() {
        return listeners.size();
    }

    /**
     * Utility - apply the given function to each listener. The function receives the listener as an argument.
     *
     * @param function function to call for each listener
     */
    public void forEach(final Function<T, @Nullable Void> function) {
        for (final ListenerEntry<T> entry : listeners.values()) {
            entry.executor.execute(
                () -> function.apply(entry.listener)
            );
        }
    }
}
