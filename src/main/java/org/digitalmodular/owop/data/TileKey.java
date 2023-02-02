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

import java.util.Map;

import org.jetbrains.annotations.Nullable;

/**
 * Can be used as a super-class for all concrete tile implementations, or to pass just the tile properties around,
 * or stand-alone container for a sort-of tile 'address', which can be used as a key for a {@link Map}.
 *
 * @author Mark Jeronimus
 */
// Created 2022-12-24
public final class TileKey {
	private final int x;
	private final int y;
	private final int zoom;
	private final int size;

	public TileKey(int x, int y, int zoom, int size) {
		this.x = x;
		this.y = y;
		this.zoom = zoom;
		this.size = size;
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}

	public int getZoom() {
		return zoom;
	}

	public int getSize() {
		return size;
	}

	@Override
	public boolean equals(@Nullable Object o) {
		if (this == o) {
			return true;
		}

		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		TileKey other = (TileKey)o;
		return x == other.x &&
		       y == other.y &&
		       zoom == other.zoom &&
		       size == other.size;
	}

	@Override
	public int hashCode() {
		int hashCode = 0x811C9DC5;
		hashCode = 0x01000193 * (hashCode ^ Integer.hashCode(x));
		hashCode = 0x01000193 * (hashCode ^ Integer.hashCode(y));
		hashCode = 0x01000193 * (hashCode ^ Integer.hashCode(zoom));
		hashCode = 0x01000193 * (hashCode ^ Integer.hashCode(size));
		return hashCode;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "[(" + x + ", " + y + "), zoom=" + zoom + ", size=" + size + ']';
	}

	public String toStringShort() {
		return "(" + x + ", " + y + "), zoom=" + zoom + ", size=" + size;
	}
}
