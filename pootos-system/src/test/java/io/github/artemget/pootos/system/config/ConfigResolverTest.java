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

import java.nio.file.Path;
import org.cactoos.text.TextOf;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Test case for {@link ConfigResolver}.
 *
 * @since 0.0.1
 */
final class ConfigResolverTest {

    @Test
    void readsWrittenValue(@TempDir final Path dir) throws Exception {
        final ConfigFile file = new ConfigDirectory(dir).file("app.conf");
        file.write(new TextOf("engine=local"));
        MatcherAssert.assertThat(
            "A written value must be read back by key",
            new FileConfigResolver(file).value("engine").asString(),
            Matchers.equalTo("local")
        );
    }

    @Test
    void returnsEmptyForUnknownKey(@TempDir final Path dir) throws Exception {
        final ConfigFile file = new ConfigDirectory(dir).file("app.conf");
        file.write(new TextOf("engine=local"));
        MatcherAssert.assertThat(
            "An unknown key must resolve to empty text",
            new FileConfigResolver(file).value("missing").asString(),
            Matchers.emptyString()
        );
    }

    @Test
    void failsWhenFileIsMissing(@TempDir final Path dir) {
        boolean failed = false;
        try {
            new FileConfigResolver(new ConfigDirectory(dir).file("absent.conf")).value("engine");
        } catch (final ConfigException err) {
            failed = true;
        }
        MatcherAssert.assertThat(
            "Reading a missing config file must fail fast",
            failed,
            Matchers.is(true)
        );
    }
}
