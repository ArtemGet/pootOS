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

import java.io.IOException;
import org.cactoos.Scalar;
import org.cactoos.Text;
import org.cactoos.text.TextOf;
import org.cactoos.text.UncheckedText;

/**
 * Probes whether a Docker runtime, such as {@code runsc}, is registered.
 *
 * <p>Probing is best-effort: a missing daemon or a failed probe means the
 * runtime is absent. The caller then selects the safe fallback, whose
 * degradation is surfaced by {@link ProbedRuntime} (ADR-001).</p>
 *
 * @since 0.0.1
 */
public final class DockerProbe implements Scalar<Boolean> {

    /**
     * Runtime name to look for.
     */
    private final Text runtime;

    /**
     * Ctor.
     *
     * @param runtime Runtime name to look for
     */
    public DockerProbe(final Text runtime) {
        this.runtime = runtime;
    }

    @Override
    public Boolean value() {
        boolean found = false;
        try {
            final Process process = new ProcessBuilder(
                "docker", "info", "--format", "{{.Runtimes}}"
            ).redirectErrorStream(true).start();
            final String output = new UncheckedText(
                new TextOf(process.getInputStream())
            ).asString();
            process.waitFor();
            found = output.contains(new UncheckedText(this.runtime).asString());
        } catch (final IOException ex) {
            found = false;
        } catch (final InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
        return found;
    }
}
