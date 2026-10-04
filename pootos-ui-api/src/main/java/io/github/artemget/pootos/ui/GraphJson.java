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

package io.github.artemget.pootos.ui;

import io.github.artemget.pootos.context.graph.GraphView;
import io.github.artemget.pootos.context.store.GraphStore;
import io.github.artemget.pootos.context.store.StoreException;
import org.cactoos.Text;
import org.cactoos.iterable.Mapped;
import org.cactoos.text.FormattedText;
import org.cactoos.text.Joined;
import org.cactoos.text.TextOf;
import org.cactoos.text.UncheckedText;

/**
 * The whole stored graph rendered as a JSON object.
 *
 * <p>The shape is the contract consumed by the web app: a {@code nodes} array
 * of {@code {id, kind, json}} and an {@code edges} array of
 * {@code {from, relation, to}}. A store read failure propagates as a checked
 * {@link StoreException} so the boundary can answer with an error status.</p>
 *
 * @since 0.0.1
 */
public final class GraphJson implements Text {

    /**
     * Store to read.
     */
    private final GraphStore store;

    /**
     * Ctor.
     *
     * @param store Store to read
     */
    public GraphJson(final GraphStore store) {
        this.store = store;
    }

    @Override
    public String asString() throws StoreException {
        final GraphView graph = this.store.load();
        return new UncheckedText(
            new FormattedText(
                "{\"nodes\":[%s],\"edges\":[%s]}",
                new Joined(
                    new TextOf(","),
                    new Mapped<Text>(
                        node -> new FormattedText(
                            "{\"id\":\"%s\",\"kind\":\"%s\",\"json\":%s}",
                            node.id().asString(),
                            new NodeKind(node.json()).asString(),
                            node.json()
                        ),
                        graph.nodes()
                    )
                ),
                new Joined(
                    new TextOf(","),
                    new Mapped<Text>(
                        edge -> new FormattedText(
                            "{\"from\":\"%s\",\"relation\":\"%s\",\"to\":\"%s\"}",
                            edge.from().asString(),
                            edge.relation().name(),
                            edge.to().asString()
                        ),
                        graph.edges()
                    )
                )
            )
        ).asString();
    }
}
