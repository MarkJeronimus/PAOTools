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

import java.util.Objects;

/**
 * @author Mark Jeronimus
 */
// Created 2022-12-21
public final class PixelChange {
	private final int  x;
	private final int  y;
	private final int  color;
	private final int  id;
	private final long timestamp;

	public PixelChange(int x,
	            int y,
	            int color,
	            int id,
	            long timestamp) {
		this.x = x;
		this.y = y;
		this.color = color;
		this.id = id;
		this.timestamp = timestamp;
	}

	public int x() {
		return x;
	}

	public int y() {
		return y;
	}

	public int color() {
		return color;
	}

	public int id() {
		return id;
	}

	public long timestamp() {
		return timestamp;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		if (obj == null || obj.getClass() != this.getClass()) {
			return false;
		}
		PixelChange that = (PixelChange)obj;
		return this.x == that.x &&
		       this.y == that.y &&
		       this.color == that.color &&
		       this.id == that.id &&
		       this.timestamp == that.timestamp;
	}

	@Override
	public int hashCode() {
		return Objects.hash(x, y, color, id, timestamp);
	}

	@Override
	public String toString() {
		return "PixelChange[" +
		       "x=" + x + ", " +
		       "y=" + y + ", " +
		       "color=" + color + ", " +
		       "id=" + id + ", " +
		       "timestamp=" + timestamp + ']';
	}

}
