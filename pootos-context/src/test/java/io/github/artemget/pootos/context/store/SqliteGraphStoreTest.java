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

package io.github.artemget.pootos.context.store;

import io.github.artemget.pootos.context.edge.Relation;
import io.github.artemget.pootos.context.edge.TypedEdge;
import io.github.artemget.pootos.context.node.LessonNode;
import io.github.artemget.pootos.context.node.Node;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Test case for {@link SqliteGraphStore}.
 *
 * @since 0.0.1
 */
final class SqliteGraphStoreTest {

    @Test
    void restoresPersistedNode(@TempDir final Path dir) throws Exception {
        final Node node = new LessonNode("boom", "fix it", 0.5);
        try (Connection connection = DriverManager.getConnection(this.database(dir))) {
            final GraphStore store = new SqliteGraphStore(connection);
            store.persist(node);
            MatcherAssert.assertThat(
                "A persisted node must be restored from the store",
                store.load().slice(node.id(), 0),
                Matchers.contains(new StoredNode(node.id(), node.json()))
            );
        }
    }

    @Test
    void keepsOneRowPerContentAddress(@TempDir final Path dir) throws Exception {
        final Node node = new LessonNode("boom", "fix it", 0.5);
        try (Connection connection = DriverManager.getConnection(this.database(dir))) {
            final GraphStore store = new SqliteGraphStore(connection);
            store.persist(node);
            store.persist(node);
        }
        MatcherAssert.assertThat(
            "Persisting the same content address twice keeps one row",
            this.count(this.database(dir)),
            Matchers.is(1)
        );
    }

    @Test
    void restoresPersistedEdge(@TempDir final Path dir) throws Exception {
        final Node source = new LessonNode("a", "first", 0.1);
        final Node target = new LessonNode("b", "second", 0.2);
        try (Connection connection = DriverManager.getConnection(this.database(dir))) {
            final GraphStore store = new SqliteGraphStore(connection);
            store.persist(source);
            store.persist(target);
            store.persist(new TypedEdge(source.id(), Relation.PRODUCES, target.id()));
            MatcherAssert.assertThat(
                "An edge must be restored so its endpoints connect",
                store.load().slice(source.id(), 1),
                Matchers.hasItem(new StoredNode(target.id(), target.json()))
            );
        }
    }

    @Test
    void survivesRestart(@TempDir final Path dir) throws Exception {
        final Node node = new LessonNode("boom", "fix it", 0.5);
        try (Connection connection = DriverManager.getConnection(this.database(dir))) {
            new SqliteGraphStore(connection).persist(node);
        }
        try (Connection connection = DriverManager.getConnection(this.database(dir))) {
            MatcherAssert.assertThat(
                "A second store over the same file must see earlier data",
                new SqliteGraphStore(connection).load().slice(node.id(), 0),
                Matchers.contains(new StoredNode(node.id(), node.json()))
            );
        }
    }

    private String database(final Path dir) {
        return String.format("jdbc:sqlite:%s", dir.resolve("graph.db"));
    }

    private int count(final String url) throws SQLException {
        int total = 0;
        try (
            Connection connection = DriverManager.getConnection(url);
            Statement statement = connection.createStatement();
            ResultSet found = statement.executeQuery("SELECT COUNT(*) FROM nodes")
        ) {
            while (found.next()) {
                total = found.getInt(1);
            }
        }
        return total;
    }
}
