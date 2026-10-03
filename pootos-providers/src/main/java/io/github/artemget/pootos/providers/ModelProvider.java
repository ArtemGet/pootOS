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

import org.cactoos.Text;

/**
 * An LLM provider the agent runtime can call for a completion (R3).
 *
 * <p>An implementation is a swappable adapter behind a short SPI, so an agent
 * can be pinned to a provider through its {@code ModelRef} without knowing the
 * transport. Secrets never cross this contract: a key is supplied at
 * construction as a {@link SecretRef}.</p>
 *
 * @since 0.0.1
 */
public interface ModelProvider {

    /**
     * Provider name used to reference it from a {@code ModelRef}.
     *
     * @return Provider identity
     */
    Text name();

    /**
     * Complete the given prompt and return the assistant text.
     *
     * @param prompt Text to send
     * @return Assistant text
     * @throws ProviderException If the provider could not be reached or replied
     */
    Text complete(Text prompt) throws ProviderException;
}
