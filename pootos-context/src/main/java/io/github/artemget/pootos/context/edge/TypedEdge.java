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

package io.github.artemget.pootos.context.edge;

import io.github.artemget.pootos.context.node.NodeId;
import java.util.Objects;

/**
 * An immutable edge between two content-addressed nodes.
 *
 * @since 0.0.1
 */
public final class TypedEdge implements Edge {

    /**
     * Source node.
     */
    private final NodeId source;

    /**
     * Relation kind.
     */
    private final Relation kind;

    /**
     * Target node.
     */
    private final NodeId target;

    /**
     * Ctor.
     *
     * @param source Source node
     * @param kind Relation kind
     * @param target Target node
     */
    public TypedEdge(final NodeId source, final Relation kind, final NodeId target) {
        this.source = source;
        this.kind = kind;
        this.target = target;
    }

    @Override
    public NodeId from() {
        return this.source;
    }

    @Override
    public Relation relation() {
        return this.kind;
    }

    @Override
    public NodeId to() {
        return this.target;
    }

    @Override
    public boolean equals(final Object obj) {
        return this == obj
            || obj instanceof TypedEdge node
            && this.same(node);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.source, this.kind, this.target);
    }

    private boolean same(final TypedEdge other) {
        return this.source.equals(other.source)
            && this.kind.equals(other.kind)
            && this.target.equals(other.target);
    }
}
