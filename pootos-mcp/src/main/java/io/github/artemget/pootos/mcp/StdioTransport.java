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

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import org.cactoos.Text;
import org.cactoos.list.ListOf;
import org.cactoos.text.TextOf;
import org.cactoos.text.UncheckedText;

/**
 * A {@link McpTransport} that exchanges one frame with a child process.
 *
 * <p>The server is started from a {@link StdioCommand}, the request is written
 * to its standard input as a newline-terminated JSON-RPC frame, and a single
 * response line is read from its standard output within a bounded time. The
 * process is always destroyed afterwards — on success, failure and timeout
 * alike — so no server is left running (AGENTS.md §3). This transport performs
 * one exchange per process; a persistent session is out of scope.</p>
 *
 * @since 0.0.1
 */
public final class StdioTransport implements McpTransport {

    /**
     * Command that starts the server.
     */
    private final StdioCommand command;

    /**
     * Maximum time to wait for a reply.
     */
    private final Duration timeout;

    /**
     * Ctor.
     *
     * @param command Command that starts the server
     * @param timeout Maximum time to wait for a reply
     */
    public StdioTransport(final StdioCommand command, final Duration timeout) {
        this.command = command;
        this.timeout = timeout;
    }

    @Override
    public Text send(final Text request) throws McpException {
        final Process process;
        try {
            process = new ProcessBuilder(new ListOf<>(this.command)).start();
        } catch (final IOException ex) {
            throw new McpException("Cannot start the MCP server", ex);
        }
        try {
            return new TextOf(this.exchange(process, request));
        } finally {
            process.destroy();
            process.destroyForcibly();
        }
    }

    private String exchange(final Process process, final Text request)
        throws McpException {
        try (OutputStream stdin = process.getOutputStream()) {
            stdin.write(
                new UncheckedText(request).asString()
                    .concat(System.lineSeparator())
                    .getBytes(StandardCharsets.UTF_8)
            );
            stdin.flush();
        } catch (final IOException ex) {
            throw new McpException("Cannot write to the MCP server", ex);
        }
        return new TimedLine(process.getInputStream(), this.timeout).value();
    }
}
