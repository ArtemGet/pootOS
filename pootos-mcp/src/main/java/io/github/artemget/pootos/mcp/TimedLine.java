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

package io.github.artemget.pootos.mcp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicReference;
import org.cactoos.Scalar;

/**
 * One line read from a stream within a bounded time.
 *
 * <p>The read runs on a virtual thread and is awaited for at most the given
 * duration, so a silent server cannot hang the caller. A missing line, an I/O
 * failure and a timeout are all reported as {@link McpException}; the worker
 * thread is daemon, so it never keeps the JVM alive.</p>
 *
 * @since 0.0.1
 */
public final class TimedLine implements Scalar<String> {

    /**
     * Stream to read the line from.
     */
    private final InputStream stream;

    /**
     * Maximum time to wait for the line.
     */
    private final Duration timeout;

    /**
     * Ctor.
     *
     * @param stream Stream to read the line from
     * @param timeout Maximum time to wait for the line
     */
    public TimedLine(final InputStream stream, final Duration timeout) {
        this.stream = stream;
        this.timeout = timeout;
    }

    @Override
    public String value() throws McpException {
        final AtomicReference<String> line = new AtomicReference<>();
        final AtomicReference<IOException> failure = new AtomicReference<>();
        final Thread worker = Thread.ofVirtual().unstarted(
            () -> {
                try {
                    line.set(
                        new BufferedReader(
                            new InputStreamReader(
                                this.stream, StandardCharsets.UTF_8
                            )
                        ).readLine()
                    );
                } catch (final IOException ex) {
                    failure.set(ex);
                }
            }
        );
        worker.start();
        try {
            worker.join(this.timeout.toMillis());
        } catch (final InterruptedException ex) {
            Thread.currentThread().interrupt();
            worker.interrupt();
            throw new McpException(
                "Interrupted while awaiting the MCP server", ex
            );
        }
        if (worker.isAlive()) {
            worker.interrupt();
            throw new McpException(
                "MCP server did not reply within %s".formatted(this.timeout)
            );
        }
        if (failure.get() != null) {
            throw new McpException(
                "Cannot read the MCP server reply", failure.get()
            );
        }
        if (line.get() == null) {
            throw new McpException("MCP server returned no reply");
        }
        return line.get();
    }
}
