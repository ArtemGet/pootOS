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

package io.github.artemget.pootos.context.node;

import org.cactoos.Text;

/**
 * Content-addressed identity of a node, a canonical hex string.
 *
 * @since 0.0.1
 */
public final class NodeId implements Text {

    /**
     * Canonical hex digest.
     */
    private final String hex;

    /**
     * Ctor.
     *
     * @param hex Canonical hex digest
     */
    public NodeId(final String hex) {
        this.hex = hex;
    }

    @Override
    public String asString() {
        return this.hex;
    }

    @Override
    public boolean equals(final Object obj) {
        return this == obj
            || obj instanceof NodeId node
            && this.hex.equals(node.hex);
    }

    @Override
    public int hashCode() {
        return this.hex.hashCode();
    }

    @Override
    public String toString() {
        return this.hex;
    }
}
