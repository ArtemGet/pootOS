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

package io.github.artemget.pootos.kernel.watchdog;

import io.github.artemget.pootos.kernel.Lease;
import io.github.artemget.pootos.kernel.Leases;
import java.time.Clock;

/**
 * A watchdog that releases every candidate lease whose time-to-live has passed.
 *
 * @since 0.0.1
 */
public final class ReclaimingWatchdog implements Watchdog {

    /**
     * Arbiter that owns the live leases.
     */
    private final Leases registry;

    /**
     * Clock used to decide whether a lease has expired.
     */
    private final Clock clock;

    /**
     * Ctor.
     *
     * @param registry Arbiter that owns the live leases
     * @param clock Clock used to decide whether a lease has expired
     */
    public ReclaimingWatchdog(final Leases registry, final Clock clock) {
        this.registry = registry;
        this.clock = clock;
    }

    @Override
    public int sweep(final Iterable<Lease> candidates) {
        int reclaimed = 0;
        for (final Lease candidate : candidates) {
            if (candidate.expired(this.clock)) {
                this.registry.release(candidate);
                reclaimed = reclaimed + 1;
            }
        }
        return reclaimed;
    }
}
