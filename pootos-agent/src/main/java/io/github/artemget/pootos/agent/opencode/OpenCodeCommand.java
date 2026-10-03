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

import java.util.Iterator;
import java.util.List;
import org.cactoos.Text;
import org.cactoos.text.UncheckedText;

/**
 * The argv of one non-interactive opencode run (ADR-002).
 *
 * <p>The argv is composed, not executed, so the invocation can be asserted
 * without a Node runtime or a sandbox. The binary and subcommand are
 * injected, so the adapter stays independent of a fixed opencode layout.</p>
 *
 * @since 0.0.1
 */
public final class OpenCodeCommand implements Iterable<String> {

    /**
     * Executable to invoke.
     */
    private final Text binary;

    /**
     * Non-interactive subcommand, e.g. {@code run}.
     */
    private final Text subcommand;

    /**
     * Prompt of the turn.
     */
    private final Text prompt;

    /**
     * Ctor.
     *
     * @param binary Executable to invoke
     * @param subcommand Non-interactive subcommand
     * @param prompt Prompt of the turn
     */
    public OpenCodeCommand(
        final Text binary,
        final Text subcommand,
        final Text prompt
    ) {
        this.binary = binary;
        this.subcommand = subcommand;
        this.prompt = prompt;
    }

    @Override
    public Iterator<String> iterator() {
        return List.of(
            new UncheckedText(this.binary).asString(),
            new UncheckedText(this.subcommand).asString(),
            new UncheckedText(this.prompt).asString()
        ).iterator();
    }
}
