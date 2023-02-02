/*
 * This file is part of PAO.
 *
 * Copyleft 2023 Mark Jeronimus. All Rights Reversed.
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

package org.digitalmodular.paotools.newpalettizer.palette;

/**
 * @author Mark Jeronimus
 */
// Created 2020-10-30
public abstract class Palette {
	public abstract int size();

	public abstract PaletteColor get(int index);

	public int getIndexOfColor(int rgb) {
		rgb &= 0xFFFFFF;

		int size = size();
		for (int i = 0; i < size; i++) {
			if (get(i).getRGB() == rgb) {
				return i;
			}
		}

		return -1;
	}
}
