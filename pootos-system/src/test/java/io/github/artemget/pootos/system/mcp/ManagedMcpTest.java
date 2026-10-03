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

package io.github.artemget.pootos.system.mcp;

import io.github.artemget.pootos.mcp.McpServer;
import io.github.artemget.pootos.mcp.McpTransport;
import org.cactoos.iterable.Mapped;
import org.cactoos.list.ListOf;
import org.cactoos.text.TextOf;
import org.cactoos.text.UncheckedText;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

/**
 * Test case for {@link ManagedMcp}.
 *
 * <p>Tool listing is exercised through a fake transport, so no process is
 * started and the case cannot hang.</p>
 *
 * @since 0.0.1
 */
final class ManagedMcpTest {

    @Test
    void exposesConfiguredServers() throws Exception {
        MatcherAssert.assertThat(
            "The facade must expose the configured servers",
            new Mapped<String>(
                McpServer::toString,
                new ManagedMcp(
                    () -> new ListOf<>(ManagedMcpTest.server()),
                    ignored -> ManagedMcpTest.transport()
                ).servers()
            ),
            Matchers.contains("fs")
        );
    }

    @Test
    void listsToolsThroughFakeTransport() throws Exception {
        final McpServer server = ManagedMcpTest.server();
        MatcherAssert.assertThat(
            "A managed server must list its tools through the transport",
            new Mapped<String>(
                text -> new UncheckedText(text).asString(),
                new ManagedMcp(
                    () -> new ListOf<>(server),
                    ignored -> ManagedMcpTest.transport()
                ).tools(server)
            ),
            Matchers.contains("fs_read", "fs_write")
        );
    }

    private static McpServer server() {
        return new McpServer("fs", new TextOf("npx -y server"));
    }

    private static McpTransport transport() {
        return request -> new TextOf(
            "{\"tools\":[{\"name\":\"fs_read\"},{\"name\":\"fs_write\"}]}"
        );
    }
}
