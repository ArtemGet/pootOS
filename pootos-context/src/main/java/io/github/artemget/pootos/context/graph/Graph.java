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
import java.util.Collection;

/**
 * An immutable context graph of content-addressed nodes and typed edges.
 *
 * @since 0.0.1
 */
public interface Graph {

    /**
     * A new graph that also contains the given node.
     *
     * @param node Node to include
     * @return New graph including the node
     */
    Graph with(Node node);

    /**
     * A new graph that also contains the given edge.
     *
     * @param edge Edge to include
     * @return New graph including the edge
     */
    Graph with(Edge edge);

    /**
     * Nodes reachable from the seed within the given number of hops.
     *
     * <p>Traversal follows edges in both directions, so an edge connects its
     * endpoints for the purpose of a slice. The seed itself is included when
     * the graph contains it; a depth of zero yields only the seed. Nodes whose
     * id is unknown to the graph are never returned.</p>
     *
     * @param seed Node to start from
     * @param depth Maximum number of hops
     * @return Reachable nodes, seed included
     */
    Collection<Node> slice(NodeId seed, int depth);
}
