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

package org.digitalmodular.paotools;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.io.File;
import java.io.IOException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import javax.imageio.ImageIO;

import org.jetbrains.annotations.Nullable;

import org.digitalmodular.utilities.StringUtilities;
import org.digitalmodular.utilities.container.UnsignedInteger;
import org.digitalmodular.utilities.graphics.image.ImageUtilities;

/**
 * @author Mark Jeronimus
 */
// Created 2020-10-24
public class ColorCountMain {
	private static final String FILENAME = "PAO_Canvas_2020_10_24.png";

	private static final PaoColorCount[] COLORS = {
			new PaoColorCount(new Color(0xFFFFFF).getRGB(), "White"),
			new PaoColorCount(new Color(0xCBC6C1).getRGB(), "Gray 4"),
			new PaoColorCount(new Color(0x948F8C).getRGB(), "Gray 3"),
			new PaoColorCount(new Color(0x595555).getRGB(), "Gray 2"),
			new PaoColorCount(new Color(0x252529).getRGB(), "Gray 1"),
			new PaoColorCount(new Color(0x000000).getRGB(), "Black"),
			new PaoColorCount(new Color(0x5F0B1A).getRGB(), "Red 1"),
			new PaoColorCount(new Color(0x991118).getRGB(), "Red 2"),
			new PaoColorCount(new Color(0xFF0000).getRGB(), "Red 3"),
			new PaoColorCount(new Color(0xFF5F00).getRGB(), "Orange Red"),
			new PaoColorCount(new Color(0xFF9000).getRGB(), "Orange"),
			new PaoColorCount(new Color(0xFFC600).getRGB(), "Orange Yellow"),
			new PaoColorCount(new Color(0xFFFF35).getRGB(), "Yellow"),
			new PaoColorCount(new Color(0x43FE00).getRGB(), "Green 4"),
			new PaoColorCount(new Color(0x00D000).getRGB(), "Green 3"),
			new PaoColorCount(new Color(0x1A901A).getRGB(), "Green 2"),
			new PaoColorCount(new Color(0x115D25).getRGB(), "Green 1"),
			new PaoColorCount(new Color(0x00BB86).getRGB(), "Turquoise 1"),
			new PaoColorCount(new Color(0x4DFFD3).getRGB(), "Turquoise 2"),
			new PaoColorCount(new Color(0x00D2FF).getRGB(), "Cyan"),
			new PaoColorCount(new Color(0x598EF0).getRGB(), "Blue 4"),
			new PaoColorCount(new Color(0x2553FF).getRGB(), "Blue 3"),
			new PaoColorCount(new Color(0x0012E1).getRGB(), "Blue 2"),
			new PaoColorCount(new Color(0x001076).getRGB(), "Blue 1"),
			new PaoColorCount(new Color(0x5200FF).getRGB(), "Purple"),
			new PaoColorCount(new Color(0x8C6EC1).getRGB(), "Lavender 1"),
			new PaoColorCount(new Color(0xC5AAEB).getRGB(), "Lavender 2"),
			new PaoColorCount(new Color(0xEFD6FA).getRGB(), "Lavender 3"),
			new PaoColorCount(new Color(0xE587DE).getRGB(), "Pink"),
			new PaoColorCount(new Color(0xE233E2).getRGB(), "Magenta 2"),
			new PaoColorCount(new Color(0x690B69).getRGB(), "Magenta 1"),
			new PaoColorCount(new Color(0xA34500).getRGB(), "Dark Orange"),
			new PaoColorCount(new Color(0xFF7662).getRGB(), "Red 4"),
			new PaoColorCount(new Color(0xF3CDC7).getRGB(), "Red 5"),
			new PaoColorCount(new Color(0xFFDFB6).getRGB(), "Brown 4"),
			new PaoColorCount(new Color(0xE4B369).getRGB(), "Brown 3"),
			new PaoColorCount(new Color(0xA56638).getRGB(), "Brown 2"),
			new PaoColorCount(new Color(0x64341F).getRGB(), "Brown 1"),
			};

	public static void main(String... args) throws Exception {
		BufferedImage img = ImageIO.read(new File("/home/zom-b/Pictures/Pixelart/PAO/Canvases/" + FILENAME));

		img = ImageUtilities.toIntRasterImage(img);
		img.setAccelerationPriority(0);
		int[] pixels = ((DataBufferInt)img.getRaster().getDataBuffer()).getData();

		Map<Integer, PaoColorCount> counts = new HashMap<>(COLORS.length);
		for (PaoColorCount color : COLORS)
			counts.put(color.rgb, color);

		for (int c : pixels) {
			@Nullable PaoColorCount paoColorCount = counts.get(c & 0xFFFFFF);
			if (paoColorCount == null)
				throw new IOException("Image contains non-PAO color: " + UnsignedInteger.toHexString(c, 6));

			paoColorCount.count++;
		}

		System.out.println(FILENAME + ':');
		counts.values()
		      .stream()
		      .sorted(Comparator.comparingInt(PaoColorCount::getCount).reversed())
		      .forEach(System.out::println);
	}

	@SuppressWarnings("PackageVisibleField")
	private static final class PaoColorCount {
		final int    rgb;
		final String name;

		int count = 0;

		private PaoColorCount(int rgb, String name) {
			this.rgb = rgb & 0xFFFFFF;
			this.name = name;
		}

		public int getCount() {
			return count;
		}

		@Override
		public String toString() {
			return StringUtilities.padLeft(Integer.toString(count), 8) + ": #" +
			       UnsignedInteger.toHexString(rgb, 6) + " (" +
			       name + ')';
		}
	}
}
