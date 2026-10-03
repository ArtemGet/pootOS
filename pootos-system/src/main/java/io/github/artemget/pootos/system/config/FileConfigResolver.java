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

import org.cactoos.Text;
import org.cactoos.text.TextOf;
import org.cactoos.text.UncheckedText;

/**
 * A {@link ConfigResolver} that reads {@code key=value} lines from a file.
 *
 * <p>Every call re-reads the file, so a change written by the System
 * Agent is visible immediately without a restart.</p>
 *
 * @since 0.0.1
 */
public final class FileConfigResolver implements ConfigResolver {

    /**
     * Configuration file.
     */
    private final ConfigFile file;

    /**
     * Ctor.
     *
     * @param file Configuration file
     */
    public FileConfigResolver(final ConfigFile file) {
        this.file = file;
    }

    @Override
    public Text value(final String key) throws ConfigException {
        final String content = new UncheckedText(this.file.read()).asString();
        Text found = new TextOf("");
        for (final String line : content.lines().toList()) {
            final String[] parts = line.split("=", 2);
            if (parts.length == 2 && parts[0].trim().equals(key)) {
                found = new TextOf(parts[1].trim());
            }
        }
        return found;
    }
}
