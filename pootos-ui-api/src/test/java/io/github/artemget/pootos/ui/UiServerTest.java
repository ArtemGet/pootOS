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

import io.github.artemget.pootos.context.node.Node;
import io.github.artemget.pootos.context.node.TaskNode;
import io.github.artemget.pootos.context.store.GraphStore;
import io.github.artemget.pootos.context.store.SqliteGraphStore;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.time.Duration;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Test case for {@link UiServer}.
 *
 * @since 0.0.1
 */
final class UiServerTest {

    @Test
    void answersHealth(@TempDir final Path dir) throws Exception {
        try (Connection connection = DriverManager.getConnection(this.url(dir))) {
            try (
                UiRunning running = new UiServer(
                    "127.0.0.1", 0, new SqliteGraphStore(connection)
                ).start()
            ) {
                MatcherAssert.assertThat(
                    "Health endpoint must report ok",
                    this.get(running.port(), "/api/health"),
                    Matchers.containsString("\"status\":\"ok\"")
                );
            }
        }
    }

    @Test
    void rendersPersistedNode(@TempDir final Path dir) throws Exception {
        final Node node = new TaskNode("ship ui-api", "read-only REST", "open");
        try (Connection connection = DriverManager.getConnection(this.url(dir))) {
            final GraphStore store = new SqliteGraphStore(connection);
            store.persist(node);
            try (UiRunning running = new UiServer("127.0.0.1", 0, store).start()) {
                MatcherAssert.assertThat(
                    "Graph endpoint must expose the persisted node",
                    this.get(running.port(), "/api/graph"),
                    Matchers.containsString(node.id().asString())
                );
            }
        }
    }

    private String get(final int port, final String path) throws Exception {
        try (
            HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build()
        ) {
            return client.send(
                HttpRequest.newBuilder(
                    URI.create(
                        String.format("http://127.0.0.1:%d%s", port, path)
                    )
                ).timeout(Duration.ofSeconds(5)).GET().build(),
                HttpResponse.BodyHandlers.ofString()
            ).body();
        }
    }

    private String url(final Path dir) {
        return String.format("jdbc:sqlite:%s", dir.resolve("graph.db"));
    }
}
