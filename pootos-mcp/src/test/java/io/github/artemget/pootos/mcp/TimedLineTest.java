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

import java.io.ByteArrayInputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Test case for {@link TimedLine}.
 *
 * @since 0.0.1
 */
final class TimedLineTest {

    @Test
    void readsAvailableLine() throws McpException {
        MatcherAssert.assertThat(
            "An available line must be returned",
            new TimedLine(
                new ByteArrayInputStream(
                    "{\"jsonrpc\":\"2.0\"}"
                        .concat(System.lineSeparator())
                        .getBytes(StandardCharsets.UTF_8)
                ),
                Duration.ofSeconds(5)
            ).value(),
            Matchers.equalTo("{\"jsonrpc\":\"2.0\"}")
        );
    }

    @Test
    void rejectsEmptyStream() {
        MatcherAssert.assertThat(
            "An empty stream must be rejected",
            Assertions.assertThrows(
                McpException.class,
                () -> new TimedLine(
                    new ByteArrayInputStream(new byte[0]),
                    Duration.ofSeconds(5)
                ).value()
            ).getMessage(),
            Matchers.containsString("no reply")
        );
    }

    @Test
    void timesOutOnSilentStream() throws Exception {
        try (
            PipedOutputStream sink = new PipedOutputStream();
            PipedInputStream stream = new PipedInputStream(sink)
        ) {
            MatcherAssert.assertThat(
                "A silent stream must time out instead of hanging",
                Assertions.assertThrows(
                    McpException.class,
                    () -> new TimedLine(stream, Duration.ofMillis(100)).value()
                ).getMessage(),
                Matchers.containsString("did not reply")
            );
        }
    }
}
