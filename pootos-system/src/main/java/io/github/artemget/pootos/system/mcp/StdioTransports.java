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

package io.github.artemget.pootos.system.mcp;

import io.github.artemget.pootos.mcp.McpServer;
import io.github.artemget.pootos.mcp.McpTransport;
import io.github.artemget.pootos.mcp.StdioCommand;
import io.github.artemget.pootos.mcp.StdioTransport;
import java.time.Duration;
import java.util.List;
import org.cactoos.Text;
import org.cactoos.list.ListOf;
import org.cactoos.text.Split;
import org.cactoos.text.TextOf;

/**
 * A {@link McpTransports} that spawns the configured stdio command.
 *
 * <p>The server's transport descriptor is a tokenized command line: the first
 * token is the program, the rest are its arguments. Every exchange is bounded
 * by the injected timeout, so a silent server cannot hang a caller
 * ({@link StdioTransport}).</p>
 *
 * @since 0.0.1
 */
public final class StdioTransports implements McpTransports {

    /**
     * Maximum time to wait for a reply.
     */
    private final Duration timeout;

    /**
     * Ctor.
     *
     * @param timeout Maximum time to wait for a reply
     */
    public StdioTransports(final Duration timeout) {
        this.timeout = timeout;
    }

    @Override
    public McpTransport transport(final McpServer server) {
        final List<Text> tokens = new ListOf<>(
            new Split(new TextOf(server.asString()), "\\s+")
        );
        return new StdioTransport(
            new StdioCommand(tokens.get(0), tokens.subList(1, tokens.size())),
            this.timeout
        );
    }
}
