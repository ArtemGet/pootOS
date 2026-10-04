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

/**
 * A read-only view over a context graph.
 *
 * <p>{@link #nodes()} and {@link #edges()} are not value getters: they hand a
 * consumer the whole structure so it can traverse and render a graph without
 * knowing how the graph is stored. Exposing an {@link Iterable} for traversal
 * is behavior, not "tell, don't ask" access to an internal field. Keeping these
 * two methods on a dedicated interface leaves {@link Graph} a small
 * mutation-and-traversal contract.</p>
 *
 * @since 0.0.1
 */
public interface GraphView {

    /**
     * Every node in this graph, for traversal.
     *
     * @return Nodes to walk
     */
    Iterable<Node> nodes();

    /**
     * Every edge in this graph, for traversal.
     *
     * @return Edges to walk
     */
    Iterable<Edge> edges();
}
