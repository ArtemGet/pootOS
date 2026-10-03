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

package io.github.artemget.pootos.context.graph;

import io.github.artemget.pootos.context.edge.Edge;
import io.github.artemget.pootos.context.node.Node;
import io.github.artemget.pootos.context.node.NodeId;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * An immutable in-memory context graph.
 *
 * <p>Every mutating operation returns a new instance; the receiver is never
 * changed. Nodes are content-addressed: adding a node whose id already exists
 * replaces the previous one, so at most one node per {@link NodeId} is kept.</p>
 *
 * @since 0.0.1
 */
public final class MemoryGraph implements Graph {

    /**
     * Content-addressed nodes.
     */
    private final Collection<Node> nodes;

    /**
     * Typed edges.
     */
    private final Collection<Edge> edges;

    /**
     * An empty graph.
     */
    public MemoryGraph() {
        this(List.of(), List.of());
    }

    /**
     * Ctor.
     *
     * @param nodes Content-addressed nodes
     * @param edges Typed edges
     */
    public MemoryGraph(final Collection<Node> nodes, final Collection<Edge> edges) {
        this.nodes = nodes;
        this.edges = edges;
    }

    @Override
    public Graph with(final Node node) {
        final Map<NodeId, Node> merged = new LinkedHashMap<>();
        for (final Node existing : this.nodes) {
            merged.put(existing.id(), existing);
        }
        merged.put(node.id(), node);
        return new MemoryGraph(merged.values(), this.edges);
    }

    @Override
    public Graph with(final Edge edge) {
        final Collection<Edge> merged = new ArrayList<>(this.edges);
        merged.add(edge);
        return new MemoryGraph(this.nodes, merged);
    }

    @Override
    public Collection<Node> slice(final NodeId seed, final int depth) {
        final Map<NodeId, Node> index = new LinkedHashMap<>();
        for (final Node node : this.nodes) {
            index.put(node.id(), node);
        }
        final Map<NodeId, Set<NodeId>> links = new HashMap<>();
        for (final Edge edge : this.edges) {
            links.computeIfAbsent(edge.from(), key -> new HashSet<>()).add(edge.to());
            links.computeIfAbsent(edge.to(), key -> new HashSet<>()).add(edge.from());
        }
        final Set<NodeId> found = new LinkedHashSet<>();
        final Set<NodeId> known = new HashSet<>();
        Set<NodeId> frontier = new LinkedHashSet<>();
        frontier.add(seed);
        for (int step = 0; step <= depth; step += 1) {
            final Set<NodeId> next = new LinkedHashSet<>();
            for (final NodeId current : frontier) {
                if (known.add(current) && index.containsKey(current)) {
                    found.add(current);
                    next.addAll(links.getOrDefault(current, Set.of()));
                }
            }
            frontier = next;
        }
        final Collection<Node> result = new LinkedHashSet<>();
        for (final NodeId id : found) {
            result.add(index.get(id));
        }
        return result;
    }
}
