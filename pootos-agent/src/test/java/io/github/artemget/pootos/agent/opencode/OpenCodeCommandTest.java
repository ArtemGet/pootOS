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

package io.github.artemget.pootos.agent.opencode;

import org.cactoos.list.ListOf;
import org.cactoos.text.TextOf;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

/**
 * Test case for {@link OpenCodeCommand}.
 *
 * @since 0.0.1
 */
final class OpenCodeCommandTest {

    @Test
    void composesRunArgv() {
        MatcherAssert.assertThat(
            "The argv must be opencode run followed by the prompt",
            new ListOf<String>(
                new OpenCodeCommand(
                    new TextOf("opencode"), new TextOf("run"),
                    new TextOf("hello")
                )
            ),
            Matchers.contains("opencode", "run", "hello")
        );
    }

    @Test
    void honoursConfiguredBinaryAndSubcommand() {
        MatcherAssert.assertThat(
            "The argv must use the configured binary and subcommand",
            new ListOf<String>(
                new OpenCodeCommand(
                    new TextOf("/opt/opencode/bin/oc"), new TextOf("exec"),
                    new TextOf("hi")
                )
            ),
            Matchers.contains("/opt/opencode/bin/oc", "exec", "hi")
        );
    }

    @Test
    void keepsMultilinePromptAsOneArgument() {
        final String prompt = String.format("fix%nthe%nbuild");
        MatcherAssert.assertThat(
            "A multiline prompt must remain a single argv element",
            new ListOf<String>(
                new OpenCodeCommand(
                    new TextOf("opencode"), new TextOf("run"),
                    new TextOf(prompt)
                )
            ),
            Matchers.hasItem(prompt)
        );
    }
}
