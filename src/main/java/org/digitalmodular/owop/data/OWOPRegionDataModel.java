/*
 * This file is part of PAO.
 *
 * Copyleft 2022 Mark Jeronimus. All Rights Reversed.
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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.digitalmodular.owop.data;

import java.util.Collection;
import java.util.function.BiConsumer;

import net.jcip.annotations.ThreadSafe;
import org.jetbrains.annotations.Nullable;

import org.digitalmodular.utilities.container.CacheMap;

import static org.digitalmodular.owop.OWOPClientMain.REGION_SIZE;

/**
 * @author Mark Jeronimus
 */
// Created 2022-12-28
@ThreadSafe
public class OWOPRegionDataModel {
	private static final int CACHE_SIZE = 64;

	private final CacheMap<TileKey, Region> regionCache = new CacheMap<>(CACHE_SIZE);

	public synchronized void addElementUpdatedListener(BiConsumer<TileKey, Region> listener) {
		regionCache.addElementUpdatedListener(listener);
	}

	public synchronized void addElementRemovedListener(BiConsumer<TileKey, Region> listener) {
		regionCache.addElementRemovedListener(listener);
	}

	public synchronized void addRegion(TileKey key, Region region) {
		regionCache.put(key, region);
	}

	public synchronized @Nullable Region getRegion(TileKey key) {
		return regionCache.get(key);
	}

	public Collection<Region> regions() {
		return regionCache.values();
	}

	public boolean chunkInCache(int regionX, int regionY, int x, int y) {
		TileKey key = new TileKey(regionX, regionY, 0, REGION_SIZE);

		@Nullable Region region = regionCache.get(key);

		return region != null && region.isSubRegionPresent(x, y);
	}
}
