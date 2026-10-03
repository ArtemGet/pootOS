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

import io.github.artemget.pootos.mcp.McpException;
import io.github.artemget.pootos.mcp.McpServer;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import org.cactoos.Text;
import org.cactoos.iterable.Mapped;
import org.cactoos.list.ListOf;
import org.cactoos.text.TextOf;
import org.cactoos.text.UncheckedText;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

/**
 * Test case for {@link StdioTransports}.
 *
 * <p>The real-process case spawns a throwaway Python echo server; it is skipped
 * by assumption when no interpreter is available, and every exchange is
 * bounded by a hard timeout so the suite never hangs.</p>
 *
 * @since 0.0.1
 */
final class StdioTransportsTest {

    @Test
    void rejectsMissingProgram() {
        MatcherAssert.assertThat(
            "A missing stdio program must be reported, not hang",
            Assertions.assertThrows(
                McpException.class,
                () -> new ManagedMcp(
                    () -> new ListOf<>(),
                    new StdioTransports(Duration.ofSeconds(2))
                ).tools(
                    new McpServer(
                        "fs", new TextOf("pootos-no-such-binary-xyz --root")
                    )
                )
            ).getMessage(),
            Matchers.containsString("Cannot start")
        );
    }

    @Test
    void exchangesWithRealServer() throws Exception {
        final Text python = StdioTransportsTest.interpreter();
        Assumptions.assumeTrue(
            !new UncheckedText(python).asString().isEmpty(),
            "Python is required to run the echo server"
        );
        final McpServer server = new McpServer(
            "fs",
            new TextOf(
                new UncheckedText(python).asString()
                    .concat(" ")
                    .concat(StdioTransportsTest.script().toString())
            )
        );
        MatcherAssert.assertThat(
            "A real stdio server must answer with its tool names",
            new Mapped<String>(
                text -> new UncheckedText(text).asString(),
                new ManagedMcp(
                    () -> new ListOf<>(server),
                    new StdioTransports(Duration.ofSeconds(10))
                ).tools(server)
            ),
            Matchers.contains("fs_read")
        );
    }

    private static Text interpreter() {
        Text found = new TextOf("");
        for (final String candidate : new ListOf<>("python3", "python")) {
            try {
                final Process probe = new ProcessBuilder(candidate, "-c", "pass")
                    .redirectErrorStream(true)
                    .start();
                if (probe.waitFor(5, TimeUnit.SECONDS) && probe.exitValue() == 0) {
                    found = new TextOf(candidate);
                    break;
                }
            } catch (final IOException ignored) {
                found = new TextOf("");
            } catch (final InterruptedException ex) {
                Thread.currentThread().interrupt();
                found = new TextOf("");
            }
        }
        return found;
    }

    private static Path script() throws IOException {
        final Path path = Files.createTempFile("pootos-mcp-tools", ".py");
        Files.writeString(
            path,
            String.join(
                System.lineSeparator(),
                "import sys",
                "sys.stdin.readline()",
                "print('{\"jsonrpc\":\"2.0\",\"id\":2,"
                    .concat("\"result\":{\"tools\":[{\"name\":\"fs_read\"}]}}')")
            )
        );
        path.toFile().deleteOnExit();
        return path;
    }
}
