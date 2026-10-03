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
 * A reference to a secret: the provider that will hold it and the name
 * it must be given there, for example {@code env:GITHUB_TOKEN}.
 *
 * <p>It names a secret, never its value.</p>
 *
 * @since 0.0.1
 */
public final class SecretRef implements Text {

    /**
     * Provider holding the secret.
     */
    private final String provider;

    /**
     * Secret name.
     */
    private final String name;

    /**
     * Ctor.
     *
     * @param provider Provider holding the secret
     * @param name Secret name
     */
    public SecretRef(final String provider, final String name) {
        this.provider = provider;
        this.name = name;
    }

    @Override
    public String asString() {
        return String.format("%s:%s", this.provider, this.name);
    }
}
