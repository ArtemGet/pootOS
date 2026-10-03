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

package io.github.artemget.pootos.sandbox.docker;

import org.cactoos.Scalar;
import org.cactoos.Text;
import org.cactoos.scalar.Unchecked;

/**
 * A runtime that prefers a strong runtime and falls back when it is absent.
 *
 * <p>The preference is probed at use time. When the preferred runtime is
 * unavailable the fallback is selected and its {@link #status()} names the
 * degradation explicitly (ADR-001).</p>
 *
 * @since 0.0.1
 */
public final class ProbedRuntime implements SandboxRuntime {

    /**
     * Preferred runtime.
     */
    private final SandboxRuntime preferred;

    /**
     * Fallback runtime.
     */
    private final SandboxRuntime fallback;

    /**
     * Whether the preferred runtime is available.
     */
    private final Scalar<Boolean> available;

    /**
     * Ctor.
     *
     * @param preferred Preferred runtime
     * @param fallback Fallback runtime
     * @param available Availability of the preferred runtime
     */
    public ProbedRuntime(
        final SandboxRuntime preferred,
        final SandboxRuntime fallback,
        final Scalar<Boolean> available
    ) {
        this.preferred = preferred;
        this.fallback = fallback;
        this.available = available;
    }

    @Override
    public Text option() {
        return this.selected().option();
    }

    @Override
    public Text status() {
        return this.selected().status();
    }

    private SandboxRuntime selected() {
        final SandboxRuntime chosen;
        if (new Unchecked<>(this.available).value()) {
            chosen = this.preferred;
        } else {
            chosen = this.fallback;
        }
        return chosen;
    }
}
