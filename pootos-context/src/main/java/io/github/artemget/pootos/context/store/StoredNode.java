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

import io.github.artemget.pootos.context.node.Node;
import io.github.artemget.pootos.context.node.NodeId;
import java.util.Objects;

/**
 * A node read back from a store, keeping its stored id and canonical JSON.
 *
 * <p>A store is agnostic of the concrete node kind, so a node restored from
 * disk is represented by its content address and JSON rather than by the
 * domain type that first wrote it. Rebuilding the domain type would need
 * reflection, which the codebase forbids.</p>
 *
 * @since 0.0.1
 */
public final class StoredNode implements Node {

    /**
     * Stored content address.
     */
    private final NodeId identity;

    /**
     * Stored canonical JSON.
     */
    private final String content;

    /**
     * Ctor.
     *
     * @param identity Stored content address
     * @param content Stored canonical JSON
     */
    public StoredNode(final NodeId identity, final String content) {
        this.identity = identity;
        this.content = content;
    }

    @Override
    public NodeId id() {
        return this.identity;
    }

    @Override
    public String json() {
        return this.content;
    }

    @Override
    public boolean equals(final Object obj) {
        return this == obj
            || obj instanceof StoredNode node
            && this.identity.equals(node.identity)
            && this.content.equals(node.content);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.identity, this.content);
    }
}
