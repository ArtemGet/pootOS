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

package io.github.artemget.pootos.kernel.actor;

import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.cactoos.Text;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

/**
 * Test case for {@link ThreadSupervisor}.
 *
 * @since 0.0.1
 */
final class ThreadSupervisorTest {

    @Test
    void runsActorSteps() throws Exception {
        final AtomicInteger steps = new AtomicInteger();
        final Actor actor = new Actor() {
            @Override
            public Text id() {
                return () -> "counter";
            }

            @Override
            public void step() {
                steps.incrementAndGet();
            }
        };
        final Supervisor supervisor = new ThreadSupervisor(
            Duration.ofMillis(1L), Duration.ofMillis(50L)
        );
        supervisor.supervise(actor);
        final long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(5L);
        while (steps.get() < 3 && System.nanoTime() < deadline) {
            Thread.sleep(5L);
        }
        supervisor.close();
        MatcherAssert.assertThat(
            "A supervised actor must run its steps",
            steps.get() >= 3,
            Matchers.is(true)
        );
    }

    @Test
    void restartsFailedActor() throws Exception {
        final Actor actor = new Actor() {
            @Override
            public Text id() {
                return () -> "failing";
            }

            @Override
            public void step() throws ActorException {
                throw new ActorException("step failed");
            }
        };
        final Supervisor supervisor = new ThreadSupervisor(
            Duration.ofMillis(1L), Duration.ofMillis(2L)
        );
        supervisor.supervise(actor);
        final long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(5L);
        while (supervisor.failures(actor.id()) < 3
            && System.nanoTime() < deadline) {
            Thread.sleep(5L);
        }
        final int count = supervisor.failures(actor.id());
        supervisor.close();
        MatcherAssert.assertThat(
            "A failed actor must be restarted",
            count >= 3,
            Matchers.is(true)
        );
    }

    @Test
    void keepsOtherActorsRunning() throws Exception {
        final Actor failing = new Actor() {
            @Override
            public Text id() {
                return () -> "failing";
            }

            @Override
            public void step() throws ActorException {
                throw new ActorException("step failed");
            }
        };
        final AtomicInteger steps = new AtomicInteger();
        final Actor healthy = new Actor() {
            @Override
            public Text id() {
                return () -> "healthy";
            }

            @Override
            public void step() {
                steps.incrementAndGet();
            }
        };
        final Supervisor supervisor = new ThreadSupervisor(
            Duration.ofMillis(1L), Duration.ofMillis(50L)
        );
        supervisor.supervise(failing);
        supervisor.supervise(healthy);
        final long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(5L);
        while (steps.get() < 3 && System.nanoTime() < deadline) {
            Thread.sleep(5L);
        }
        supervisor.close();
        MatcherAssert.assertThat(
            "A failing actor must not stop a healthy one",
            steps.get() >= 3,
            Matchers.is(true)
        );
    }

    @Test
    void stopsActorOnClose() throws Exception {
        final AtomicInteger steps = new AtomicInteger();
        final Actor actor = new Actor() {
            @Override
            public Text id() {
                return () -> "closing";
            }

            @Override
            public void step() {
                steps.incrementAndGet();
            }
        };
        final Supervisor supervisor = new ThreadSupervisor(
            Duration.ofMillis(1L), Duration.ofMillis(50L)
        );
        supervisor.supervise(actor);
        final long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(5L);
        while (steps.get() < 1 && System.nanoTime() < deadline) {
            Thread.sleep(5L);
        }
        supervisor.close();
        final int frozen = steps.get();
        Thread.sleep(50L);
        MatcherAssert.assertThat(
            "No steps must run after close",
            steps.get(),
            Matchers.is(frozen)
        );
    }
}
