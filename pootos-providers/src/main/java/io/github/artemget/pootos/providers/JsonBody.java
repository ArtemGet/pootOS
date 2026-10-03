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

package io.github.artemget.pootos.providers;

import org.cactoos.Text;
import org.cactoos.text.FormattedText;

/**
 * A chat-completions request body rendered as JSON.
 *
 * <p>A minimal, dependency-free renderer: the model and the prompt are emitted
 * as JSON strings with control characters escaped, which is all the
 * OpenAI-compatible protocol needs for a single user message.</p>
 *
 * @since 0.0.1
 */
public final class JsonBody implements Text {

    /**
     * Model name.
     */
    private final Text model;

    /**
     * User prompt.
     */
    private final Text prompt;

    /**
     * Ctor.
     *
     * @param model Model name
     * @param prompt User prompt
     */
    public JsonBody(final Text model, final Text prompt) {
        this.model = model;
        this.prompt = prompt;
    }

    @Override
    public String asString() throws Exception {
        return new FormattedText(
            "{\"model\":\"%s\",\"messages\":[{\"role\":\"user\",\"content\":\"%s\"}]}",
            JsonBody.escaped(this.model.asString()),
            JsonBody.escaped(this.prompt.asString())
        ).asString();
    }

    private static String escaped(final String source) {
        final StringBuilder out = new StringBuilder(source.length());
        for (int pos = 0; pos < source.length(); ++pos) {
            final char chr = source.charAt(pos);
            if (chr == '\\') {
                out.append("\\\\");
            } else if (chr == '"') {
                out.append("\\\"");
            } else if (chr == '\n') {
                out.append("\\n");
            } else if (chr == '\r') {
                out.append("\\r");
            } else if (chr == '\t') {
                out.append("\\t");
            } else {
                out.append(chr);
            }
        }
        return out.toString();
    }
}
