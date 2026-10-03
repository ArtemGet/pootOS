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

import java.util.Objects;

/**
 * A first-class lesson of the journal of mistakes.
 *
 * @since 0.0.1
 */
public final class LessonNode implements Node {

    /**
     * Trigger signature.
     */
    private final String trigger;

    /**
     * Advice to apply.
     */
    private final String advice;

    /**
     * Confidence in the lesson.
     */
    private final double confidence;

    /**
     * Ctor.
     *
     * @param trigger Trigger signature
     * @param advice Advice to apply
     * @param confidence Confidence in the lesson
     */
    public LessonNode(final String trigger, final String advice, final double confidence) {
        this.trigger = trigger;
        this.advice = advice;
        this.confidence = confidence;
    }

    @Override
    public NodeId id() {
        return new NodeId(new HexDigest(this.json()).value());
    }

    @Override
    public String json() {
        return "{\"advice\":\"%s\",\"confidence\":%s,\"trigger\":\"%s\"}".formatted(
            new EscapedText(this.advice).value(),
            this.confidence,
            new EscapedText(this.trigger).value()
        );
    }

    @Override
    public boolean equals(final Object obj) {
        return this == obj
            || obj instanceof LessonNode node
            && this.same(node);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.trigger, this.advice, this.confidence);
    }

    private boolean same(final LessonNode other) {
        return this.trigger.equals(other.trigger)
            && this.advice.equals(other.advice)
            && Double.compare(this.confidence, other.confidence) == 0;
    }
}
