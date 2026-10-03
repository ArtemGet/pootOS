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

package io.github.artemget.pootos.ui;

import com.sun.net.httpserver.HttpServer;
import io.github.artemget.pootos.context.store.GraphStore;
import java.io.IOException;
import java.net.InetSocketAddress;
import org.cactoos.text.TextOf;

/**
 * A read-only REST server built on the JDK {@link HttpServer}.
 *
 * <p>Binding happens in {@link #start()}, never in the constructor, so the
 * object stays cheap to create and free of I/O. The server exposes four
 * read-only routes under {@code /api}: health, the context graph, agents and
 * resources. Writing, live updates and authentication are out of scope.</p>
 *
 * @since 0.0.1
 */
public final class UiServer implements UiApi {

    /**
     * Host to bind.
     */
    private final String host;

    /**
     * Port to bind; zero asks the OS for an ephemeral port.
     */
    private final int port;

    /**
     * Context graph to serve.
     */
    private final GraphStore store;

    /**
     * Ctor.
     *
     * @param host Host to bind
     * @param port Port to bind
     * @param store Context graph to serve
     */
    public UiServer(final String host, final int port, final GraphStore store) {
        this.host = host;
        this.port = port;
        this.store = store;
    }

    @Override
    public UiRunning start() throws IOException {
        final HttpServer server = HttpServer.create(
            new InetSocketAddress(this.host, this.port), 0
        );
        server.createContext(
            "/api/health",
            new ExactPath(
                "/api/health",
                new JsonHandler(new TextOf("{\"status\":\"ok\"}"))
            )
        );
        server.createContext(
            "/api/agents",
            new ExactPath(
                "/api/agents",
                new JsonHandler(new TextOf("{\"agents\":[]}"))
            )
        );
        server.createContext(
            "/api/resources",
            new ExactPath(
                "/api/resources",
                new JsonHandler(new TextOf("{\"leases\":[]}"))
            )
        );
        server.createContext(
            "/api/graph",
            new ExactPath("/api/graph", new GraphHandler(this.store))
        );
        server.start();
        return new BoundServer(server);
    }
}
