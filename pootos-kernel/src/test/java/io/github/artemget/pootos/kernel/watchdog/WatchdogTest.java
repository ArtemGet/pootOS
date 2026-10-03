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

import io.github.artemget.pootos.kernel.InMemoryLeases;
import io.github.artemget.pootos.kernel.LeaseException;
import io.github.artemget.pootos.kernel.Leases;
import io.github.artemget.pootos.kernel.Resource;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

/**
 * Test case for {@link ReclaimingWatchdog}.
 *
 * @since 0.0.1
 */
final class WatchdogTest {

    @Test
    void reclaimsExpiredLease() throws LeaseException {
        final Clock clock = Clock.fixed(
            Instant.parse("2026-01-01T00:00:00Z"), ZoneOffset.UTC
        );
        final Leases leases = new InMemoryLeases(clock, Duration.ofSeconds(-1));
        MatcherAssert.assertThat(
            "An expired lease must be reclaimed",
            new ReclaimingWatchdog(leases, clock).sweep(
                List.of(leases.acquire(new Resource("gpu:0"), () -> "agent-a"))
            ),
            Matchers.is(1)
        );
    }

    @Test
    void leavesLiveLeaseAlone() throws LeaseException {
        final Clock clock = Clock.fixed(
            Instant.parse("2026-01-01T00:00:00Z"), ZoneOffset.UTC
        );
        final Leases leases = new InMemoryLeases(clock, Duration.ofMinutes(1));
        MatcherAssert.assertThat(
            "A live lease must not be reclaimed",
            new ReclaimingWatchdog(leases, clock).sweep(
                List.of(leases.acquire(new Resource("gpu:0"), () -> "agent-a"))
            ),
            Matchers.is(0)
        );
    }

    @Test
    void keepsLiveResourceHeld() throws LeaseException {
        final Clock clock = Clock.fixed(
            Instant.parse("2026-01-01T00:00:00Z"), ZoneOffset.UTC
        );
        final Leases leases = new InMemoryLeases(clock, Duration.ofMinutes(1));
        new ReclaimingWatchdog(leases, clock).sweep(
            List.of(leases.acquire(new Resource("gpu:0"), () -> "agent-a"))
        );
        MatcherAssert.assertThat(
            "A resource under a live lease must remain held",
            leases.held(new Resource("gpu:0")),
            Matchers.is(true)
        );
    }
}
