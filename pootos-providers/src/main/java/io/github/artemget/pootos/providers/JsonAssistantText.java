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

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.cactoos.Text;

/**
 * The assistant text pulled out of an OpenAI-compatible chat-completions reply.
 *
 * <p>A minimal, dependency-free extraction of the first {@code content} string;
 * it is not a general JSON parser and deliberately ignores every other field
 * of the response.</p>
 *
 * @since 0.0.1
 */
public final class JsonAssistantText implements Text {

    /**
     * The first {@code content} string in the response.
     */
    private static final Pattern CONTENT = Pattern.compile(
        "\"content\"\\s*:\\s*\"((?:\\\\.|[^\"\\\\])*)\""
    );

    /**
     * Response body.
     */
    private final Text source;

    /**
     * Ctor.
     *
     * @param source Response body
     */
    public JsonAssistantText(final Text source) {
        this.source = source;
    }

    @Override
    public String asString() throws Exception {
        final Matcher matcher = JsonAssistantText.CONTENT.matcher(
            this.source.asString()
        );
        if (!matcher.find()) {
            throw new IllegalStateException("Response carries no assistant content");
        }
        return matcher.group(1);
    }
}
