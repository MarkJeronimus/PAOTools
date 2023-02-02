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

import java.awt.image.BufferedImage;

import net.jcip.annotations.NotThreadSafe;
import org.jetbrains.annotations.Nullable;

/**
 * A {@code Chunk} is a {@link Region} with added properties
 * ({@link #isLocked() locked} and {@link #getTimestamp() timestamp})
 *
 * @author Mark Jeronimus
 */
// Created 2022-12-13
@NotThreadSafe
public final class Chunk extends Region {
	public static final int CHUNK_SIZE = 16;

	private final boolean locked;
	private final long    timestamp;

	public Chunk(TileKey key, boolean locked) {
		super(key);
		this.locked = locked;
		timestamp = System.currentTimeMillis();
	}

	public Chunk(TileKey key, boolean locked, long timestamp) {
		super(key);
		this.locked = locked;
		this.timestamp = timestamp;
	}

	public Chunk(TileKey key, @Nullable BufferedImage image, boolean locked) {
		super(key, image);
		this.locked = locked;
		timestamp = System.currentTimeMillis();
	}

	public Chunk(TileKey key, @Nullable BufferedImage image, boolean locked, long timestamp) {
		super(key, image);
		this.locked = locked;
		this.timestamp = timestamp;
	}

	public boolean isLocked() {
		return locked;
	}

	public long getTimestamp() {
		return timestamp;
	}

	@Override
	public boolean equals(@Nullable Object o) {
		if (this == o)
			return true;

		if (o == null || getClass() != o.getClass())
			return false;

		Chunk other = (Chunk)o;
		return getX() == other.getX() &&
		       getY() == other.getY() &&
		       getZoom() == other.getZoom() &&
		       getSize() == other.getSize() &&
		       isLocked() == other.isLocked() &&
		       getTimestamp() == other.getTimestamp() &&
		       getImage().equals(other.getImage());
	}

	@Override
	public int hashCode() {
		int hashCode = 0x811C9DC5;
		hashCode = 0x01000193 * (hashCode ^ Integer.hashCode(getX()));
		hashCode = 0x01000193 * (hashCode ^ Integer.hashCode(getY()));
		hashCode = 0x01000193 * (hashCode ^ Integer.hashCode(getZoom()));
		hashCode = 0x01000193 * (hashCode ^ Integer.hashCode(getSize()));
		hashCode = 0x01000193 * (hashCode ^ getImage().hashCode());
		hashCode = 0x01000193 * (hashCode ^ Boolean.hashCode(isLocked()));
		hashCode = 0x01000193 * (hashCode ^ Long.hashCode(getTimestamp()));
		return hashCode;
	}
}
