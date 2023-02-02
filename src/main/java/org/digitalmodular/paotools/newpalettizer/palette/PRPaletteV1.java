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

import java.awt.Color;

/**
 * @author Mark Jeronimus
 */
// Created 2020-10-30
public final class PRPaletteV1 extends Palette {
	private static final PaletteColor[] COLORS = {
			new PaletteColor(new Color(0x000000).getRGB(), "Black"),
			new PaletteColor(new Color(0x1A1C1E).getRGB(), "Gray 1"),
			new PaletteColor(new Color(0x4C4E4F).getRGB(), "Gray 2"),
			new PaletteColor(new Color(0x7E807E).getRGB(), "Gray 3"),
			new PaletteColor(new Color(0xAFAFB1).getRGB(), "Gray 4"),
			new PaletteColor(new Color(0xDEDCDA).getRGB(), "Gray 5"),
			new PaletteColor(new Color(0xFFFFFF).getRGB(), "White"),
			new PaletteColor(new Color(0x4A0610).getRGB(), "Red 1"),
			new PaletteColor(new Color(0x8F0D15).getRGB(), "Red 2"),
			new PaletteColor(new Color(0xF82327).getRGB(), "Red 3"),
			new PaletteColor(new Color(0xEF671A).getRGB(), "Orange 1"),
			new PaletteColor(new Color(0xFFA82A).getRGB(), "Orange 2"),
			new PaletteColor(new Color(0xF7ED05).getRGB(), "Yellow 1"),
			new PaletteColor(new Color(0xFDF996).getRGB(), "Yellow 2"),
			new PaletteColor(new Color(0xCC8774).getRGB(), "Skin 1"),
			new PaletteColor(new Color(0xF0B39F).getRGB(), "Skin 2"),
			new PaletteColor(new Color(0xF9DED4).getRGB(), "Skin 3"),
			new PaletteColor(new Color(0x3B261B).getRGB(), "Brown 1"),
			new PaletteColor(new Color(0x785332).getRGB(), "Brown 2"),
			new PaletteColor(new Color(0xC09667).getRGB(), "Brown 3"),
			new PaletteColor(new Color(0xE5C8A1).getRGB(), "Brown 4"),
			new PaletteColor(new Color(0x093C09).getRGB(), "Green 1"),
			new PaletteColor(new Color(0x277A0B).getRGB(), "Green 2"),
			new PaletteColor(new Color(0x5EC22A).getRGB(), "Green 3"),
			new PaletteColor(new Color(0x6DFC6D).getRGB(), "Green 4"),
			new PaletteColor(new Color(0x154048).getRGB(), "Cyan 1"),
			new PaletteColor(new Color(0x3D9692).getRGB(), "Cyan 2"),
			new PaletteColor(new Color(0x7EF5B6).getRGB(), "Cyan 3"),
			new PaletteColor(new Color(0x0A197E).getRGB(), "Blue 1"),
			new PaletteColor(new Color(0x134DEF).getRGB(), "Blue 2"),
			new PaletteColor(new Color(0x4691FF).getRGB(), "Blue 3"),
			new PaletteColor(new Color(0x6CD2FF).getRGB(), "Blue 4"),
			new PaletteColor(new Color(0x3A178E).getRGB(), "Purple 1"),
			new PaletteColor(new Color(0x8069BA).getRGB(), "Purple 2"),
			new PaletteColor(new Color(0xC2AFFF).getRGB(), "Purple 3"),
			new PaletteColor(new Color(0xE0DBFF).getRGB(), "Purple 4"),
			new PaletteColor(new Color(0x6E1653).getRGB(), "Magenta 1"),
			new PaletteColor(new Color(0xF45DCE).getRGB(), "Magenta 2"),
			new PaletteColor(new Color(0xFCB3FF).getRGB(), "Magenta 3"),
			new PaletteColor(new Color(0x46523A).getRGB(), "Camouflage"),
			};

	private static final Palette INSTANCE = new PRPaletteV1();

	private PRPaletteV1() {
		if (INSTANCE != null) {
			throw new AssertionError();
		}
	}

	public static Palette instance() {
		return INSTANCE;
	}

	@Override
	public int size() {
		return COLORS.length;
	}

	@Override
	public PaletteColor get(int index) {
		return COLORS[index];
	}
}
