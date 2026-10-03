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

package io.github.artemget.pootos.sandbox;

import org.cactoos.text.TextOf;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

/**
 * Test case for {@link FakeSandbox}.
 *
 * @since 0.0.1
 */
final class FakeSandboxTest {

    @Test
    void returnsCannedOutput() throws Exception {
        MatcherAssert.assertThat(
            "Fake sandbox must return its canned output",
            new FakeSandbox(new TextOf("pong"))
                .shell()
                .exec(new TextOf("ping"))
                .asString(),
            Matchers.equalTo("pong")
        );
    }

    @Test
    void returnsEmptyCannedOutput() throws Exception {
        MatcherAssert.assertThat(
            "Fake sandbox must return an empty canned output",
            new FakeSandbox(new TextOf(""))
                .shell()
                .exec(new TextOf("ping"))
                .asString(),
            Matchers.equalTo("")
        );
    }
}
