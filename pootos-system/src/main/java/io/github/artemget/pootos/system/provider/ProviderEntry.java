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

package io.github.artemget.pootos.system.provider;

import org.cactoos.Text;

/**
 * A non-secret description of one configured model provider.
 *
 * <p>An entry carries only plain data: the provider name, its kind, the base
 * URL and the default model, plus the name of the {@code SecretRef} that will
 * hold the key. It never carries a secret value.</p>
 *
 * @since 0.0.1
 */
public interface ProviderEntry {

    /**
     * Provider name used to reference it.
     *
     * @return Provider name
     */
    Text name();

    /**
     * Provider kind selecting the adapter.
     *
     * @return Provider kind
     */
    Text kind();

    /**
     * Base URL of the provider endpoint.
     *
     * @return Base URL
     */
    Text base();

    /**
     * Default model of the provider.
     *
     * @return Default model
     */
    Text model();

    /**
     * Name of the secret reference holding the key.
     *
     * @return Secret reference name
     */
    Text key();
}
