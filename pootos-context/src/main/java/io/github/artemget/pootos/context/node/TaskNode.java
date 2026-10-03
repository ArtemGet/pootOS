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

import java.util.Objects;

/**
 * A first-class unit of work of the context graph.
 *
 * @since 0.0.1
 */
public final class TaskNode implements Node {

    /**
     * Short task title.
     */
    private final String title;

    /**
     * Task description.
     */
    private final String description;

    /**
     * Task status.
     */
    private final String status;

    /**
     * Ctor.
     *
     * @param title Short task title
     * @param description Task description
     * @param status Task status
     */
    public TaskNode(final String title, final String description, final String status) {
        this.title = title;
        this.description = description;
        this.status = status;
    }

    @Override
    public NodeId id() {
        return new HashedNode(this).id();
    }

    @Override
    public String json() {
        return "{\"description\":\"%s\",\"status\":\"%s\",\"title\":\"%s\"}".formatted(
            new EscapedText(this.description).asString(),
            new EscapedText(this.status).asString(),
            new EscapedText(this.title).asString()
        );
    }

    @Override
    public boolean equals(final Object obj) {
        return this == obj
            || obj instanceof TaskNode node
            && this.same(node);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.title, this.description, this.status);
    }

    private boolean same(final TaskNode other) {
        return this.title.equals(other.title)
            && this.description.equals(other.description)
            && this.status.equals(other.status);
    }
}
