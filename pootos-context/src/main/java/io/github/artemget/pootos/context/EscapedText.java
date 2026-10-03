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

package io.github.artemget.pootos.context;

/**
 * A text rendered as an escaped JSON string body.
 *
 * @since 0.0.1
 */
public final class EscapedText implements Text {

    /**
     * Source text.
     */
    private final String source;

    /**
     * Ctor.
     *
     * @param source Source text
     */
    public EscapedText(final String source) {
        this.source = source;
    }

    @Override
    public String value() {
        final StringBuilder out = new StringBuilder(this.source.length());
        for (int pos = 0; pos < this.source.length(); ++pos) {
            final char chr = this.source.charAt(pos);
            if (chr == '\\') {
                out.append("\\\\");
            } else if (chr == '"') {
                out.append("\\\"");
            } else if (chr == '\b') {
                out.append("\\b");
            } else if (chr == '\f') {
                out.append("\\f");
            } else if (chr == '\n') {
                out.append("\\n");
            } else if (chr == '\r') {
                out.append("\\r");
            } else if (chr == '\t') {
                out.append("\\t");
            } else if (chr < 0x20) {
                out.append("\\u")
                    .append(Character.forDigit((chr >>> 12) & 0xF, 16))
                    .append(Character.forDigit((chr >>> 8) & 0xF, 16))
                    .append(Character.forDigit((chr >>> 4) & 0xF, 16))
                    .append(Character.forDigit(chr & 0xF, 16));
            } else {
                out.append(chr);
            }
        }
        return out.toString();
    }
}
