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
package org.jackhuang.hmcl.download;

import org.jackhuang.hmcl.game.GameComponentType;
import org.jackhuang.hmcl.setting.DownloadSource;
import org.jackhuang.hmcl.task.GetTask;
import org.jackhuang.hmcl.task.Task;
import org.jackhuang.hmcl.util.RegionUtils;
import org.jackhuang.hmcl.util.versioning.GameVersionNumber;
import org.jetbrains.annotations.NotNullByDefault;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

/// Checks loader source selection and fallback without executing HTTP tasks.
@NotNullByDefault
public final class HMCLDownloadProviderTest {

    /// Restores mainland fallbacks while preserving explicit mirror choice and loader-specific priority.
    @ParameterizedTest
    @CsvSource({
            "FORGE, DEFAULT", "FORGE, OFFICIAL", "FORGE, MIRROR",
            "NEO_FORGE, DEFAULT", "NEO_FORGE, OFFICIAL", "NEO_FORGE, MIRROR"
    })
    public void testLoaderSources(GameComponentType type, DownloadSource source) {
        HMCLDownloadProvider provider = new HMCLDownloadProvider("https://mirror.invalid");
        provider.setVersionListSource(source);
        Task<?> task = provider.fetchVersionsAsync(type, GameVersionNumber.asGameVersion("1.21.1"));
        boolean mainland = RegionUtils.isIsChinaMainlandMachine();
        boolean fallback = mainland || source == DownloadSource.MIRROR;
        assertEquals(!fallback, task.isRelyingOnDependents(), "A fallback must tolerate primary-source failure");

        // Follow the primary task chain to inspect the request selected before any network I/O.
        Task<?> primary = task;
        while (!(primary instanceof GetTask)) {
            assertEquals(1, primary.getDependents().size());
            primary = primary.getDependents().iterator().next();
        }
        boolean mirrorFirst = source == DownloadSource.MIRROR
                || mainland && type == GameComponentType.NEO_FORGE && source == DownloadSource.DEFAULT;
        assertEquals(mirrorFirst, primary.getName().startsWith("https://mirror.invalid/"));
    }
}
