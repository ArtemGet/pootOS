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
import io.github.artemget.pootos.system.config.FileConfigResolver;
import io.github.artemget.pootos.system.secret.DeclaredSecretForm;
import java.nio.file.Path;
import java.util.List;
import org.cactoos.text.TextOf;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Test case for {@link SystemAgent}.
 *
 * @since 0.0.1
 */
final class SystemAgentTest {

    @Test
    void writesProviderConfig(@TempDir final Path dir) throws Exception {
        final ConfigDirectory folder = new ConfigDirectory(dir);
        new ConfiguredSystemAgent(
            folder, new DeclaredSecretForm(List.of())
        ).defineProvider(new TextOf("openai"), new TextOf("https://api.openai.com"));
        MatcherAssert.assertThat(
            "A defined provider must be readable from the config file",
            new FileConfigResolver(folder.file("providers.conf")).value("openai").asString(),
            Matchers.equalTo("https://api.openai.com")
        );
    }

    @Test
    void readsProviderConfig(@TempDir final Path dir) throws Exception {
        final ConfigDirectory folder = new ConfigDirectory(dir);
        folder.file("providers.conf").write(new TextOf("openai=https://api.openai.com"));
        MatcherAssert.assertThat(
            "A stored provider endpoint must be read back",
            new ConfiguredSystemAgent(folder, new DeclaredSecretForm(List.of()))
                .provider(new TextOf("openai")).asString(),
            Matchers.equalTo("https://api.openai.com")
        );
    }

    @Test
    void exposesSecretForm(@TempDir final Path dir) {
        MatcherAssert.assertThat(
            "A system agent must expose the secrets a human must supply",
            new ConfiguredSystemAgent(new ConfigDirectory(dir), new DeclaredSecretForm(List.of()))
                .secretForm().fields(),
            Matchers.empty()
        );
    }
}
