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

import io.github.artemget.pootos.providers.ModelProvider;
import io.github.artemget.pootos.providers.OpenAiCompatible;
import io.github.artemget.pootos.providers.ProviderException;
import java.net.URI;
import java.net.http.HttpClient;
import org.cactoos.Scalar;
import org.cactoos.Text;
import org.cactoos.text.FormattedText;
import org.cactoos.text.UncheckedText;

/**
 * Builds a {@link ModelProvider} from a configured entry and an injected key.
 *
 * <p>The key is supplied already resolved at the boundary; this object never
 * reads and never stores a secret value. It only selects the transport by the
 * entry kind and wires the adapter.</p>
 *
 * @since 0.0.1
 */
public final class ModelProviderOf implements Scalar<ModelProvider> {

    /**
     * Configured entry.
     */
    private final ProviderEntry entry;

    /**
     * Resolved key value.
     */
    private final Text key;

    /**
     * HTTP client.
     */
    private final HttpClient client;

    /**
     * Ctor.
     *
     * @param entry Configured entry
     * @param key Resolved key value
     * @param client HTTP client
     */
    public ModelProviderOf(
        final ProviderEntry entry, final Text key, final HttpClient client
    ) {
        this.entry = entry;
        this.key = key;
        this.client = client;
    }

    @Override
    public ModelProvider value() throws ProviderException {
        if (!"openai".equals(new UncheckedText(this.entry.kind()).asString())) {
            throw new ProviderException(
                new UncheckedText(
                    new FormattedText(
                        "Unsupported provider kind: %s", this.entry.kind()
                    )
                ).asString(),
                new UnsupportedOperationException("Unknown provider kind")
            );
        }
        return new OpenAiCompatible(
            URI.create(new UncheckedText(this.entry.base()).asString()),
            this.entry.name(),
            this.entry.model(),
            () -> this.key,
            this.client
        );
    }
}
