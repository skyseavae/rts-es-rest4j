/*
 * Licensed to Elasticsearch under one or more contributor
 * license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright
 * ownership. Elasticsearch licenses this file to you under
 * the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.elasticsearch.common.recycler;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * A {@link Recycler} implementation based on a concurrent {@link java.util.Deque}. This implementation is thread-safe.
 */
public class ConcurrentDequeRecycler<T> extends DequeRecycler<T> {

    // we maintain size separately because concurrent deque implementations typically have linear-time size() impls
    final AtomicInteger size;

    public ConcurrentDequeRecycler(Recycler.C<T> c, int maxSize) {
        // todo:mazhen change it to null
        super(c, null, maxSize);
        this.size = new AtomicInteger();
    }

    @Override
    public void close() {
        assert deque.size() == size.get();
        super.close();
        size.set(0);
    }

    @Override
    public Recycler.V<T> obtain(int sizing) {
        final Recycler.V<T> v = super.obtain(sizing);
        if (v.isRecycled()) {
            size.decrementAndGet();
        }
        return v;
    }

    @Override
    protected boolean beforeRelease() {
        return size.incrementAndGet() <= maxSize;
    }

    @Override
    protected void afterRelease(boolean recycled) {
        if (!recycled) {
            size.decrementAndGet();
        }
    }

}
