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

package io.github.artemget.pootos.mcp;

import org.cactoos.Text;
import org.cactoos.text.UncheckedText;

/**
 * A configured MCP server: a name and a transport descriptor.
 *
 * <p>The descriptor is what a transport needs to reach the server — a stdio
 * command line or an HTTP URL — while the name is the server's identity, used
 * for equality and diagnostics. The descriptor is rendered as the object's
 * text; credentials never belong here, they are separate secret references.</p>
 *
 * @since 0.0.1
 */
public final class McpServer implements Text {

    /**
     * Server identity.
     */
    private final String name;

    /**
     * Transport descriptor: a stdio command line or an HTTP URL.
     */
    private final Text descriptor;

    /**
     * Ctor.
     *
     * @param name Server identity
     * @param descriptor Transport descriptor
     */
    public McpServer(final String name, final Text descriptor) {
        this.name = name;
        this.descriptor = descriptor;
    }

    @Override
    public String asString() {
        return new UncheckedText(this.descriptor).asString();
    }

    @Override
    public boolean equals(final Object obj) {
        return this == obj
            || obj instanceof McpServer server
            && this.name.equals(server.name);
    }

    @Override
    public int hashCode() {
        return this.name.hashCode();
    }

    @Override
    public String toString() {
        return this.name;
    }
}
