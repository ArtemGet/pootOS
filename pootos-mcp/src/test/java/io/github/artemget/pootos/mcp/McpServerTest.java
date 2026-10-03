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

import org.cactoos.text.TextOf;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

/**
 * Test case for {@link McpServer}.
 *
 * @since 0.0.1
 */
final class McpServerTest {

    @Test
    void rendersDescriptorAsText() {
        MatcherAssert.assertThat(
            "A server must render its transport descriptor",
            new McpServer("fs", new TextOf("npx -y server")).asString(),
            Matchers.equalTo("npx -y server")
        );
    }

    @Test
    void identifiesByName() {
        MatcherAssert.assertThat(
            "Servers with the same name must be equal",
            new McpServer("fs", new TextOf("a"))
                .equals(new McpServer("fs", new TextOf("b"))),
            Matchers.is(true)
        );
    }

    @Test
    void differsByName() {
        MatcherAssert.assertThat(
            "Servers with different names must not be equal",
            new McpServer("fs", new TextOf("a"))
                .equals(new McpServer("http", new TextOf("a"))),
            Matchers.is(false)
        );
    }

    @Test
    void hashesByName() {
        MatcherAssert.assertThat(
            "Servers with the same name must share a hash",
            new McpServer("fs", new TextOf("a")).hashCode(),
            Matchers.equalTo(new McpServer("fs", new TextOf("b")).hashCode())
        );
    }

    @Test
    void printsItsName() {
        MatcherAssert.assertThat(
            "A server must print its name",
            new McpServer("fs", new TextOf("a")).toString(),
            Matchers.equalTo("fs")
        );
    }
}
