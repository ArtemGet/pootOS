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
 * A {@link ProviderEntry} assembled from its plain, non-secret parts.
 *
 * @since 0.0.1
 */
public final class RecordedProvider implements ProviderEntry {

    /**
     * Provider name.
     */
    private final Text provider;

    /**
     * Provider kind.
     */
    private final Text type;

    /**
     * Base URL.
     */
    private final Text url;

    /**
     * Default model.
     */
    private final Text preferred;

    /**
     * Secret reference name.
     */
    private final Text reference;

    /**
     * Ctor.
     *
     * @param name Provider name
     * @param kind Provider kind
     * @param base Base URL
     * @param model Default model
     * @param key Secret reference name
     */
    public RecordedProvider(
        final Text name,
        final Text kind,
        final Text base,
        final Text model,
        final Text key
    ) {
        this.provider = name;
        this.type = kind;
        this.url = base;
        this.preferred = model;
        this.reference = key;
    }

    @Override
    public Text name() {
        return this.provider;
    }

    @Override
    public Text kind() {
        return this.type;
    }

    @Override
    public Text base() {
        return this.url;
    }

    @Override
    public Text model() {
        return this.preferred;
    }

    @Override
    public Text key() {
        return this.reference;
    }
}
