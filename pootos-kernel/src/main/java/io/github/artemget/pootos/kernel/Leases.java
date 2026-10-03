/*
 * MIT License
 *
 * Copyright (c) 2024-2025. Artem Getmanskii
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package io.github.artemget.pootos.kernel;

import org.cactoos.Text;

/**
 * The resource arbiter: hands out and revokes exclusive leases.
 *
 * @since 0.0.1
 */
public interface Leases {

    /**
     * Try to acquire an exclusive lease on a resource.
     *
     * @param resource Resource to lease
     * @param owner Text identifying the owner
     * @return Granted lease
     * @throws LeaseException When the resource is held by a live lease
     */
    Lease acquire(Resource resource, Text owner) throws LeaseException;

    /**
     * Release a previously granted lease.
     *
     * @param lease Lease to release
     */
    void release(Lease lease);

    /**
     * Whether a resource is currently held by a live lease.
     *
     * @param resource Resource to query
     * @return True when a live lease exists
     */
    boolean held(Resource resource);
}
