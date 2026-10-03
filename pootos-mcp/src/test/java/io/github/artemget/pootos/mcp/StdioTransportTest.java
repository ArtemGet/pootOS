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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import org.cactoos.Text;
import org.cactoos.list.ListOf;
import org.cactoos.text.TextOf;
import org.cactoos.text.UncheckedText;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

/**
 * Test case for {@link StdioTransport}.
 *
 * <p>The success and timeout cases spawn a throwaway Python echo server; they
 * are skipped by assumption when no interpreter is available, and every exchange
 * is bounded by a hard timeout so the suite never hangs.</p>
 *
 * @since 0.0.1
 */
final class StdioTransportTest {

    @Test
    void rejectsMissingProgram() {
        MatcherAssert.assertThat(
            "A missing server program must be reported",
            Assertions.assertThrows(
                McpException.class,
                () -> new StdioTransport(
                    new StdioCommand(
                        new TextOf("pootos-no-such-binary-xyz"),
                        new ListOf<>()
                    ),
                    Duration.ofSeconds(2)
                ).send(JsonRpc.listTools())
            ).getMessage(),
            Matchers.containsString("Cannot start")
        );
    }

    @Test
    void exchangesFrameWithRealServer() throws Exception {
        final Text python = StdioTransportTest.interpreter();
        Assumptions.assumeTrue(
            !new UncheckedText(python).asString().isEmpty(),
            "Python is required to run the echo server"
        );
        MatcherAssert.assertThat(
            "A real stdio server must answer with a JSON-RPC frame",
            new UncheckedText(
                new StdioTransport(
                    new StdioCommand(
                        python,
                        new ListOf<>(
                            new TextOf(
                                StdioTransportTest.script(
                                    "import sys",
                                    "sys.stdin.readline()",
                                    "print('{\"jsonrpc\":\"2.0\",\"id\":1,"
                                        .concat("\"result\":{\"tools\":[]}}')")
                                ).toString()
                            )
                        )
                    ),
                    Duration.ofSeconds(10)
                ).send(JsonRpc.listTools())
            ).asString(),
            Matchers.containsString("\"tools\"")
        );
    }

    @Test
    void failsWhenServerIsSilent() throws Exception {
        final Text python = StdioTransportTest.interpreter();
        Assumptions.assumeTrue(
            !new UncheckedText(python).asString().isEmpty(),
            "Python is required to run the silent server"
        );
        MatcherAssert.assertThat(
            "A silent server must fail within the timeout, not hang",
            Assertions.assertThrows(
                McpException.class,
                () -> new StdioTransport(
                    new StdioCommand(
                        python,
                        new ListOf<>(
                            new TextOf(
                                StdioTransportTest.script(
                                    "import sys, time",
                                    "sys.stdin.readline()",
                                    "time.sleep(30)"
                                ).toString()
                            )
                        )
                    ),
                    Duration.ofMillis(300)
                ).send(JsonRpc.listTools())
            ).getMessage(),
            Matchers.containsString("did not reply")
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

    private static Path script(final String... lines) throws IOException {
        final Path path = Files.createTempFile("pootos-mcp-echo", ".py");
        Files.writeString(path, String.join(System.lineSeparator(), lines));
        path.toFile().deleteOnExit();
        return path;
    }
}
