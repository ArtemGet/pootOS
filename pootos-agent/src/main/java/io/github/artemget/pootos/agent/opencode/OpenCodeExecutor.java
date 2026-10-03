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

import io.github.artemget.pootos.agent.AgentExecutor;
import io.github.artemget.pootos.sandbox.Sandbox;
import org.cactoos.Text;
import org.cactoos.text.TextOf;

/**
 * An {@link AgentExecutor} that runs opencode inside a {@link Sandbox}
 * (ADR-002).
 *
 * <p>One turn is a single non-interactive opencode run; its stdout becomes
 * the result. The executor holds no state of its own, so the sandbox and its
 * resource leases stay owned by the kernel.</p>
 *
 * @since 0.0.1
 */
public final class OpenCodeExecutor implements AgentExecutor {

    /**
     * Sandbox the run happens in.
     */
    private final Sandbox sandbox;

    /**
     * Ctor.
     *
     * @param sandbox Sandbox the run happens in
     */
    public OpenCodeExecutor(final Sandbox sandbox) {
        this.sandbox = sandbox;
    }

    @Override
    public Text execute(final Text prompt) {
        return this.sandbox.shell().exec(
            new TextOf(
                String.join(
                    " ",
                    new OpenCodeCommand(
                        new TextOf("opencode"),
                        new TextOf("run"),
                        prompt
                    )
                )
            )
        );
    }
}
