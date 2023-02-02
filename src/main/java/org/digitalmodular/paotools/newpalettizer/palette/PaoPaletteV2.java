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
public final class PaoPaletteV2 extends Palette {
	private static final PaletteColor[] COLORS = {
			new PaletteColor(new Color(0xFFFFFF).getRGB(), "White"),
			new PaletteColor(new Color(0xCBC6C1).getRGB(), "Gray 4"),
			new PaletteColor(new Color(0x948F8C).getRGB(), "Gray 3"),
			new PaletteColor(new Color(0x595555).getRGB(), "Gray 2"),
			new PaletteColor(new Color(0x252529).getRGB(), "Gray 1"),
			new PaletteColor(new Color(0x000000).getRGB(), "Black"),
			new PaletteColor(new Color(0x5F0B1A).getRGB(), "Red 1"),
			new PaletteColor(new Color(0x991118).getRGB(), "Red 2"),
			new PaletteColor(new Color(0xFF0000).getRGB(), "Red 3"),
			new PaletteColor(new Color(0xFF5F00).getRGB(), "Red-Orange"),
			new PaletteColor(new Color(0xFF9000).getRGB(), "Orange"),
			new PaletteColor(new Color(0xFFC600).getRGB(), "Yellow-Orange"),
			new PaletteColor(new Color(0xFFFF35).getRGB(), "Yellow"),
			new PaletteColor(new Color(0x43FE00).getRGB(), "Green 4"),
			new PaletteColor(new Color(0x00D000).getRGB(), "Green 3"),
			new PaletteColor(new Color(0x1A901A).getRGB(), "Green 2"),
			new PaletteColor(new Color(0x115D25).getRGB(), "Green 1"),
			new PaletteColor(new Color(0x00BB86).getRGB(), "Turquoise 1"),
			new PaletteColor(new Color(0x4DFFD3).getRGB(), "Turquoise 2"),
			new PaletteColor(new Color(0x00D2FF).getRGB(), "Cyan"),
			new PaletteColor(new Color(0x598EF0).getRGB(), "Blue 4"),
			new PaletteColor(new Color(0x2553FF).getRGB(), "Blue 3"),
			new PaletteColor(new Color(0x0012E1).getRGB(), "Blue 2"),
			new PaletteColor(new Color(0x001076).getRGB(), "Blue 1"),
			new PaletteColor(new Color(0x5200FF).getRGB(), "Purple"),
			new PaletteColor(new Color(0x8C6EC1).getRGB(), "Lavender 1"),
			new PaletteColor(new Color(0xC5AAEB).getRGB(), "Lavender 2"),
			new PaletteColor(new Color(0xEFD6FA).getRGB(), "Lavender 3"),
			new PaletteColor(new Color(0xE587DE).getRGB(), "Magenta 3"),
			new PaletteColor(new Color(0xE233E2).getRGB(), "Magenta 2"),
			new PaletteColor(new Color(0x690B69).getRGB(), "Magenta 1"),
			new PaletteColor(new Color(0xA34500).getRGB(), "Saturated Brown"),
			new PaletteColor(new Color(0xFF7662).getRGB(), "Salmon"),
			new PaletteColor(new Color(0xF3CDC7).getRGB(), "Pink"),
			new PaletteColor(new Color(0xFFDFB6).getRGB(), "Brown 4"),
			new PaletteColor(new Color(0xE4B369).getRGB(), "Brown 3"),
			new PaletteColor(new Color(0xA56638).getRGB(), "Brown 2"),
			new PaletteColor(new Color(0x64341F).getRGB(), "Brown 1"),
			};

	private static final Palette INSTANCE = new PaoPaletteV2();

	private PaoPaletteV2() {throw new AssertionError();
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
