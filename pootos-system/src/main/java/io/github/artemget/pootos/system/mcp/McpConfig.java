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
import io.github.artemget.pootos.system.config.ConfigFile;
import io.github.artemget.pootos.system.config.ConfigResolver;
import io.github.artemget.pootos.system.secret.SecretRef;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.cactoos.Text;
import org.cactoos.text.TextOf;
import org.cactoos.text.UncheckedText;

/**
 * MCP servers stored as plain {@code key=value} config data.
 *
 * <p>Every server is a triple of keys under the {@code mcp.} prefix — a
 * transport descriptor, an enabled flag and an optional secret reference. The
 * configuration file is re-read on every call, so a change written by the
 * System Agent is visible without a restart (ADR-006). Entries never hold a
 * secret value, only a {@link SecretRef} name.</p>
 *
 * @since 0.0.1
 */
public final class McpConfig implements McpCatalog, McpDefinitions {

    /**
     * Key prefix for every MCP server entry.
     */
    private static final String PREFIX = "mcp.";

    /**
     * Key suffix holding the transport descriptor.
     */
    private static final String COMMAND = ".command";

    /**
     * Key suffix holding the enabled flag.
     */
    private static final String ENABLED = ".enabled";

    /**
     * Enabled flag value.
     */
    private static final String ACTIVE = "true";

    /**
     * Configuration file holding the entries.
     */
    private final ConfigFile file;

    /**
     * Resolver reading individual values from the file.
     */
    private final ConfigResolver resolver;

    /**
     * Ctor.
     *
     * @param file Configuration file
     * @param resolver Resolver reading individual values
     */
    public McpConfig(final ConfigFile file, final ConfigResolver resolver) {
        this.file = file;
        this.resolver = resolver;
    }

    @Override
    public Collection<McpServer> servers() throws ConfigException {
        final Collection<McpServer> found = new ArrayList<>(0);
        for (final String line
            : new UncheckedText(this.file.read()).asString().lines().toList()) {
            final String[] parts = line.split("=", 2);
            if (parts.length == 2 && parts[0].trim().endsWith(McpConfig.COMMAND)) {
                final String key = parts[0].trim();
                final String name = McpConfig.named(key);
                if (this.active(name)) {
                    found.add(new McpServer(name, this.resolver.value(key)));
                }
            }
        }
        return found;
    }

    @Override
    public void define(final McpServer server) throws ConfigException {
        final String prefix = McpConfig.PREFIX.concat(server.toString());
        this.upsert(prefix.concat(McpConfig.COMMAND), server);
        this.upsert(prefix.concat(McpConfig.ENABLED), new TextOf(McpConfig.ACTIVE));
    }

    @Override
    public void define(final McpServer server, final SecretRef secret)
        throws ConfigException {
        this.define(server);
        this.upsert(
            McpConfig.PREFIX.concat(server.toString()).concat(".secret"),
            secret
        );
    }

    @Override
    public void enabled(final McpServer server, final boolean enabled)
        throws ConfigException {
        this.upsert(
            McpConfig.PREFIX.concat(server.toString()).concat(McpConfig.ENABLED),
            new TextOf(Boolean.toString(enabled))
        );
    }

    private boolean active(final String name) throws ConfigException {
        return McpConfig.ACTIVE.equals(
            new UncheckedText(
                this.resolver.value(
                    McpConfig.PREFIX.concat(name).concat(McpConfig.ENABLED)
                )
            ).asString()
        );
    }

    private static String named(final String key) {
        return key.substring(
            McpConfig.PREFIX.length(),
            key.length() - McpConfig.COMMAND.length()
        );
    }

    private void upsert(final String key, final Text value) throws ConfigException {
        final String rendered = key.concat("=")
            .concat(new UncheckedText(value).asString());
        final List<String> lines = new ArrayList<>(0);
        boolean found = false;
        for (final String line
            : new UncheckedText(this.file.read()).asString().lines().toList()) {
            final String[] parts = line.split("=", 2);
            if (parts.length == 2 && parts[0].trim().equals(key)) {
                lines.add(rendered);
                found = true;
            } else {
                lines.add(line);
            }
        }
        if (!found) {
            lines.add(rendered);
        }
        this.file.write(new TextOf(String.join(System.lineSeparator(), lines)));
    }
}
