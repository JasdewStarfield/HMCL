/*
 * Hello Minecraft! Launcher
 * Copyright (C) 2026 huangyuhui <huanghongxun2008@126.com> and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.jackhuang.hmcl.util;

import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;

/// Verifies that download access ignores cached region restrictions and skips IP lookup.
@NotNullByDefault
public final class RegionUtilsTest {

    /// Covers fresh, mainland, and non-mainland cache states without issuing a network request.
    @ParameterizedTest
    @NullSource
    @ValueSource(booleans = {true, false})
    public void testDownloadsRemainAvailable(@Nullable Boolean cachedRegion) throws ReflectiveOperationException {
        Field cache = RegionUtils.class.getDeclaredField("isChinaMainlandNetwork");
        cache.setAccessible(true);
        @Nullable Object previous = cache.get(null);
        try {
            cache.set(null, cachedRegion);
            assertFalse(RegionUtils.isIsChinaMainlandNetwork());
            if (cachedRegion == null)
                assertNull(cache.get(null), "Disabled restrictions must leave the IP lookup cache untouched");
        } finally {
            cache.set(null, previous);
        }
    }
}
