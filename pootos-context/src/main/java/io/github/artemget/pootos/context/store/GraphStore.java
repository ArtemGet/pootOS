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
import io.github.artemget.pootos.context.graph.GraphView;
import io.github.artemget.pootos.context.node.Node;

/**
 * A durable store of content-addressed nodes and typed edges.
 *
 * <p>Writing is idempotent: persisting the same node or edge twice leaves a
 * single copy. The whole store is read back as a {@link GraphView}, so readers
 * traverse the persisted structure without reaching into a graph.</p>
 *
 * @since 0.0.1
 */
public interface GraphStore {

    /**
     * Persist a node by its content address.
     *
     * @param node Node to persist
     * @throws StoreException When the store cannot be written
     */
    void persist(Node node) throws StoreException;

    /**
     * Persist a typed edge.
     *
     * @param edge Edge to persist
     * @throws StoreException When the store cannot be written
     */
    void persist(Edge edge) throws StoreException;

    /**
     * Read the whole persisted graph.
     *
     * @return View of every stored node and edge
     * @throws StoreException When the store cannot be read
     */
    GraphView load() throws StoreException;
}
