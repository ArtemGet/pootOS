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
import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * An in-memory resource arbiter.
 *
 * <p>This object is stateful on purpose: it is the arbiter that owns the
 * current set of leases. The state is a {@link ConcurrentMap} of live leases
 * so that grants and releases are safe under the virtual threads that run
 * agents in parallel. A lease expires once its time-to-live has passed, so
 * a crashed owner cannot hold a resource forever and no deadlock is possible
 * by construction.</p>
 *
 * @since 0.0.1
 */
public final class InMemoryLeases implements Leases {

    /**
     * Leases currently held, keyed by resource.
     */
    private final ConcurrentMap<Resource, Lease> held;

    /**
     * Clock used to read the current moment.
     */
    private final Clock clock;

    /**
     * Time to live of every granted lease.
     */
    private final Duration ttl;

    /**
     * Ctor.
     *
     * @param clock Clock to read the current moment from
     * @param ttl Time to live of every granted lease
     */
    public InMemoryLeases(final Clock clock, final Duration ttl) {
        this.held = new ConcurrentHashMap<>();
        this.clock = clock;
        this.ttl = ttl;
    }

    @Override
    public Lease acquire(final Resource resource, final Text owner)
        throws LeaseException {
        final Lease fresh = new ExpiringLease(
            resource, owner, this.clock.instant().plus(this.ttl)
        );
        final Optional<Lease> current = Optional.ofNullable(
            this.held.putIfAbsent(resource, fresh)
        );
        if (current.filter(lease -> !lease.expired(this.clock)).isPresent()) {
            throw new LeaseException(
                "Resource '%s' is held by another lease".formatted(
                    resource.value()
                )
            );
        }
        if (current.isPresent()
            && !this.held.replace(resource, current.get(), fresh)) {
            throw new LeaseException(
                "Resource '%s' is held by another lease".formatted(
                    resource.value()
                )
            );
        }
        return fresh;
    }

    @Override
    public void release(final Lease lease) {
        this.held.remove(lease.resource(), lease);
    }

    @Override
    public boolean held(final Resource resource) {
        return Optional.ofNullable(this.held.get(resource))
            .filter(lease -> !lease.expired(this.clock))
            .isPresent();
    }
}
