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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.cactoos.Text;
import org.cactoos.text.UncheckedText;

/**
 * The argv of a stdio MCP server: a program followed by its arguments.
 *
 * <p>The argv is composed, not executed, so the exact command line a transport
 * will spawn can be asserted without a process. The program and each argument
 * are separate tokens, so a path that contains whitespace stays intact — the
 * configured command line must already be tokenized by the caller.</p>
 *
 * @since 0.0.1
 */
public final class StdioCommand implements Iterable<String> {

    /**
     * Program to start.
     */
    private final Text program;

    /**
     * Arguments passed to the program.
     */
    private final Iterable<Text> args;

    /**
     * Ctor.
     *
     * @param program Program to start
     * @param args Arguments passed to the program
     */
    public StdioCommand(final Text program, final Iterable<Text> args) {
        this.program = program;
        this.args = args;
    }

    @Override
    public Iterator<String> iterator() {
        final List<String> argv = new ArrayList<>(1);
        argv.add(new UncheckedText(this.program).asString());
        for (final Text arg : this.args) {
            argv.add(new UncheckedText(arg).asString());
        }
        return argv.iterator();
    }
}
