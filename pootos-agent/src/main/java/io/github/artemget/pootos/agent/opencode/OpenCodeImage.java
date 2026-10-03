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

package io.github.artemget.pootos.agent.opencode;

import org.cactoos.Text;
import org.cactoos.text.TextOf;

/**
 * The container image an {@link OpenCodeExecutor} run happens in
 * (ADR-001, ADR-002).
 *
 * <p>It is a Cactoos {@link Text} with a pinned default, so the image name is
 * configurable without a getter or a public static constant: an injected value
 * wins, a blank one falls back to the pinned default that matches
 * {@code docker/agent/Dockerfile}.</p>
 *
 * @since 0.0.1
 */
public final class OpenCodeImage implements Text {

    /**
     * Configured image, or blank for the pinned default.
     */
    private final Text image;

    /**
     * Ctor.
     */
    public OpenCodeImage() {
        this(new TextOf(""));
    }

    /**
     * Ctor.
     *
     * @param image Configured image, blank for the pinned default
     */
    public OpenCodeImage(final Text image) {
        this.image = image;
    }

    @Override
    public String asString() throws Exception {
        final String value = this.image.asString();
        final String resolved;
        if (value.isBlank()) {
            resolved = "pootos/opencode:1.18.34";
        } else {
            resolved = value;
        }
        return resolved;
    }
}
