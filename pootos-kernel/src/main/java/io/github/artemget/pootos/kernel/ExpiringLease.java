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

import io.github.artemget.pootos.context.node.Text;
import java.time.Clock;
import java.time.Instant;

/**
 * A lease that becomes invalid after a fixed moment in time.
 *
 * @since 0.0.1
 */
public final class ExpiringLease implements Lease {

    /**
     * Resource this lease grants.
     */
    private final Resource resource;

    /**
     * Owner of this lease.
     */
    private final Text owner;

    /**
     * Moment after which this lease is invalid.
     */
    private final Instant until;

    /**
     * Ctor.
     *
     * @param resource Resource this lease grants
     * @param owner Owner of this lease
     * @param until Moment after which this lease is invalid
     */
    public ExpiringLease(final Resource resource, final Text owner, final Instant until) {
        this.resource = resource;
        this.owner = owner;
        this.until = until;
    }

    @Override
    public Resource resource() {
        return this.resource;
    }

    @Override
    public Text owner() {
        return this.owner;
    }

    @Override
    public boolean expired(final Clock clock) {
        return clock.instant().isAfter(this.until);
    }
}
