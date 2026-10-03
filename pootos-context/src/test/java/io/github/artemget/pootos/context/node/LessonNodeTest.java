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
 * Test case for {@link LessonNode}.
 *
 * @since 0.0.1
 */
final class LessonNodeTest {

    @Test
    void derivesSameIdForSameContent() {
        MatcherAssert.assertThat(
            "Content addressing must be deterministic",
            new LessonNode("boom", "fix it", 0.5).id(),
            Matchers.equalTo(new LessonNode("boom", "fix it", 0.5).id())
        );
    }

    @Test
    void producesDifferentIdForDifferentContent() {
        MatcherAssert.assertThat(
            "Different content must yield different ids",
            new LessonNode("boom", "fix it", 0.5).id(),
            Matchers.not(Matchers.equalTo(new LessonNode("boom", "other", 0.5).id()))
        );
    }

    @Test
    void derivesIdFromCanonicalJsonDigest() {
        MatcherAssert.assertThat(
            "Id must equal the hex digest of the canonical JSON",
            new LessonNode("boom", "fix it", 0.5).id(),
            Matchers.equalTo(
                new NodeId(
                    new HexDigest(
                        "{\"advice\":\"fix it\",\"confidence\":0.5,\"trigger\":\"boom\"}"
                    ).value()
                )
            )
        );
    }

    @Test
    void rendersCanonicalJson() {
        MatcherAssert.assertThat(
            "Canonical JSON must list fields in sorted order",
            new LessonNode("boom", "fix it", 0.9).json(),
            Matchers.equalTo(
                "{\"advice\":\"fix it\",\"confidence\":0.9,\"trigger\":\"boom\"}"
            )
        );
    }

    @Test
    void escapesQuotesInJson() {
        MatcherAssert.assertThat(
            "Quotes in content must be escaped in JSON",
            new LessonNode("a\"b", "c", 1.0).json(),
            Matchers.equalTo(
                "{\"advice\":\"c\",\"confidence\":1.0,\"trigger\":\"a\\\"b\"}"
            )
        );
    }

    @Test
    void escapesMultilineAdviceInJson() {
        MatcherAssert.assertThat(
            "A multiline advice must not break the canonical JSON",
            new LessonNode("boom", String.format("one%ctwo", 10), 0.5).json(),
            Matchers.equalTo(
                "{\"advice\":\"one\\ntwo\",\"confidence\":0.5,\"trigger\":\"boom\"}"
            )
        );
    }

    @Test
    void treatsEqualContentAsEqual() {
        MatcherAssert.assertThat(
            "Lessons with equal content must be equal",
            new LessonNode("boom", "fix it", 0.5),
            Matchers.equalTo(new LessonNode("boom", "fix it", 0.5))
        );
    }

    @Test
    void hashesEqualContentEqually() {
        MatcherAssert.assertThat(
            "Lessons with equal content must share a hash code",
            new LessonNode("boom", "fix it", 0.5).hashCode(),
            Matchers.equalTo(new LessonNode("boom", "fix it", 0.5).hashCode())
        );
    }
}
