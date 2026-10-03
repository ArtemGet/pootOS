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

package io.github.artemget.pootos.providers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicReference;
import org.cactoos.text.TextOf;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Test case for {@link OpenAiCompatible}.
 *
 * @since 0.0.1
 */
final class OpenAiCompatibleTest {

    @Test
    void extractsAssistantText() throws Exception {
        final HttpServer server = OpenAiCompatibleTest.server(
            OpenAiCompatibleTest::respond
        );
        try {
            MatcherAssert.assertThat(
                "Adapter must return the assistant content",
                OpenAiCompatibleTest.provider(server, "sk-test")
                    .complete(new TextOf("ping")).asString(),
                Matchers.equalTo("pong")
            );
        } finally {
            server.stop(0);
        }
    }

    @Test
    void sendsBearerKey() throws Exception {
        final AtomicReference<String> header = new AtomicReference<>("");
        final HttpServer server = OpenAiCompatibleTest.server(
            exchange -> {
                header.set(exchange.getRequestHeaders().getFirst("Authorization"));
                OpenAiCompatibleTest.respond(exchange);
            }
        );
        try {
            OpenAiCompatibleTest.provider(server, "sk-secret")
                .complete(new TextOf("ping")).asString();
            MatcherAssert.assertThat(
                "Request must carry the injected bearer key",
                header.get(),
                Matchers.equalTo("Bearer sk-secret")
            );
        } finally {
            server.stop(0);
        }
    }

    @Test
    void sendsModelName() throws Exception {
        final AtomicReference<String> payload = new AtomicReference<>("");
        final HttpServer server = OpenAiCompatibleTest.server(
            exchange -> {
                payload.set(
                    new String(
                        exchange.getRequestBody().readAllBytes(),
                        StandardCharsets.UTF_8
                    )
                );
                OpenAiCompatibleTest.respond(exchange);
            }
        );
        try {
            OpenAiCompatibleTest.provider(server, "sk-test")
                .complete(new TextOf("ping")).asString();
            MatcherAssert.assertThat(
                "Request body must name the configured model",
                payload.get(),
                Matchers.containsString("\"model\":\"gpt-test\"")
            );
        } finally {
            server.stop(0);
        }
    }

    @Test
    void failsWhenEndpointUnreachable() throws Exception {
        final HttpServer server = OpenAiCompatibleTest.server(
            OpenAiCompatibleTest::respond
        );
        final URI endpoint = OpenAiCompatibleTest.endpoint(server);
        server.stop(0);
        MatcherAssert.assertThat(
            "An unreachable endpoint must raise a provider failure",
            Assertions.assertThrows(
                ProviderException.class,
                () -> new OpenAiCompatible(
                    endpoint,
                    new TextOf("openai"),
                    new TextOf("gpt-test"),
                    () -> new TextOf("sk-test"),
                    HttpClient.newHttpClient()
                ).complete(new TextOf("ping"))
            ),
            Matchers.instanceOf(ProviderException.class)
        );
    }

    private static ModelProvider provider(final HttpServer server, final String key) {
        return new OpenAiCompatible(
            OpenAiCompatibleTest.endpoint(server),
            new TextOf("openai"),
            new TextOf("gpt-test"),
            () -> new TextOf(key),
            HttpClient.newHttpClient()
        );
    }

    private static HttpServer server(final HttpHandler handler)
        throws IOException {
        final HttpServer server = HttpServer.create(new InetSocketAddress(0), 0);
        server.createContext("/", handler);
        server.start();
        return server;
    }

    private static URI endpoint(final HttpServer server) {
        return URI.create(
            String.format(
                "http://localhost:%d/v1/chat/completions",
                server.getAddress().getPort()
            )
        );
    }

    private static void respond(final HttpExchange exchange) throws IOException {
        final byte[] body =
            "{\"choices\":[{\"message\":{\"content\":\"pong\"}}]}"
                .getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(200, body.length);
        try (OutputStream stream = exchange.getResponseBody()) {
            stream.write(body);
        }
    }
}
