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

import java.util.ArrayList;
import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.cactoos.Text;
import org.cactoos.text.TextOf;
import org.cactoos.text.UncheckedText;

/**
 * A JSON-RPC 2.0 MCP client: builds the request frames and parses replies.
 *
 * <p>Framing is deterministic — a fixed request id per method — so the emitted
 * bytes can be asserted in a unit test without a server. Parsing is a minimal,
 * dependency-free extraction of the {@code name} of every entry of the
 * {@code tools} array; it is deliberately not a general JSON parser.</p>
 *
 * @since 0.0.1
 */
public final class JsonRpc implements McpClient {

    /**
     * Matches a {@code "name"} string value.
     */
    private static final Pattern NAME = Pattern.compile(
        "\"name\"\\s*:\\s*\"((?:\\\\.|[^\"\\\\])*)\""
    );

    /**
     * Server this client talks to.
     */
    private final McpServer server;

    /**
     * Transport carrying the frames.
     */
    private final McpTransport transport;

    /**
     * Ctor.
     *
     * @param server Server this client talks to
     * @param transport Transport carrying the frames
     */
    public JsonRpc(final McpServer server, final McpTransport transport) {
        this.server = server;
        this.transport = transport;
    }

    @Override
    public Collection<Text> tools() throws McpException {
        this.transport.send(JsonRpc.initialize());
        return this.parsed(this.transport.send(JsonRpc.listTools()));
    }

    @Override
    public String toString() {
        return "JsonRpc[%s]".formatted(this.server);
    }

    /**
     * The deterministic {@code initialize} frame.
     *
     * @return JSON-RPC request body
     */
    static Text initialize() {
        return new TextOf(
            "{\"jsonrpc\":\"2.0\",\"id\":1,\"method\":\"initialize\","
                .concat("\"params\":{\"protocolVersion\":\"2024-11-05\",")
                .concat("\"capabilities\":{},\"clientInfo\":")
                .concat("{\"name\":\"pootos\",\"version\":\"0.0.1\"}}}")
        );
    }

    /**
     * The deterministic {@code tools/list} frame.
     *
     * @return JSON-RPC request body
     */
    static Text listTools() {
        return new TextOf(
            "{\"jsonrpc\":\"2.0\",\"id\":2,\"method\":\"tools/list\","
                .concat("\"params\":{}}")
        );
    }

    private Collection<Text> parsed(final Text reply) throws McpException {
        final String json = new UncheckedText(reply).asString();
        final int start = json.indexOf("\"tools\"");
        if (start < 0) {
            throw new McpException(
                "Server %s returned no tools array".formatted(this.server)
            );
        }
        final Matcher matcher = JsonRpc.NAME.matcher(json.substring(start));
        final Collection<Text> names = new ArrayList<>(0);
        while (matcher.find()) {
            names.add(new TextOf(matcher.group(1)));
        }
        return names;
    }
}
