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

import io.github.artemget.pootos.system.config.ConfigDirectory;
import io.github.artemget.pootos.system.config.ConfigException;
import io.github.artemget.pootos.system.config.ConfigFile;
import io.github.artemget.pootos.system.config.FileConfigResolver;
import io.github.artemget.pootos.system.secret.SecretForm;
import org.cactoos.Text;
import org.cactoos.text.FormattedText;
import org.cactoos.text.UncheckedText;

/**
 * A {@link SystemAgent} that stores provider endpoints in a config file.
 *
 * <p>Only plain data is written: a provider name and its endpoint. No
 * secret value ever passes through this object.</p>
 *
 * @since 0.0.1
 */
public final class ConfiguredSystemAgent implements SystemAgent {

    /**
     * Configuration directory.
     */
    private final ConfigDirectory directory;

    /**
     * Declared secret form.
     */
    private final SecretForm form;

    /**
     * Ctor.
     *
     * @param directory Configuration directory
     * @param form Declared secret form
     */
    public ConfiguredSystemAgent(final ConfigDirectory directory, final SecretForm form) {
        this.directory = directory;
        this.form = form;
    }

    @Override
    public SecretForm secretForm() {
        return this.form;
    }

    @Override
    public void defineProvider(final Text name, final Text endpoint) throws ConfigException {
        this.config().write(new FormattedText("%s=%s", name, endpoint));
    }

    @Override
    public Text provider(final Text name) throws ConfigException {
        return new FileConfigResolver(this.config()).value(new UncheckedText(name).asString());
    }

    private ConfigFile config() {
        return this.directory.file("providers.conf");
    }
}
