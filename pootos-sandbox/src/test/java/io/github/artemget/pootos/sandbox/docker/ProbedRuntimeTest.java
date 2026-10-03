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

package io.github.artemget.pootos.sandbox.docker;

import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

/**
 * Test case for {@link ProbedRuntime}.
 *
 * @since 0.0.1
 */
final class ProbedRuntimeTest {

    @Test
    void prefersGvisorWhenAvailable() throws Exception {
        MatcherAssert.assertThat(
            "An available gVisor runtime must be selected",
            new ProbedRuntime(
                new GvisorRuntime(), new HardenedRuntime(), () -> true
            ).option().asString(),
            Matchers.containsString("runsc")
        );
    }

    @Test
    void fallsBackWhenUnavailable() throws Exception {
        MatcherAssert.assertThat(
            "A missing gVisor runtime must degrade explicitly",
            new ProbedRuntime(
                new GvisorRuntime(), new HardenedRuntime(), () -> false
            ).status().asString(),
            Matchers.containsString("unavailable")
        );
    }

    @Test
    void dropsRuntimeSelectorOnFallback() throws Exception {
        MatcherAssert.assertThat(
            "The degraded runtime must not pass a selector",
            new ProbedRuntime(
                new GvisorRuntime(), new HardenedRuntime(), () -> false
            ).option().asString(),
            Matchers.equalTo("")
        );
    }
}
