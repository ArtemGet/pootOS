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

import io.github.artemget.pootos.mcp.JsonRpc;
import io.github.artemget.pootos.mcp.McpException;
import io.github.artemget.pootos.mcp.McpServer;
import io.github.artemget.pootos.system.config.ConfigException;
import java.util.Collection;
import org.cactoos.Text;

/**
 * The MCP facade: the configured servers and the tools each exposes.
 *
 * <p>Tool listing is delegated to the injected {@link McpTransports}, so a
 * real run reaches the server over its configured transport while a test
 * supplies a fake one. This object never starts a process itself; the
 * transport owns the bounded timeout.</p>
 *
 * @since 0.0.1
 */
public final class ManagedMcp implements McpCatalog, McpTools {

    /**
     * Configured servers.
     */
    private final McpCatalog catalog;

    /**
     * Transports reaching the servers.
     */
    private final McpTransports transports;

    /**
     * Ctor.
     *
     * @param catalog Configured servers
     * @param transports Transports reaching the servers
     */
    public ManagedMcp(final McpCatalog catalog, final McpTransports transports) {
        this.catalog = catalog;
        this.transports = transports;
    }

    @Override
    public Collection<McpServer> servers() throws ConfigException {
        return this.catalog.servers();
    }

    @Override
    public Collection<Text> tools(final McpServer server) throws McpException {
        return new JsonRpc(server, this.transports.transport(server)).tools();
    }
}
