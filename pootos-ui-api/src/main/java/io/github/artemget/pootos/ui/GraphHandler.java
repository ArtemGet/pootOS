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

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import io.github.artemget.pootos.context.store.GraphStore;
import io.github.artemget.pootos.context.store.StoreException;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

/**
 * An {@link HttpHandler} that serves the context graph as JSON.
 *
 * <p>A failure to read the store is recovered once, at this boundary: the
 * response is a {@code 500} with a short error body instead of a stack trace,
 * so no internal detail leaks to the client.</p>
 *
 * @since 0.0.1
 */
public final class GraphHandler implements HttpHandler {

    /**
     * Store to read.
     */
    private final GraphStore store;

    /**
     * Ctor.
     *
     * @param store Store to read
     */
    public GraphHandler(final GraphStore store) {
        this.store = store;
    }

    @Override
    public void handle(final HttpExchange exchange) throws IOException {
        String body;
        int code;
        try {
            body = new GraphJson(this.store).asString();
            code = 200;
        } catch (final StoreException err) {
            body = "{\"error\":\"store unavailable\"}";
            code = 500;
        }
        final byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set(
            "Content-Type", "application/json; charset=utf-8"
        );
        exchange.sendResponseHeaders(code, bytes.length);
        try (OutputStream out = exchange.getResponseBody()) {
            out.write(bytes);
        }
    }
}
