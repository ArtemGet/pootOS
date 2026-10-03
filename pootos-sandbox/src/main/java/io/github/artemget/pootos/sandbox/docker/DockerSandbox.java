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

import io.github.artemget.pootos.sandbox.Sandbox;
import io.github.artemget.pootos.sandbox.Shell;
import java.io.IOException;
import java.io.UncheckedIOException;
import org.cactoos.Text;
import org.cactoos.list.ListOf;
import org.cactoos.text.TextOf;
import org.cactoos.text.UncheckedText;

/**
 * A {@link Sandbox} that runs its shell in a hardened Docker container.
 *
 * <p>The container is ephemeral ({@code --rm}), read-only and non-root,
 * with no host mounts and no network by default (ADR-001). The runtime in
 * use is exposed through {@link SandboxRuntime#status()}, so a gVisor
 * fallback is visible rather than silent.</p>
 *
 * @since 0.0.1
 */
public final class DockerSandbox implements Sandbox, SandboxRuntime {

    /**
     * Image the container is created from.
     */
    private final Text image;

    /**
     * Isolation runtime in use.
     */
    private final SandboxRuntime runtime;

    /**
     * Memory limit.
     */
    private final Text memory;

    /**
     * Process limit.
     */
    private final Text pids;

    /**
     * CPU limit.
     */
    private final Text cpus;

    /**
     * Explicit host mounts.
     */
    private final Iterable<Text> mounts;

    /**
     * Ctor.
     *
     * @param image Image the container is created from
     * @param runtime Isolation runtime in use
     * @param memory Memory limit
     * @param pids Process limit
     * @param cpus CPU limit
     * @param mounts Explicit host mounts
     */
    public DockerSandbox(
        final Text image,
        final SandboxRuntime runtime,
        final Text memory,
        final Text pids,
        final Text cpus,
        final Iterable<Text> mounts
    ) {
        this.image = image;
        this.runtime = runtime;
        this.memory = memory;
        this.pids = pids;
        this.cpus = cpus;
        this.mounts = mounts;
    }

    @Override
    public Shell shell() {
        return this::run;
    }

    @Override
    public void close() {
        // Containers are created with --rm, so nothing is left to release.
    }

    @Override
    public Text option() {
        return this.runtime.option();
    }

    @Override
    public Text status() {
        return this.runtime.status();
    }

    private Text run(final Text command) {
        final ProcessBuilder builder = new ProcessBuilder(
            new ListOf<>(
                new DockerCommand(
                    this.image, this.runtime.option(), this.memory,
                    this.pids, this.cpus, this.mounts, command
                )
            )
        );
        try {
            final Process process = builder.redirectErrorStream(true).start();
            process.getOutputStream().close();
            final String output = new UncheckedText(
                new TextOf(process.getInputStream())
            ).asString();
            final int code = process.waitFor();
            if (code != 0) {
                throw new IllegalStateException(
                    "Sandbox command exited with code %d: %s"
                        .formatted(code, output)
                );
            }
            return new TextOf(output);
        } catch (final IOException ex) {
            throw new UncheckedIOException(
                "Cannot run the sandbox container", ex
            );
        } catch (final InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(
                "Interrupted while running the sandbox container", ex
            );
        }
    }
}
