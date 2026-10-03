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

package io.github.artemget.pootos.context.edge;

import io.github.artemget.pootos.context.node.NodeId;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

/**
 * Test case for {@link TypedEdge}.
 *
 * @since 0.0.1
 */
final class EdgeTest {

    @Test
    void exposesSource() {
        MatcherAssert.assertThat(
            "Edge must expose its source node",
            new TypedEdge(new NodeId("a"), Relation.PRODUCES, new NodeId("b")).from(),
            Matchers.equalTo(new NodeId("a"))
        );
    }

    @Test
    void exposesTarget() {
        MatcherAssert.assertThat(
            "Edge must expose its target node",
            new TypedEdge(new NodeId("a"), Relation.PRODUCES, new NodeId("b")).to(),
            Matchers.equalTo(new NodeId("b"))
        );
    }

    @Test
    void exposesTypedRelation() {
        MatcherAssert.assertThat(
            "Edge must expose its relation kind",
            new TypedEdge(new NodeId("a"), Relation.TAUGHT_BY, new NodeId("b")).relation(),
            Matchers.equalTo(Relation.TAUGHT_BY)
        );
    }

    @Test
    void comparesEqualForSameEndpointsAndRelation() {
        MatcherAssert.assertThat(
            "Edges with equal content must be equal",
            new TypedEdge(new NodeId("a"), Relation.ADDRESSES, new NodeId("b")),
            Matchers.equalTo(
                new TypedEdge(new NodeId("a"), Relation.ADDRESSES, new NodeId("b"))
            )
        );
    }

    @Test
    void distinguishesDifferentRelation() {
        MatcherAssert.assertThat(
            "Edges with different relation must not be equal",
            new TypedEdge(new NodeId("a"), Relation.ADDRESSES, new NodeId("b")),
            Matchers.not(
                Matchers.equalTo(
                    new TypedEdge(new NodeId("a"), Relation.PRODUCES, new NodeId("b"))
                )
            )
        );
    }

    @Test
    void hashesSameContentEqually() {
        MatcherAssert.assertThat(
            "Edges with equal content must share a hash code",
            new TypedEdge(new NodeId("a"), Relation.PRODUCES, new NodeId("b")).hashCode(),
            Matchers.equalTo(
                new TypedEdge(new NodeId("a"), Relation.PRODUCES, new NodeId("b")).hashCode()
            )
        );
    }
}
