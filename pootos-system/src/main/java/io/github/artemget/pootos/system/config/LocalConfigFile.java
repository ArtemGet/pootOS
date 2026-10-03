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

package io.github.artemget.pootos.system.config;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.cactoos.Text;
import org.cactoos.text.TextOf;
import org.cactoos.text.UncheckedText;

/**
 * A {@link ConfigFile} stored on the local filesystem.
 *
 * @since 0.0.1
 */
public final class LocalConfigFile implements ConfigFile {

    /**
     * File path.
     */
    private final Path path;

    /**
     * Ctor.
     *
     * @param path File path
     */
    public LocalConfigFile(final Path path) {
        this.path = path;
    }

    @Override
    public Text read() throws ConfigException {
        try {
            return new TextOf(Files.readString(this.path));
        } catch (final IOException err) {
            throw new ConfigException("Failed to read config file", err);
        }
    }

    @Override
    public void write(final Text data) throws ConfigException {
        try {
            Files.writeString(this.path, new UncheckedText(data).asString());
        } catch (final IOException err) {
            throw new ConfigException("Failed to write config file", err);
        }
    }
}
