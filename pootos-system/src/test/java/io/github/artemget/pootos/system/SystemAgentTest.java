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
import io.github.artemget.pootos.system.provider.ProviderConfig;
import io.github.artemget.pootos.system.provider.RecordedProvider;
import io.github.artemget.pootos.system.secret.DeclaredSecretForm;
import java.nio.file.Path;
import java.util.List;
import org.cactoos.text.TextOf;
import org.cactoos.text.UncheckedText;
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
    void accumulatesProviders(@TempDir final Path dir) throws Exception {
        final ConfigDirectory folder = new ConfigDirectory(dir);
        final SystemAgent agent = new ConfiguredSystemAgent(
            folder, new DeclaredSecretForm(List.of())
        );
        SystemAgentTest.record(agent, "openai", "openai", "gpt-4o");
        SystemAgentTest.record(agent, "anthropic", "anthropic", "claude-3");
        MatcherAssert.assertThat(
            "Defining two providers must keep both in the config file",
            new FileConfigResolver(folder.file("providers.conf")).value("providers").asString(),
            Matchers.equalTo("openai,anthropic")
        );
    }

    @Test
    void redefinesProvider(@TempDir final Path dir) throws Exception {
        final SystemAgent agent = new ConfiguredSystemAgent(
            new ConfigDirectory(dir), new DeclaredSecretForm(List.of())
        );
        SystemAgentTest.record(agent, "openai", "openai", "gpt-4o");
        SystemAgentTest.record(agent, "anthropic", "anthropic", "claude-3");
        SystemAgentTest.record(agent, "openai", "openai", "gpt-5");
        MatcherAssert.assertThat(
            "Redefining a name must replace only that entry",
            agent.providers().stream()
                .map(entry -> new UncheckedText(entry.model()).asString())
                .toList(),
            Matchers.contains("gpt-5", "claude-3")
        );
    }

    @Test
    void listsConfiguredProviders(@TempDir final Path dir) throws Exception {
        final SystemAgent agent = new ConfiguredSystemAgent(
            new ConfigDirectory(dir), new DeclaredSecretForm(List.of())
        );
        SystemAgentTest.record(agent, "openai", "openai", "gpt-4o");
        MatcherAssert.assertThat(
            "Listing must expose every recorded entry field",
            new ProviderConfig(agent.providers()).asString(),
            Matchers.allOf(
                Matchers.containsString("providers=openai"),
                Matchers.containsString("openai.base=https://api.openai.com"),
                Matchers.containsString("openai.model=gpt-4o"),
                Matchers.containsString("openai.key=env:OPENAI_KEY")
            )
        );
    }

    @Test
    void listsNothingWhenUnset(@TempDir final Path dir) throws Exception {
        MatcherAssert.assertThat(
            "An absent config must list no providers",
            new ConfiguredSystemAgent(
                new ConfigDirectory(dir), new DeclaredSecretForm(List.of())
            ).providers(),
            Matchers.empty()
        );
    }

    private static void record(
        final SystemAgent agent, final String name, final String kind, final String model
    ) throws Exception {
        agent.defineProvider(
            new RecordedProvider(
                new TextOf(name),
                new TextOf(kind),
                new TextOf("https://api.openai.com"),
                new TextOf(model),
                new TextOf("env:OPENAI_KEY")
            )
        );
    }
}
