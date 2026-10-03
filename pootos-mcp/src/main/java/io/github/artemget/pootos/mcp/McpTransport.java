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

package io.github.artemget.pootos.mcp;

import org.cactoos.Text;

/**
 * A carrier that exchanges one MCP frame with a server (stdio, HTTP, fake).
 *
 * <p>The protocol layer never knows how the bytes travel: a real adapter talks
 * to a child process or an HTTP endpoint, a test supplies a canned reply. Both
 * requests and responses stay as {@link Text} so no JSON library leaks in.</p>
 *
 * @since 0.0.1
 */
@FunctionalInterface
public interface McpTransport {

    /**
     * Send a request frame and return the response frame.
     *
     * @param request Request frame
     * @return Response frame
     * @throws McpException If the frame could not be exchanged
     */
    Text send(Text request) throws McpException;
}
