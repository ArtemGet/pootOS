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

import io.github.artemget.pootos.context.edge.Edge;
import io.github.artemget.pootos.context.edge.Relation;
import io.github.artemget.pootos.context.edge.TypedEdge;
import io.github.artemget.pootos.context.graph.GraphView;
import io.github.artemget.pootos.context.graph.MemoryGraph;
import io.github.artemget.pootos.context.node.Node;
import io.github.artemget.pootos.context.node.NodeId;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;

/**
 * A {@link GraphStore} backed by a SQLite database.
 *
 * <p>The store holds one {@link Connection}: a connection is a mutable handle
 * to an external durable resource, yet the field references it and is never
 * reassigned, so the object stays immutable at the field level. The connection
 * is owned by the caller and is deliberately not closed here.</p>
 *
 * <p>Writing is idempotent: a node row is keyed by its content address and an
 * existing row is replaced, while an edge row is keyed by its triple and a
 * repeat is ignored. The tables are created on demand, so a constructor stays
 * free of I/O.</p>
 *
 * @since 0.0.1
 */
public final class SqliteGraphStore implements GraphStore {

    /**
     * SQLite connection.
     */
    private final Connection connection;

    /**
     * Ctor.
     *
     * @param connection Open SQLite connection
     */
    public SqliteGraphStore(final Connection connection) {
        this.connection = connection;
    }

    @Override
    public void persist(final Node node) throws StoreException {
        try {
            this.schema();
            try (
                PreparedStatement stmt = this.connection.prepareStatement(
                    "INSERT OR REPLACE INTO nodes (id, json) VALUES (?, ?)"
                )
            ) {
                stmt.setString(1, node.id().asString());
                stmt.setString(2, node.json());
                stmt.executeUpdate();
            }
        } catch (final SQLException err) {
            throw new StoreException("Failed to persist node", err);
        }
    }

    @Override
    public void persist(final Edge edge) throws StoreException {
        try {
            this.schema();
            try (
                PreparedStatement stmt = this.connection.prepareStatement(
                    """
                    INSERT OR IGNORE INTO edges (from_id, relation, to_id)
                    VALUES (?, ?, ?)
                    """
                )
            ) {
                stmt.setString(1, edge.from().asString());
                stmt.setString(2, edge.relation().name());
                stmt.setString(3, edge.to().asString());
                stmt.executeUpdate();
            }
        } catch (final SQLException err) {
            throw new StoreException("Failed to persist edge", err);
        }
    }

    @Override
    public GraphView load() throws StoreException {
        try {
            this.schema();
            return new MemoryGraph(this.nodes(), this.edges());
        } catch (final SQLException err) {
            throw new StoreException("Failed to load graph", err);
        }
    }

    private void schema() throws SQLException {
        try (Statement statement = this.connection.createStatement()) {
            statement.execute(
                """
                CREATE TABLE IF NOT EXISTS nodes (
                    id TEXT PRIMARY KEY,
                    json TEXT NOT NULL
                )
                """
            );
            statement.execute(
                """
                CREATE TABLE IF NOT EXISTS edges (
                    from_id TEXT NOT NULL,
                    relation TEXT NOT NULL,
                    to_id TEXT NOT NULL,
                    PRIMARY KEY (from_id, relation, to_id)
                )
                """
            );
        }
    }

    private Collection<Node> nodes() throws SQLException {
        final Collection<Node> nodes = new ArrayList<>(0);
        try (
            Statement statement = this.connection.createStatement();
            ResultSet rows = statement.executeQuery("SELECT id, json FROM nodes")
        ) {
            while (rows.next()) {
                nodes.add(
                    new StoredNode(new NodeId(rows.getString(1)), rows.getString(2))
                );
            }
        }
        return nodes;
    }

    private Collection<Edge> edges() throws SQLException {
        final Collection<Edge> edges = new ArrayList<>(0);
        try (
            Statement statement = this.connection.createStatement();
            ResultSet rows = statement.executeQuery(
                "SELECT from_id, relation, to_id FROM edges"
            )
        ) {
            while (rows.next()) {
                edges.add(
                    new TypedEdge(
                        new NodeId(rows.getString(1)),
                        Relation.valueOf(rows.getString(2)),
                        new NodeId(rows.getString(3))
                    )
                );
            }
        }
        return edges;
    }
}
