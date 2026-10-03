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

import io.github.artemget.pootos.sandbox.docker.DockerProbe;
import io.github.artemget.pootos.sandbox.docker.DockerSandbox;
import io.github.artemget.pootos.sandbox.docker.GvisorRuntime;
import io.github.artemget.pootos.sandbox.docker.HardenedRuntime;
import io.github.artemget.pootos.sandbox.docker.ProbedRuntime;
import java.util.concurrent.TimeUnit;
import org.cactoos.list.ListOf;
import org.cactoos.text.FormattedText;
import org.cactoos.text.TextOf;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

/**
 * Test case for {@link OpenCodeImage}.
 *
 * <p>The last test is an integration test (ADR-001, ADR-002): it starts the
 * real hardened sandbox and checks that opencode answers inside it. It is
 * skipped by assumption when Docker is unavailable or the pinned image has not
 * been built locally, so a host without the image stays green. Docker never
 * runs unbounded: every probe and the run itself carry a hard timeout.</p>
 *
 * @since 0.0.1
 */
final class OpenCodeImageTest {

    @Test
    void defaultsToPinnedImage() throws Exception {
        MatcherAssert.assertThat(
            "The default image must be the pinned agent image",
            new OpenCodeImage().asString(),
            Matchers.equalTo("pootos/opencode:1.18.34")
        );
    }

    @Test
    void honoursConfiguredImage() throws Exception {
        MatcherAssert.assertThat(
            "A configured image must win over the pinned default",
            new OpenCodeImage(new TextOf("registry.local/agent:dev"))
                .asString(),
            Matchers.equalTo("registry.local/agent:dev")
        );
    }

    @Test
    void fallsBackOnBlankImage() throws Exception {
        MatcherAssert.assertThat(
            "A blank image must fall back to the pinned default",
            new OpenCodeImage(new TextOf("  ")).asString(),
            Matchers.equalTo("pootos/opencode:1.18.34")
        );
    }

    @Test
    @Timeout(value = 3, unit = TimeUnit.MINUTES)
    void runsOpenCodeInSandbox() throws Exception {
        final String image = new OpenCodeImage().asString();
        Assumptions.assumeTrue(
            this.answers("docker", "version"),
            "Docker is required to run the agent sandbox"
        );
        Assumptions.assumeTrue(
            this.answers("docker", "image", "inspect", image),
            new FormattedText(
                "The agent image must be built locally first: %s", image
            ).asString()
        );
        MatcherAssert.assertThat(
            "opencode must answer from inside the hardened sandbox",
            new DockerSandbox(
                new TextOf(image),
                new ProbedRuntime(
                    new GvisorRuntime(),
                    new HardenedRuntime(),
                    new DockerProbe(new TextOf("runsc"))
                ),
                new TextOf("512m"),
                new TextOf("128"),
                new TextOf("1.0"),
                new ListOf<>()
            ).shell()
                .exec(new TextOf("HOME=/tmp opencode --version"))
                .asString(),
            Matchers.not(Matchers.blankString())
        );
    }

    private boolean answers(final String... command) throws Exception {
        final Process process = new ProcessBuilder(command)
            .redirectErrorStream(true)
            .start();
        process.getOutputStream().close();
        final boolean done = process.waitFor(30, TimeUnit.SECONDS);
        if (!done) {
            process.destroyForcibly();
        }
        return done && process.exitValue() == 0;
    }
}
