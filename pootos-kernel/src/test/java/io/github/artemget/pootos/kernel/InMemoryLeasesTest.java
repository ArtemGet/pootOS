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

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Test case for {@link InMemoryLeases}.
 *
 * @since 0.0.1
 */
final class InMemoryLeasesTest {

    @Test
    void acquiresFreeResource() throws LeaseException {
        final Leases leases = new InMemoryLeases(
            Clock.fixed(Instant.parse("2026-01-01T00:00:00Z"), ZoneOffset.UTC),
            Duration.ofMinutes(1)
        );
        leases.acquire(new Resource("gpu:0"), () -> "agent-a");
        MatcherAssert.assertThat(
            "A free resource must be held after acquisition",
            leases.held(new Resource("gpu:0")),
            Matchers.is(true)
        );
    }

    @Test
    void rejectsSecondOwner() throws LeaseException {
        final Leases leases = new InMemoryLeases(
            Clock.fixed(Instant.parse("2026-01-01T00:00:00Z"), ZoneOffset.UTC),
            Duration.ofMinutes(1)
        );
        leases.acquire(new Resource("gpu:0"), () -> "agent-a");
        MatcherAssert.assertThat(
            "A live exclusive lease must reject a second owner",
            Assertions.assertThrows(
                LeaseException.class,
                () -> leases.acquire(new Resource("gpu:0"), () -> "agent-b")
            ).getMessage(),
            Matchers.containsString("gpu:0")
        );
    }

    @Test
    void releasesResource() throws LeaseException {
        final Leases leases = new InMemoryLeases(
            Clock.fixed(Instant.parse("2026-01-01T00:00:00Z"), ZoneOffset.UTC),
            Duration.ofMinutes(1)
        );
        leases.release(
            leases.acquire(new Resource("gpu:0"), () -> "agent-a")
        );
        MatcherAssert.assertThat(
            "A released resource must no longer be held",
            leases.held(new Resource("gpu:0")),
            Matchers.is(false)
        );
    }

    @Test
    void reclaimsExpiredLease() throws LeaseException {
        final Leases leases = new InMemoryLeases(
            Clock.fixed(Instant.parse("2026-01-01T00:00:00Z"), ZoneOffset.UTC),
            Duration.ofSeconds(-1)
        );
        leases.acquire(new Resource("gpu:0"), () -> "agent-a");
        MatcherAssert.assertThat(
            "An expired lease must be reclaimable by a new owner",
            leases.acquire(new Resource("gpu:0"), () -> "agent-b"),
            Matchers.notNullValue()
        );
    }
}
