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
import io.github.artemget.pootos.system.config.ConfigException;
import io.github.artemget.pootos.system.secret.SecretRef;

/**
 * A writable MCP server configuration.
 *
 * <p>The System Agent writes plain data only: a server's name, transport
 * descriptor and enabled flag, plus a {@link SecretRef} naming the secret a
 * human must supply — never a secret value (ADR-006).</p>
 *
 * @since 0.0.1
 */
public interface McpDefinitions {

    /**
     * Define a server that needs no authentication.
     *
     * @param server Server to define
     * @throws ConfigException When the configuration cannot be written
     */
    void define(McpServer server) throws ConfigException;

    /**
     * Define a server that needs a secret.
     *
     * @param server Server to define
     * @param secret Secret reference: a name, never a value
     * @throws ConfigException When the configuration cannot be written
     */
    void define(McpServer server, SecretRef secret) throws ConfigException;

    /**
     * Enable or disable a server.
     *
     * @param server Server to toggle
     * @param enabled Whether the server must be enabled
     * @throws ConfigException When the configuration cannot be written
     */
    void enabled(McpServer server, boolean enabled) throws ConfigException;
}
