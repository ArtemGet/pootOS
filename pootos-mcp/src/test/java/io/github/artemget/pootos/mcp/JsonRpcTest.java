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
import org.cactoos.iterable.Mapped;
import org.cactoos.text.TextOf;
import org.cactoos.text.UncheckedText;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Test case for {@link JsonRpc}.
 *
 * @since 0.0.1
 */
final class JsonRpcTest {

    @Test
    void buildsInitializeFrame() {
        MatcherAssert.assertThat(
            "The initialize frame must be deterministic",
            new UncheckedText(JsonRpc.initialize()).asString(),
            Matchers.equalTo(
                "{\"jsonrpc\":\"2.0\",\"id\":1,\"method\":\"initialize\","
                    .concat("\"params\":{\"protocolVersion\":\"2024-11-05\",")
                    .concat("\"capabilities\":{},\"clientInfo\":")
                    .concat("{\"name\":\"pootos\",\"version\":\"0.0.1\"}}}")
            )
        );
    }

    @Test
    void buildsListToolsFrame() {
        MatcherAssert.assertThat(
            "The tools/list frame must be deterministic",
            new UncheckedText(JsonRpc.listTools()).asString(),
            Matchers.equalTo(
                "{\"jsonrpc\":\"2.0\",\"id\":2,\"method\":\"tools/list\","
                    .concat("\"params\":{}}")
            )
        );
    }

    @Test
    void listsToolsThroughFakeTransport() throws McpException {
        MatcherAssert.assertThat(
            "A client must list tools through its transport",
            new Mapped<String>(
                text -> new UncheckedText(text).asString(),
                new JsonRpc(JsonRpcTest.server(), JsonRpcTest.transport()).tools()
            ),
            Matchers.contains("fs_read", "fs_write")
        );
    }

    @Test
    void rejectsReplyWithoutTools() {
        MatcherAssert.assertThat(
            "A reply without a tools array must be rejected",
            Assertions.assertThrows(
                McpException.class,
                () -> new JsonRpc(
                    JsonRpcTest.server(),
                    request -> new TextOf("{}")
                ).tools()
            ).getMessage(),
            Matchers.containsString("fs")
        );
    }

    @Test
    void printsItsServer() {
        MatcherAssert.assertThat(
            "The client must print its server name",
            new JsonRpc(JsonRpcTest.server(), JsonRpcTest.transport()).toString(),
            Matchers.equalTo("JsonRpc[fs]")
        );
    }

    private static McpServer server() {
        return new McpServer("fs", new TextOf("npx -y server"));
    }

    private static McpTransport transport() {
        return request -> JsonRpcTest.reply();
    }

    private static Text reply() {
        return new TextOf(
            "{\"tools\":[{\"name\":\"fs_read\",\"description\":\"r\"},"
                .concat("{\"name\":\"fs_write\",\"description\":\"w\"}]}")
        );
    }
}
