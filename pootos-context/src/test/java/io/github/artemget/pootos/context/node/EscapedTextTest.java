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

package io.github.artemget.pootos.context.node;

import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

/**
 * Test case for {@link EscapedText}.
 *
 * @since 0.0.1
 */
final class EscapedTextTest {

    @Test
    void escapesDoubleQuote() {
        MatcherAssert.assertThat(
            "A double quote must be escaped",
            new EscapedText("a\"b").asString(),
            Matchers.equalTo("a\\\"b")
        );
    }

    @Test
    void escapesBackslash() {
        MatcherAssert.assertThat(
            "A backslash must be escaped",
            new EscapedText("a\\b").asString(),
            Matchers.equalTo("a\\\\b")
        );
    }

    @Test
    void escapesLineFeed() {
        MatcherAssert.assertThat(
            "A line feed must use its short escape",
            new EscapedText(String.format("a%cb", 10)).asString(),
            Matchers.equalTo("a\\nb")
        );
    }

    @Test
    void escapesTab() {
        MatcherAssert.assertThat(
            "A tab must use its short escape",
            new EscapedText("a\tb").asString(),
            Matchers.equalTo("a\\tb")
        );
    }

    @Test
    void escapesCarriageReturn() {
        MatcherAssert.assertThat(
            "A carriage return must use its short escape",
            new EscapedText(String.format("a%cb", 13)).asString(),
            Matchers.equalTo("a\\rb")
        );
    }

    @Test
    void escapesBackspace() {
        MatcherAssert.assertThat(
            "A backspace must use its short escape",
            new EscapedText("a\bb").asString(),
            Matchers.equalTo("a\\bb")
        );
    }

    @Test
    void escapesFormFeed() {
        MatcherAssert.assertThat(
            "A form feed must use its short escape",
            new EscapedText("a\fb").asString(),
            Matchers.equalTo("a\\fb")
        );
    }

    @Test
    void escapesOtherControlCharAsUnicode() {
        MatcherAssert.assertThat(
            "A control char without a short form must be \\u escaped",
            new EscapedText(String.format("a%cb", 1)).asString(),
            Matchers.equalTo("a\\u0001b")
        );
    }

    @Test
    void keepsPlainTextIntact() {
        MatcherAssert.assertThat(
            "Plain text must be left untouched",
            new EscapedText("plain").asString(),
            Matchers.equalTo("plain")
        );
    }
}
