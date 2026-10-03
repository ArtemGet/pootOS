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
import java.nio.file.FileSystem;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * A {@link ConfigWatcher} built on {@link WatchService}.
 *
 * <p>The watch is hot: when a change appears within the timeout the
 * reload action runs in place, so no restart is needed.</p>
 *
 * @since 0.0.1
 */
public final class FileConfigWatcher implements ConfigWatcher {

    /**
     * Filesystem to watch through.
     */
    private final FileSystem files;

    /**
     * Ctor.
     *
     * @param files Filesystem to watch through
     */
    public FileConfigWatcher(final FileSystem files) {
        this.files = files;
    }

    @Override
    public void watch(
        final ConfigDirectory directory, final Runnable reload, final Duration timeout
    ) throws ConfigException {
        try (WatchService service = this.files.newWatchService()) {
            directory.register(service);
            final WatchKey key = service.poll(timeout.toMillis(), TimeUnit.MILLISECONDS);
            if (key != null) {
                reload.run();
            }
        } catch (final IOException err) {
            throw new ConfigException("Failed to watch config directory", err);
        } catch (final InterruptedException err) {
            Thread.currentThread().interrupt();
            throw new ConfigException("Interrupted while watching config", err);
        }
    }
}
