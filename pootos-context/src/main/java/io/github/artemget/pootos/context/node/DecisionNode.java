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
import java.util.Objects;
import org.cactoos.bytes.Sha256DigestOf;
import org.cactoos.io.InputOf;
import org.cactoos.text.HexOf;
import org.cactoos.text.UncheckedText;

/**
 * A first-class decision recorded in the context graph.
 *
 * @since 0.0.1
 */
public final class DecisionNode implements Node {

    /**
     * The choice made.
     */
    private final String choice;

    /**
     * Rationale behind the choice.
     */
    private final String rationale;

    /**
     * Ctor.
     *
     * @param choice The choice made
     * @param rationale Rationale behind the choice
     */
    public DecisionNode(final String choice, final String rationale) {
        this.choice = choice;
        this.rationale = rationale;
    }

    @Override
    public NodeId id() {
        return new NodeId(
            new UncheckedText(
                new HexOf(
                    new Sha256DigestOf(
                        new InputOf(this.json(), StandardCharsets.UTF_8)
                    )
                )
            ).asString()
        );
    }

    @Override
    public String json() {
        return "{\"choice\":\"%s\",\"rationale\":\"%s\"}".formatted(
            new EscapedText(this.choice).asString(),
            new EscapedText(this.rationale).asString()
        );
    }

    @Override
    public boolean equals(final Object obj) {
        return this == obj
            || obj instanceof DecisionNode node
            && this.same(node);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.choice, this.rationale);
    }

    private boolean same(final DecisionNode other) {
        return this.choice.equals(other.choice)
            && this.rationale.equals(other.rationale);
    }
}
