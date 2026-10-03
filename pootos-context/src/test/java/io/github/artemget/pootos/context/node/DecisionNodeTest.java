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

import java.nio.charset.StandardCharsets;
import org.cactoos.bytes.Sha256DigestOf;
import org.cactoos.io.InputOf;
import org.cactoos.text.HexOf;
import org.cactoos.text.UncheckedText;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

/**
 * Test case for {@link DecisionNode}.
 *
 * @since 0.0.1
 */
final class DecisionNodeTest {

    @Test
    void derivesSameIdForSameContent() {
        MatcherAssert.assertThat(
            "Content addressing must be deterministic",
            new DecisionNode("sqlite", "portable").id(),
            Matchers.equalTo(new DecisionNode("sqlite", "portable").id())
        );
    }

    @Test
    void producesDifferentIdForDifferentContent() {
        MatcherAssert.assertThat(
            "Different content must yield different ids",
            new DecisionNode("sqlite", "portable").id(),
            Matchers.not(Matchers.equalTo(new DecisionNode("sqlite", "fast").id()))
        );
    }

    @Test
    void derivesIdFromCanonicalJsonDigest() {
        MatcherAssert.assertThat(
            "Id must equal the hex digest of the canonical JSON",
            new DecisionNode("sqlite", "portable").id(),
            Matchers.equalTo(
                new NodeId(
                    new UncheckedText(
                        new HexOf(
                            new Sha256DigestOf(
                                new InputOf(
                                    "{\"choice\":\"sqlite\",\"rationale\":\"portable\"}",
                                    StandardCharsets.UTF_8
                                )
                            )
                        )
                    ).asString()
                )
            )
        );
    }

    @Test
    void rendersCanonicalJson() {
        MatcherAssert.assertThat(
            "Canonical JSON must list fields in sorted order",
            new DecisionNode("sqlite", "portable").json(),
            Matchers.equalTo("{\"choice\":\"sqlite\",\"rationale\":\"portable\"}")
        );
    }

    @Test
    void escapesQuotesInJson() {
        MatcherAssert.assertThat(
            "Quotes in content must be escaped in JSON",
            new DecisionNode("a\"b", "c").json(),
            Matchers.equalTo("{\"choice\":\"a\\\"b\",\"rationale\":\"c\"}")
        );
    }

    @Test
    void treatsEqualContentAsEqual() {
        MatcherAssert.assertThat(
            "Decisions with equal content must be equal",
            new DecisionNode("sqlite", "portable"),
            Matchers.equalTo(new DecisionNode("sqlite", "portable"))
        );
    }

    @Test
    void hashesEqualContentEqually() {
        MatcherAssert.assertThat(
            "Decisions with equal content must share a hash code",
            new DecisionNode("sqlite", "portable").hashCode(),
            Matchers.equalTo(new DecisionNode("sqlite", "portable").hashCode())
        );
    }
}
