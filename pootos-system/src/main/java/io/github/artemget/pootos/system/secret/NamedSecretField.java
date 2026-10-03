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

package io.github.artemget.pootos.system.secret;

import org.cactoos.Text;

/**
 * A {@link SecretField} described by a reference, a label and a purpose.
 *
 * @since 0.0.1
 */
public final class NamedSecretField implements SecretField {

    /**
     * Secret reference.
     */
    private final SecretRef reference;

    /**
     * Human-readable label.
     */
    private final Text label;

    /**
     * Why the secret is needed.
     */
    private final Text purpose;

    /**
     * Ctor.
     *
     * @param reference Secret reference
     * @param label Human-readable label
     * @param purpose Why the secret is needed
     */
    public NamedSecretField(final SecretRef reference, final Text label, final Text purpose) {
        this.reference = reference;
        this.label = label;
        this.purpose = purpose;
    }

    @Override
    public SecretRef name() {
        return this.reference;
    }

    @Override
    public Text label() {
        return this.label;
    }

    @Override
    public Text purpose() {
        return this.purpose;
    }
}
