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

import io.github.artemget.pootos.providers.ProviderException;
import java.net.http.HttpClient;
import org.cactoos.text.TextOf;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Test case for {@link ModelProviderOf}.
 *
 * @since 0.0.1
 */
final class ModelProviderOfTest {

    @Test
    void buildsProviderFromEntry() throws Exception {
        MatcherAssert.assertThat(
            "A configured entry and key must yield a named provider",
            new ModelProviderOf(
                new RecordedProvider(
                    new TextOf("openai"),
                    new TextOf("openai"),
                    new TextOf("https://api.openai.com/v1/chat/completions"),
                    new TextOf("gpt-4o"),
                    new TextOf("env:OPENAI_KEY")
                ),
                new TextOf("sk-test"),
                HttpClient.newHttpClient()
            ).value().name().asString(),
            Matchers.equalTo("openai")
        );
    }

    @Test
    void rejectsUnknownKind() {
        MatcherAssert.assertThat(
            "An unsupported kind must raise a provider failure",
            Assertions.assertThrows(
                ProviderException.class,
                () -> new ModelProviderOf(
                    new RecordedProvider(
                        new TextOf("acme"),
                        new TextOf("acme"),
                        new TextOf("https://acme.example"),
                        new TextOf("acme-1"),
                        new TextOf("env:ACME_KEY")
                    ),
                    new TextOf("sk-test"),
                    HttpClient.newHttpClient()
                ).value()
            ),
            Matchers.instanceOf(ProviderException.class)
        );
    }
}
