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

package io.github.artemget.pootos.system.provider;

import java.util.ArrayList;
import java.util.Collection;
import org.cactoos.Text;
import org.cactoos.text.FormattedText;
import org.cactoos.text.Joined;
import org.cactoos.text.TextOf;

/**
 * A collection of {@link ProviderEntry} rendered as {@code key=value} lines.
 *
 * <p>The first line is an index of provider names; every entry then
 * contributes its kind, base URL, model and secret reference name. Only
 * non-secret data is rendered.</p>
 *
 * @since 0.0.1
 */
public final class ProviderConfig implements Text {

    /**
     * Entries to render.
     */
    private final Collection<ProviderEntry> entries;

    /**
     * Ctor.
     *
     * @param entries Entries to render
     */
    public ProviderConfig(final Collection<ProviderEntry> entries) {
        this.entries = entries;
    }

    @Override
    public String asString() throws Exception {
        final Collection<Text> lines = new ArrayList<>(1 + (4 * this.entries.size()));
        lines.add(
            new FormattedText(
                "providers=%s", new Joined(new TextOf(","), this.names())
            )
        );
        for (final ProviderEntry entry : this.entries) {
            lines.add(new FormattedText("%s.kind=%s", entry.name(), entry.kind()));
            lines.add(new FormattedText("%s.base=%s", entry.name(), entry.base()));
            lines.add(new FormattedText("%s.model=%s", entry.name(), entry.model()));
            lines.add(new FormattedText("%s.key=%s", entry.name(), entry.key()));
        }
        return new Joined(
            new TextOf(System.lineSeparator()), lines
        ).asString();
    }

    private Collection<Text> names() {
        final Collection<Text> result = new ArrayList<>(this.entries.size());
        for (final ProviderEntry entry : this.entries) {
            result.add(entry.name());
        }
        return result;
    }
}
