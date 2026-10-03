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

package io.github.artemget.pootos.kernel.budget;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

/**
 * Test case for {@link WallClockBudget}.
 *
 * @since 0.0.1
 */
final class BudgetTest {

    @Test
    void staysWithinWindow() {
        final Instant start = Instant.parse("2026-01-01T00:00:00Z");
        MatcherAssert.assertThat(
            "A budget must not be exceeded inside its window",
            new WallClockBudget(Duration.ofMinutes(1)).exceeded(
                start,
                Clock.fixed(start, ZoneOffset.UTC)
            ),
            Matchers.is(false)
        );
    }

    @Test
    void exceedsAfterWindow() {
        final Instant start = Instant.parse("2026-01-01T00:00:00Z");
        MatcherAssert.assertThat(
            "A budget must be exceeded after its window",
            new WallClockBudget(Duration.ofMinutes(1)).exceeded(
                start,
                Clock.fixed(start.plus(Duration.ofMinutes(2)), ZoneOffset.UTC)
            ),
            Matchers.is(true)
        );
    }
}
