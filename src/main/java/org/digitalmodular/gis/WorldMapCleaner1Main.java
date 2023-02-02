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

package org.digitalmodular.gis;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

/**
 * @author zom-b
 */
// Created 2023-01-30
public class WorldMapCleaner1Main {
	public static final int POLYGON_COLOR    = 0xFFFFFFFF;
	public static final int BACKGROUND_COLOR = 0xFF000000;

	public static final int SEA_COLOR    = 0xFF0A197E;
	public static final int BORDER_COLOR = 0xFF000000;
	public static final int LAND_COLOR   = 0xFF134DEF;

	public static void main(String... args) throws IOException {
		BufferedImage read = ImageIO.read(new File("/home/zom-b/Projects/Javascript/pixelywars/maps/grid1.png"));

		BufferedImage img = new BufferedImage(read.getWidth(), read.getHeight(), BufferedImage.TYPE_INT_RGB);

		for (int y = 0; y < img.getHeight(); y++) {
			for (int x = 0; x < img.getWidth(); x++) {
				if (read.getRGB(x, y) == POLYGON_COLOR) {
					img.setRGB(x, y, LAND_COLOR);
				} else if (isBorder(read, x, y)) {
					img.setRGB(x, y, BORDER_COLOR);
				} else {
					img.setRGB(x, y, SEA_COLOR);
				}
			}
		}

		ImageIO.write(img, "PNG", new File("/home/zom-b/Projects/Javascript/pixelywars/maps/grid1a.png"));
	}

	private static boolean isBorder(BufferedImage img, int x, int y) {
		int b = getPixel(img, x, y - 1);
		int d = getPixel(img, x - 1, y);
		int e = getPixel(img, x, y);
		int f = getPixel(img, x + 1, y);
		int h = getPixel(img, x, y + 1);

		if (e == BACKGROUND_COLOR) {
			if (b == POLYGON_COLOR ||
			    d == POLYGON_COLOR ||
			    f == POLYGON_COLOR ||
			    h == POLYGON_COLOR) {
				return true;
			}
		}

		return false;
	}

	private static int getPixel(BufferedImage img, int x, int y) {
		if (x < 0) {
			x = 0;
		} else if (x >= img.getWidth()) {
			x = img.getWidth() - 1;
		}

		if (y < 0) {
			y = 0;
		} else if (y >= img.getHeight()) {
			y = img.getHeight() - 1;
		}

		return img.getRGB(x, y);
	}
}
