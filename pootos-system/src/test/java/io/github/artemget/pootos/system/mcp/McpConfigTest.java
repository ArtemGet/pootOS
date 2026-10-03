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
import io.github.artemget.pootos.system.config.ConfigDirectory;
import io.github.artemget.pootos.system.config.ConfigException;
import io.github.artemget.pootos.system.config.ConfigFile;
import io.github.artemget.pootos.system.config.FileConfigResolver;
import io.github.artemget.pootos.system.secret.SecretRef;
import java.nio.file.Path;
import org.cactoos.iterable.Mapped;
import org.cactoos.text.TextOf;
import org.cactoos.text.UncheckedText;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Test case for {@link McpConfig}.
 *
 * @since 0.0.1
 */
final class McpConfigTest {

    @Test
    void definesServer(@TempDir final Path dir) throws Exception {
        final McpConfig config = McpConfigTest.config(dir);
        config.define(McpConfigTest.server());
        MatcherAssert.assertThat(
            "A defined server must be listed",
            new Mapped<String>(McpServer::toString, config.servers()),
            Matchers.contains("fs")
        );
    }

    @Test
    void disablesServer(@TempDir final Path dir) throws Exception {
        final McpConfig config = McpConfigTest.config(dir);
        config.define(McpConfigTest.server());
        config.enabled(McpConfigTest.server(), false);
        MatcherAssert.assertThat(
            "A disabled server must not be listed",
            config.servers(),
            Matchers.empty()
        );
    }

    @Test
    void enablesDisabledServer(@TempDir final Path dir) throws Exception {
        final McpConfig config = McpConfigTest.config(dir);
        config.define(McpConfigTest.server());
        config.enabled(McpConfigTest.server(), false);
        config.enabled(McpConfigTest.server(), true);
        MatcherAssert.assertThat(
            "A re-enabled server must be listed again",
            config.servers(),
            Matchers.hasSize(1)
        );
    }

    @Test
    void reflectsExternalChange(@TempDir final Path dir) throws Exception {
        final ConfigFile file = new ConfigDirectory(dir).file("mcp.conf");
        file.write(new TextOf(""));
        final McpConfig config = new McpConfig(file, new FileConfigResolver(file));
        config.define(McpConfigTest.server());
        config.enabled(McpConfigTest.server(), false);
        file.write(
            new TextOf(
                "mcp.fs.command=npx -y server".concat(
                    System.lineSeparator()
                ).concat("mcp.fs.enabled=true")
            )
        );
        MatcherAssert.assertThat(
            "A change written by the System Agent must be visible without a restart",
            config.servers(),
            Matchers.hasSize(1)
        );
    }

    @Test
    void storesSecretReferenceName(@TempDir final Path dir) throws Exception {
        final ConfigFile file = new ConfigDirectory(dir).file("mcp.conf");
        file.write(new TextOf(""));
        new McpConfig(file, new FileConfigResolver(file))
            .define(McpConfigTest.server(), new SecretRef("env", "FS_TOKEN"));
        MatcherAssert.assertThat(
            "A secret must be stored as a reference name, never a value",
            new UncheckedText(file.read()).asString(),
            Matchers.containsString("mcp.fs.secret=env:FS_TOKEN")
        );
    }

    private static McpConfig config(final Path dir) throws ConfigException {
        final ConfigFile file = new ConfigDirectory(dir).file("mcp.conf");
        file.write(new TextOf(""));
        return new McpConfig(file, new FileConfigResolver(file));
    }

    private static McpServer server() {
        return new McpServer("fs", new TextOf("npx -y server"));
    }
}
