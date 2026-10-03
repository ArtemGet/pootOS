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

package io.github.artemget.pootos.providers;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.cactoos.Text;
import org.cactoos.text.TextOf;
import org.cactoos.text.UncheckedText;

/**
 * A {@link ModelProvider} that speaks the OpenAI chat-completions protocol.
 *
 * <p>The endpoint, the provider name and the model are injected, so the same
 * adapter serves any OpenAI-compatible backend (OpenAI, a self-hosted gateway,
 * a local server). The API key is injected as a {@link SecretRef} and is only
 * read when the request is built; it is never stored or logged as a value.</p>
 *
 * @since 0.0.1
 */
public final class OpenAiCompatible implements ModelProvider {

    /**
     * Chat-completions endpoint.
     */
    private final URI endpoint;

    /**
     * Provider name.
     */
    private final Text provider;

    /**
     * Model name.
     */
    private final Text model;

    /**
     * Secret reference for the API key.
     */
    private final SecretRef key;

    /**
     * HTTP client.
     */
    private final HttpClient client;

    /**
     * Ctor.
     *
     * @param endpoint Chat-completions endpoint
     * @param provider Provider name
     * @param model Model name
     * @param key Secret reference for the API key
     * @param client HTTP client
     */
    public OpenAiCompatible(
        final URI endpoint,
        final Text provider,
        final Text model,
        final SecretRef key,
        final HttpClient client
    ) {
        this.endpoint = endpoint;
        this.provider = provider;
        this.model = model;
        this.key = key;
        this.client = client;
    }

    @Override
    public Text name() {
        return this.provider;
    }

    @Override
    public Text complete(final Text prompt) throws ProviderException {
        try {
            return new JsonAssistantText(
                new TextOf(
                    this.client.send(
                        HttpRequest.newBuilder(this.endpoint)
                            .header("Content-Type", "application/json").header(
                                "Authorization",
                                "Bearer ".concat(
                                    new UncheckedText(this.key.value()).asString()
                                )
                            ).POST(
                                HttpRequest.BodyPublishers.ofString(
                                    new UncheckedText(
                                        new JsonBody(this.model, prompt)
                                    ).asString()
                                )
                            ).build(),
                        HttpResponse.BodyHandlers.ofString()
                    ).body()
                )
            );
        } catch (final IOException failure) {
            throw new ProviderException("Failed to reach the model provider", failure);
        } catch (final InterruptedException failure) {
            Thread.currentThread().interrupt();
            throw new ProviderException("Interrupted calling the model provider", failure);
        }
    }
}
