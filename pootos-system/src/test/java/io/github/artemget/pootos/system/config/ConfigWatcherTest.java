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

import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicBoolean;
import org.cactoos.text.TextOf;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Test case for {@link ConfigWatcher}.
 *
 * @since 0.0.1
 */
final class ConfigWatcherTest {

    @Test
    void picksUpChange(@TempDir final Path dir) throws Exception {
        final ConfigDirectory folder = new ConfigDirectory(dir);
        final ConfigFile file = folder.file("app.conf");
        file.write(new TextOf("engine=local"));
        final AtomicBoolean changed = new AtomicBoolean(false);
        final Thread writer = new Thread(
            () -> {
                try {
                    Thread.sleep(200L);
                    file.write(new TextOf("engine=remote"));
                } catch (final InterruptedException err) {
                    Thread.currentThread().interrupt();
                } catch (final ConfigException err) {
                    throw new IllegalStateException(err);
                }
            }
        );
        writer.start();
        new FileConfigWatcher(FileSystems.getDefault()).watch(
            folder, () -> changed.set(true), Duration.ofSeconds(10L)
        );
        writer.join();
        MatcherAssert.assertThat(
            "A change to the file must be picked up by the watcher",
            changed.get(),
            Matchers.is(true)
        );
    }

    @Test
    void givesUpAfterTimeout(@TempDir final Path dir) {
        final AtomicBoolean changed = new AtomicBoolean(false);
        boolean failed = false;
        try {
            new FileConfigWatcher(FileSystems.getDefault()).watch(
                new ConfigDirectory(dir), () -> changed.set(true), Duration.ofMillis(1L)
            );
        } catch (final ConfigException err) {
            failed = true;
        }
        MatcherAssert.assertThat(
            "A watch without changes must return without reloading",
            failed || changed.get(),
            Matchers.is(false)
        );
    }
}
