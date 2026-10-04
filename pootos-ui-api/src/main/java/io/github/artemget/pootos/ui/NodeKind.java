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

import org.cactoos.Text;

/**
 * The kind of a node, derived from its canonical JSON.
 *
 * <p>The store is content-addressed and type-agnostic: a node read back is
 * represented by its id and JSON, not by the domain type that first wrote it
 * (see {@code StoredNode}). The UI still needs a kind, so it is inferred from
 * the discriminator keys the canonical JSON carries. An unrecognised shape
 * yields the generic {@code node} kind rather than failing.</p>
 *
 * @since 0.0.1
 */
public final class NodeKind implements Text {

    /**
     * Canonical JSON of a node.
     */
    private final String json;

    /**
     * Ctor.
     *
     * @param json Canonical JSON of a node
     */
    public NodeKind(final String json) {
        this.json = json;
    }

    @Override
    public String asString() {
        final String kind;
        if (this.json.contains("\"title\"")) {
            kind = "task";
        } else if (this.json.contains("\"choice\"")) {
            kind = "decision";
        } else if (this.json.contains("\"advice\"")) {
            kind = "lesson";
        } else {
            kind = "node";
        }
        return kind;
    }
}
