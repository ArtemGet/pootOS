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

package io.github.artemget.pootos.system;

import io.github.artemget.pootos.system.config.ConfigException;
import io.github.artemget.pootos.system.secret.SecretForm;
import org.cactoos.Text;

/**
 * Configures the runtime without a restart, writing plain data only.
 *
 * <p>The agent never reads or writes secret values: it exposes the
 * {@link SecretForm} a human must fill, and writes non-secret config
 * files for providers.</p>
 *
 * @since 0.0.1
 */
public interface SystemAgent {

    /**
     * Secrets a human must supply.
     *
     * @return Secret form
     */
    SecretForm secretForm();

    /**
     * Record a provider endpoint in the config file.
     *
     * @param name Provider name
     * @param endpoint Provider endpoint
     * @throws ConfigException When the config file cannot be written
     */
    void defineProvider(Text name, Text endpoint) throws ConfigException;

    /**
     * Read a recorded provider endpoint.
     *
     * @param name Provider name
     * @return Provider endpoint, empty when absent
     * @throws ConfigException When the config file cannot be read
     */
    Text provider(Text name) throws ConfigException;
}
