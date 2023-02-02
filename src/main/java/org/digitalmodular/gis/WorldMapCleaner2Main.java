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

import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;

/**
 * @author zom-b
 */
// Created 2023-01-30
public class WorldMapCleaner2Main {
	public static final  int SEA_COLOR             = 0xFF0A197E;
	public static final  int BORDER_COLOR          = 0xFF000000;
	public static final  int LAND_COLOR            = 0xFF134DEF;
	public static final  int CONSUMED_BORDER_COLOR = 0xFF101010;
	private static final int OUT_OF_BOUNDS         = CONSUMED_BORDER_COLOR;
	public static final  int NEW_LAND_COLOR        = 0xFF0F38C1;

	public static void main(String... args) throws IOException {
		BufferedImage img = ImageIO.read(new File("/home/zom-b/Projects/Javascript/pixelywars/maps/grid1a.png"));

		for (int y = 0; y < img.getHeight(); y++) {
			for (int x = 0; x < img.getWidth(); x++) {
				if (img.getRGB(x, y) == BORDER_COLOR) {
					findChain(img, x, y);
				}
			}
		}

		ImageIO.write(img, "PNG", new File("/home/zom-b/Projects/Javascript/pixelywars/maps/grid1b.png"));
	}

	private static void findChain(BufferedImage img, int x, int y) {
		List<Point> chain = new ArrayList<>(100);

		walkChain(img, chain, x, y);

		if (!chainBordersSea(img, chain)) {
			setLand(img, chain);
		}
	}

	private static void walkChain(BufferedImage img, List<Point> chain, int x, int y) {
		img.setRGB(x, y, CONSUMED_BORDER_COLOR);
		chain.add(new Point(x, y));
		int i = 0;

		while (i < chain.size()) {
			Point point = chain.get(i);

			if (getPixel(img, point.x - 1, point.y - 1) == BORDER_COLOR) {
				img.setRGB(point.x - 1, point.y - 1, CONSUMED_BORDER_COLOR);
				chain.add(new Point(point.x - 1, point.y - 1));
			}
			if (getPixel(img, point.x, point.y - 1) == BORDER_COLOR) {
				img.setRGB(point.x, point.y - 1, CONSUMED_BORDER_COLOR);
				chain.add(new Point(point.x, point.y - 1));
			}
			if (getPixel(img, point.x + 1, point.y - 1) == BORDER_COLOR) {
				img.setRGB(point.x + 1, point.y - 1, CONSUMED_BORDER_COLOR);
				chain.add(new Point(point.x + 1, point.y - 1));
			}
			if (getPixel(img, point.x - 1, point.y) == BORDER_COLOR) {
				img.setRGB(point.x - 1, point.y, CONSUMED_BORDER_COLOR);
				chain.add(new Point(point.x - 1, point.y));
			}
			if (getPixel(img, point.x + 1, point.y) == BORDER_COLOR) {
				img.setRGB(point.x + 1, point.y, CONSUMED_BORDER_COLOR);
				chain.add(new Point(point.x + 1, point.y));
			}
			if (getPixel(img, point.x - 1, point.y + 1) == BORDER_COLOR) {
				img.setRGB(point.x - 1, point.y + 1, CONSUMED_BORDER_COLOR);
				chain.add(new Point(point.x - 1, point.y + 1));
			}
			if (getPixel(img, point.x, point.y + 1) == BORDER_COLOR) {
				img.setRGB(point.x, point.y + 1, CONSUMED_BORDER_COLOR);
				chain.add(new Point(point.x, point.y + 1));
			}
			if (getPixel(img, point.x + 1, point.y + 1) == BORDER_COLOR) {
				img.setRGB(point.x + 1, point.y + 1, CONSUMED_BORDER_COLOR);
				chain.add(new Point(point.x + 1, point.y + 1));
			}

			i++;
		}

	}

	private static boolean chainBordersSea(BufferedImage img, List<Point> chain) {
		for (Point point : chain) {
			if (getPixel(img, point.x - 1, point.y - 1) == SEA_COLOR ||
			    getPixel(img, point.x, point.y - 1) == SEA_COLOR ||
			    getPixel(img, point.x + 1, point.y - 1) == SEA_COLOR ||
			    getPixel(img, point.x - 1, point.y) == SEA_COLOR ||
			    getPixel(img, point.x + 1, point.y) == SEA_COLOR ||
			    getPixel(img, point.x - 1, point.y + 1) == SEA_COLOR ||
			    getPixel(img, point.x, point.y + 1) == SEA_COLOR ||
			    getPixel(img, point.x + 1, point.y + 1) == SEA_COLOR) {
				return true;
			}
		}

		return false;
	}

	private static int getPixel(BufferedImage img, int x, int y) {
		if (x < 0 || y < 0 || x >= img.getWidth() || y >= img.getHeight()) {
			return OUT_OF_BOUNDS;
		}

		return img.getRGB(x, y);
	}

	private static void setLand(BufferedImage img, List<Point> chain) {
		for (Point point : chain) {
			img.setRGB(point.x, point.y, NEW_LAND_COLOR);
		}
	}
}
