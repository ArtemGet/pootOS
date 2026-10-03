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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import org.cactoos.Text;
import org.cactoos.text.UncheckedText;

/**
 * The hardened {@code docker run} argv of one sandbox command (ADR-001).
 *
 * <p>The argv is composed, not executed, so the isolation profile can be
 * asserted without a Docker daemon. Host directories are never mounted
 * unless passed explicitly.</p>
 *
 * @since 0.0.1
 */
public final class DockerCommand implements Iterable<String> {

    /**
     * Image the container is created from.
     */
    private final Text image;

    /**
     * Runtime selector, e.g. {@code --runtime=runsc}.
     */
    private final Text option;

    /**
     * Memory limit.
     */
    private final Text memory;

    /**
     * Process limit.
     */
    private final Text pids;

    /**
     * CPU limit.
     */
    private final Text cpus;

    /**
     * Explicit host mounts, empty by default.
     */
    private final Iterable<Text> mounts;

    /**
     * Command to run inside the container.
     */
    private final Text command;

    /**
     * Ctor.
     *
     * @param image Image the container is created from
     * @param option Runtime selector
     * @param memory Memory limit
     * @param pids Process limit
     * @param cpus CPU limit
     * @param mounts Explicit host mounts
     * @param command Command to run inside the container
     */
    public DockerCommand(
        final Text image,
        final Text option,
        final Text memory,
        final Text pids,
        final Text cpus,
        final Iterable<Text> mounts,
        final Text command
    ) {
        this.image = image;
        this.option = option;
        this.memory = memory;
        this.pids = pids;
        this.cpus = cpus;
        this.mounts = mounts;
        this.command = command;
    }

    @Override
    public Iterator<String> iterator() {
        final Collection<String> argv = new ArrayList<>(32);
        argv.addAll(List.of("docker", "run", "--rm", "-i"));
        final String selector = new UncheckedText(this.option).asString();
        if (!selector.isEmpty()) {
            argv.add(selector);
        }
        argv.addAll(
            List.of(
                "--read-only",
                "--tmpfs", "/tmp:rw,noexec,nosuid,size=64m",
                "--user", "65534:65534",
                "--cap-drop", "ALL",
                "--security-opt", "no-new-privileges",
                "--network", "none",
                "--memory", new UncheckedText(this.memory).asString(),
                "--pids-limit", new UncheckedText(this.pids).asString(),
                "--cpus", new UncheckedText(this.cpus).asString()
            )
        );
        for (final Text mount : this.mounts) {
            argv.addAll(List.of("--volume", new UncheckedText(mount).asString()));
        }
        argv.add(new UncheckedText(this.image).asString());
        final String body = new UncheckedText(this.command).asString();
        if (!body.isEmpty()) {
            argv.addAll(List.of("sh", "-c", body));
        }
        return argv.iterator();
    }
}
