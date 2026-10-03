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
import org.junit.jupiter.api.Test;

/**
 * Test case for {@link DockerCommand}.
 *
 * @since 0.0.1
 */
final class DockerCommandTest {

    @Test
    void buildsHardenedArgv() {
        MatcherAssert.assertThat(
            "The argv must encode the full isolation profile of ADR-001",
            new ListOf<String>(
                new DockerCommand(
                    new TextOf("alpine"), new TextOf("--runtime=runsc"),
                    new TextOf("256m"), new TextOf("64"), new TextOf("1.0"),
                    new ListOf<>(), new TextOf("echo hi")
                )
            ),
            Matchers.contains(
                "docker", "run", "--rm", "-i", "--runtime=runsc",
                "--read-only", "--tmpfs", "/tmp:rw,noexec,nosuid,size=64m",
                "--user", "65534:65534", "--cap-drop", "ALL",
                "--security-opt", "no-new-privileges", "--network", "none",
                "--memory", "256m", "--pids-limit", "64", "--cpus", "1.0",
                "alpine", "sh", "-c", "echo hi"
            )
        );
    }

    @Test
    void omitsRuntimeOnFallback() {
        MatcherAssert.assertThat(
            "A degraded runtime must not pass any runtime selector",
            new ListOf<String>(
                new DockerCommand(
                    new TextOf("alpine"), new TextOf(""),
                    new TextOf("256m"), new TextOf("64"), new TextOf("1.0"),
                    new ListOf<>(), new TextOf("echo hi")
                )
            ),
            Matchers.not(Matchers.hasItem("--runtime=runsc"))
        );
    }

    @Test
    void mountsNothingByDefault() {
        MatcherAssert.assertThat(
            "No host directory may be mounted unless explicit",
            new ListOf<String>(
                new DockerCommand(
                    new TextOf("alpine"), new TextOf(""),
                    new TextOf("256m"), new TextOf("64"), new TextOf("1.0"),
                    new ListOf<>(), new TextOf("echo hi")
                )
            ),
            Matchers.not(Matchers.hasItem("--volume"))
        );
    }

    @Test
    void mountsExplicitVolume() {
        MatcherAssert.assertThat(
            "An explicitly provided mount must reach docker",
            new ListOf<String>(
                new DockerCommand(
                    new TextOf("alpine"), new TextOf(""),
                    new TextOf("256m"), new TextOf("64"), new TextOf("1.0"),
                    new ListOf<>(new TextOf("/work:/data:ro")),
                    new TextOf("echo hi")
                )
            ),
            Matchers.hasItems("--volume", "/work:/data:ro")
        );
    }

    @Test
    void runsEmptyCommandWithoutShell() {
        MatcherAssert.assertThat(
            "An empty command must not invoke a shell",
            new ListOf<String>(
                new DockerCommand(
                    new TextOf("alpine"), new TextOf(""),
                    new TextOf("256m"), new TextOf("64"), new TextOf("1.0"),
                    new ListOf<>(), new TextOf("")
                )
            ),
            Matchers.not(Matchers.hasItem("sh"))
        );
    }
}
