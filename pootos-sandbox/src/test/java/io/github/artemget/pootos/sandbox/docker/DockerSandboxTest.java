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

package io.github.artemget.pootos.sandbox.docker;

import org.cactoos.list.ListOf;
import org.cactoos.text.TextOf;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

/**
 * Test case for {@link DockerSandbox}.
 *
 * <p>This is an integration test: it runs a real container and is skipped by
 * assumption when Docker is unavailable, so a host without Docker stays
 * green.</p>
 *
 * @since 0.0.1
 */
final class DockerSandboxTest {

    @Test
    void runsCommandInContainer() throws Exception {
        Assumptions.assumeTrue(
            new ProcessBuilder("docker", "version")
                .redirectErrorStream(true)
                .start()
                .waitFor() == 0,
            "Docker is required to run the sandbox"
        );
        MatcherAssert.assertThat(
            "A container must run under the hardened profile",
            this.sandbox().shell().exec(new TextOf("")).asString(),
            Matchers.containsString("Hello from Docker")
        );
    }

    @Test
    void surfacesRuntime() throws Exception {
        Assumptions.assumeTrue(
            new ProcessBuilder("docker", "version")
                .redirectErrorStream(true)
                .start()
                .waitFor() == 0,
            "Docker is required to run the sandbox"
        );
        MatcherAssert.assertThat(
            "The isolation runtime must be surfaced, not hidden",
            this.sandbox().status().asString(),
            Matchers.containsString("runsc")
        );
    }

    private DockerSandbox sandbox() {
        return new DockerSandbox(
            new TextOf("hello-world"),
            new ProbedRuntime(
                new GvisorRuntime(),
                new HardenedRuntime(),
                new DockerProbe(new TextOf("runsc"))
            ),
            new TextOf("256m"),
            new TextOf("64"),
            new TextOf("1.0"),
            new ListOf<>()
        );
    }
}
