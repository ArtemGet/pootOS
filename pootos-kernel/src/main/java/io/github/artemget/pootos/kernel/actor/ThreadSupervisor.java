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
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicBoolean;
import org.cactoos.Text;
import org.cactoos.text.UncheckedText;

/**
 * A supervisor that runs every actor on its own virtual thread.
 *
 * <p>This object is stateful on purpose: it owns the live actor threads and
 * their failure counters, so that actors can fail and be restarted while the
 * supervisor keeps observing them. A failed step is caught here - the single
 * recovery point of the kernel - the failure is recorded, and the actor is
 * restarted after a bounded backoff instead of taking the kernel down.</p>
 *
 * @since 0.0.1
 */
public final class ThreadSupervisor implements Supervisor {

    /**
     * Threads currently running actors.
     */
    private final Collection<Thread> threads;

    /**
     * Failure count per actor id.
     */
    private final ConcurrentMap<String, Integer> failures;

    /**
     * Whether the supervisor is still running.
     */
    private final AtomicBoolean running;

    /**
     * Base backoff before restarting a failed actor.
     */
    private final Duration backoff;

    /**
     * Upper bound of the backoff.
     */
    private final Duration ceiling;

    /**
     * Ctor.
     *
     * @param backoff Base backoff before restarting a failed actor
     * @param ceiling Upper bound of the backoff
     */
    public ThreadSupervisor(final Duration backoff, final Duration ceiling) {
        this.threads = new ConcurrentLinkedQueue<>();
        this.failures = new ConcurrentHashMap<>();
        this.running = new AtomicBoolean(true);
        this.backoff = backoff;
        this.ceiling = ceiling;
    }

    @Override
    public void supervise(final Actor actor) {
        final String ident = new UncheckedText(actor.id()).asString();
        this.failures.putIfAbsent(ident, 0);
        this.threads.add(
            Thread.ofVirtual().name(ident).start(
                () -> this.loop(actor, ident)
            )
        );
    }

    @Override
    public int failures(final Text id) {
        return this.failures.getOrDefault(
            new UncheckedText(id).asString(), 0
        );
    }

    @Override
    public void close() throws InterruptedException {
        this.running.set(false);
        for (final Thread thread : this.threads) {
            thread.interrupt();
        }
        for (final Thread thread : this.threads) {
            thread.join();
        }
    }

    private void loop(final Actor actor, final String ident) {
        while (this.running.get()) {
            try {
                actor.step();
            } catch (final ActorException failure) {
                this.failures.merge(ident, 1, Integer::sum);
                this.pause(this.failures.getOrDefault(ident, 0));
            }
        }
    }

    private void pause(final int count) {
        Duration delay = this.backoff.multipliedBy(count);
        if (delay.compareTo(this.ceiling) > 0) {
            delay = this.ceiling;
        }
        try {
            Thread.sleep(delay.toMillis());
        } catch (final InterruptedException ignored) {
            Thread.currentThread().interrupt();
        }
    }
}
