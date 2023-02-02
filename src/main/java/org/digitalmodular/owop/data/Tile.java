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

/**
 * @author Mark Jeronimus
 */
// Created 2022-12-23
public interface Tile {
	/**
	 * The x coordinate in world space of the upper-left corener of the tile.
	 **/
	int getX();

	/**
	 * The y coordinate in world space of the upper-left corener of the tile.
	 **/
	int getY();

	/**
	 * The zoom level of the tile.
	 * <p>
	 * Zoom is an exponential quantity:
	 * <ul><li>{@code 0} is 1:1</li>
	 * <li>Positive is zoomed in by a factor of 2<sup>zoom</sup></li>
	 * <li>Negative is zoomed out by a factor of 2<sup>-zoom</sup></li></ul>
	 **/
	int getZoom();

	/**
	 * The size, in pixels, of the square tile.
	 **/
	int getSize();

	/**
	 * Create an object that contains all of the information, and only the information, that this interface exposes.
	 * <p>
	 * This is useful for using as a key in, for example, {@link Map Maps},
	 * where equality should not depend on state specific to sub-classes.
	 */
	TileKey makeKey();
}
