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
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

/**
 * An {@link HttpHandler} that delegates only for one exact request path.
 *
 * <p>The JDK {@link com.sun.net.httpserver.HttpServer} selects a context by
 * prefix, so a handler bound to {@code /api/graph} would also answer
 * {@code /api/graphfoo}. This decorator compares the request path with the one
 * it owns and answers any other path with {@code 404}, so a sibling prefix is
 * never served.</p>
 *
 * @since 0.0.1
 */
public final class ExactPath implements HttpHandler {

    /**
     * Path this handler owns.
     */
    private final String owned;

    /**
     * Handler to delegate to on an exact match.
     */
    private final HttpHandler origin;

    /**
     * Ctor.
     *
     * @param owned Path to serve exactly
     * @param origin Handler to delegate to on a match
     */
    public ExactPath(final String owned, final HttpHandler origin) {
        this.owned = owned;
        this.origin = origin;
    }

    @Override
    public void handle(final HttpExchange exchange) throws IOException {
        if (this.owned.equals(exchange.getRequestURI().getPath())) {
            this.origin.handle(exchange);
        } else {
            final byte[] body = "{\"error\":\"not found\"}"
                .getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set(
                "Content-Type", "application/json; charset=utf-8"
            );
            exchange.sendResponseHeaders(404, body.length);
            try (OutputStream out = exchange.getResponseBody()) {
                out.write(body);
            }
        }
    }
}
