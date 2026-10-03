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

package io.github.artemget.pootos.context.graph;

import io.github.artemget.pootos.context.edge.Relation;
import io.github.artemget.pootos.context.edge.TypedEdge;
import io.github.artemget.pootos.context.node.LessonNode;
import io.github.artemget.pootos.context.node.Node;
import io.github.artemget.pootos.context.node.NodeId;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

/**
 * Test case for {@link MemoryGraph}.
 *
 * @since 0.0.1
 */
final class MemoryGraphTest {

    @Test
    void findsNodeAddedWithNode() {
        final Node node = new LessonNode("boom", "fix it", 0.5);
        MatcherAssert.assertThat(
            "A node added to the graph must be found by a slice from its id",
            new MemoryGraph().with(node).slice(node.id(), 0),
            Matchers.hasItem(node)
        );
    }

    @Test
    void connectsEndpointsByEdge() {
        final Node source = new LessonNode("a", "first", 0.1);
        final Node target = new LessonNode("b", "second", 0.2);
        MatcherAssert.assertThat(
            "An edge must connect its endpoints in a slice",
            new MemoryGraph()
                .with(source)
                .with(target)
                .with(new TypedEdge(source.id(), Relation.PRODUCES, target.id()))
                .slice(source.id(), 1),
            Matchers.hasItem(target)
        );
    }

    @Test
    void reachesNodeWithinDepth() {
        final Node first = new LessonNode("one", "fix one", 0.1);
        final Node second = new LessonNode("two", "fix two", 0.2);
        final Node third = new LessonNode("three", "fix three", 0.3);
        MatcherAssert.assertThat(
            "A node two hops away must be reached at depth two",
            new MemoryGraph()
                .with(first)
                .with(second)
                .with(third)
                .with(new TypedEdge(first.id(), Relation.PRODUCES, second.id()))
                .with(new TypedEdge(second.id(), Relation.PRODUCES, third.id()))
                .slice(first.id(), 2),
            Matchers.hasItem(third)
        );
    }

    @Test
    void stopsBeyondDepth() {
        final Node first = new LessonNode("one", "fix one", 0.1);
        final Node second = new LessonNode("two", "fix two", 0.2);
        final Node third = new LessonNode("three", "fix three", 0.3);
        MatcherAssert.assertThat(
            "A node beyond the given depth must not be reached",
            new MemoryGraph()
                .with(first)
                .with(second)
                .with(third)
                .with(new TypedEdge(first.id(), Relation.PRODUCES, second.id()))
                .with(new TypedEdge(second.id(), Relation.PRODUCES, third.id()))
                .slice(first.id(), 1),
            Matchers.not(Matchers.hasItem(third))
        );
    }

    @Test
    void collapsesNodesWithSameAddress() {
        final Node node = new LessonNode("boom", "fix it", 0.5);
        MatcherAssert.assertThat(
            "Nodes with the same content address must collapse to one",
            new MemoryGraph().with(node).with(node).slice(node.id(), 0),
            Matchers.hasSize(1)
        );
    }

    @Test
    void returnsEmptySliceForUnknownSeed() {
        MatcherAssert.assertThat(
            "A graph without the seed must yield an empty slice",
            new MemoryGraph().slice(new NodeId("unknown"), 3),
            Matchers.hasSize(0)
        );
    }
}
