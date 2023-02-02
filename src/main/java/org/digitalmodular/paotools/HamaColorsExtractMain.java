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

import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;

import org.digitalmodular.utilities.HexUtilities;
import org.digitalmodular.utilities.graphics.color.Color3f;
import org.digitalmodular.utilities.graphics.color.Color3fConst;
import org.digitalmodular.utilities.graphics.color.ColorUtilities;
import static org.digitalmodular.utilities.NumberUtilities.clamp;
import static org.digitalmodular.utilities.NumberUtilities.unLerp;

/**
 * @author Mark Jeronimus
 */
// Created 2022-12-26
public final class HamaColorsExtractMain {
	private static final int[]         backtrack       = new int[19968];
	private static       int           numBacktrack    = 0;
	private static       List<Color3f> collectedColors = new ArrayList<>(1000);

	private static int[] MINI_NUMBERS = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11,
	                                     12, 0, 0, 0, 0, 17, 18, 0, 20, 21, 22,
	                                     0, 26, 27, 28, 29, 30, 31, 0, 33, 0, 0,
	                                     0, 0, 0, 0, 43, 44, 45, 46, 47, 48, 49,
	                                     60, 70, 71, 0, 0, 0, 75, 76, 77, 78, 79,
	                                     82, 83, 84, 95, 96, 97, 98, 0, 0, 0, 0};

	public static void main(String... args) throws IOException {
		BufferedImage img = ImageIO.read(new File("Hama.png"));

		int lastNumber = 0;

		int subImageIndex = -1;
		for (int x = 0; x < img.getWidth(); x += 244) {
			for (int y = 0; y < img.getHeight(); y += 144) {
				subImageIndex++;

				if (MINI_NUMBERS[subImageIndex] == 0) {
					continue;
				}

				BufferedImage tile = new BufferedImage(156, 128, BufferedImage.TYPE_INT_RGB);
				Graphics2D    g    = tile.createGraphics();
				try {
					g.drawImage(img, -x, -y, null);
				} finally {
					g.dispose();
				}

				int[] pixels = ((DataBufferInt)tile.getRaster().getDataBuffer()).getData();

				while (++lastNumber < MINI_NUMBERS[subImageIndex]) {
					String filename = Integer.toString(lastNumber);
					filename = "Hama" + "0".repeat(3 - filename.length()) + filename + ".png";
					System.out.println(filename + ": #000000");
				}

				String filename = Integer.toString(lastNumber);
				filename = "Hama" + "0".repeat(3 - filename.length()) + filename + ".png";

				boolean beadFound = false;
				for (int color : pixels) {
					beadFound = beadFound || color != 0xFFFFFF;

					for (int i = 1; i <= 5; i++) {
						if (color == Color3fConst.DOS_PALETTE[i]) {
							System.out.println("Conflict: DOS color " + i + " in " + filename);
						}
					}
				}

				int islands = findIslands(pixels);
				if (islands != 5) {
					System.err.println(filename + ": islands found: " + islands);
				}

				Point[] centers = findCenters(pixels);

				int averageColor = findAverageColor(pixels, centers);
				System.out.println(filename + ": #" + HexUtilities.toColorString(averageColor));

				ImageIO.write(tile, "PNG", new File(filename));
			}
		}
	}

	private static int findIslands(int[] pixels) {
		int islands = 0;
		for (int i = 0; i < 19968; i++) {
			if (pixels[i] == 0xFFFFFF) {
				backtrack[0] = i;
				numBacktrack = 1;
				islands++;
				int numPixels = floodFill(pixels, 0xFFFFFF, Color3fConst.DOS_PALETTE[islands]);

				if (numPixels < 100) {
					backtrack[0] = i;
					numBacktrack = 1;
					floodFill(pixels, Color3fConst.DOS_PALETTE[islands], 0xFFFFFF);
					islands--;
				}
			}
		}

		return islands;
	}

	private static int floodFill(int[] pixels, int find, int replace) {
		int numPixels = 0;

		while (numBacktrack > 0) {
			int p = backtrack[--numBacktrack];
			pixels[p] = replace;
			numPixels++;

			if (p > 156 && pixels[p - 156] == find) {
				backtrack[numBacktrack++] = p - 156;
			}
			if (p > 0 && pixels[p - 1] == find) {
				backtrack[numBacktrack++] = p - 1;
			}
			if (p < 19967 && pixels[p + 1] == find) {
				backtrack[numBacktrack++] = p + 1;
			}
			if (p < 19812 && pixels[p + 156] == find) {
				backtrack[numBacktrack++] = p + 156;
			}
		}

		return numPixels;
	}

	private static Point[] findCenters(int[] pixels) {
		Point[] centers = new Point[4];

		for (int i = 2; i <= 5; i++) {
			int color = Color3fConst.DOS_PALETTE[i];

			int x         = 0;
			int y         = 0;
			int numPixels = 0;
			for (int p = 0; p < pixels.length; p++) {
				if (pixels[p] == color) {
					x += p % 156;
					y += p / 156;
					numPixels++;
				}
			}

			x = (x + numPixels / 2) / numPixels;
			y = (y + numPixels / 2) / numPixels;
			centers[i - 2] = new Point(x, y);
		}

		if (centers[2].y < centers[1].y) {
			Point temp = centers[2];
			centers[2] = centers[1];
			centers[1] = temp;
		}

		return centers;
	}

	private static int findAverageColor(int[] pixels, Point[] centers) {
		collectedColors.clear();

		fillTriangle(pixels, centers, 0);
		fillTriangle(pixels, centers, 1);

		float sumR = 0;
		float sumG = 0;
		float sumB = 0;
		for (Color3f color : collectedColors) {
			Color3f rgb = ColorUtilities.fromSRGB(color);
			sumR += rgb.r;
			sumG += rgb.g;
			sumB += rgb.b;
		}

		sumR /= collectedColors.size();
		sumG /= collectedColors.size();
		sumB /= collectedColors.size();

		sumR = unLerp(0.011157666f, 0.85165670f, sumR);
		sumG = unLerp(0.010514607f, 0.87048346f, sumG);
		sumB = unLerp(0.010761460f, 0.85766286f, sumB);

		sumR = clamp(sumR, 0.0f, 1.0f);
		sumG = clamp(sumG, 0.0f, 1.0f);
		sumB = clamp(sumB, 0.0f, 1.0f);

		sumR = ColorUtilities.toSRGB(sumR);
		sumG = ColorUtilities.toSRGB(sumG);
		sumB = ColorUtilities.toSRGB(sumB);

		int r = Math.round(sumR * 255);
		int g = Math.round(sumG * 255);
		int b = Math.round(sumB * 255);
		return (r << 16) | (g << 8) | b;
	}

	private static void fillTriangle(int[] pixels, Point[] centers, int start) {
		int x0   = centers[start + 0].x;
		int y0   = centers[start + 0].y;
		int x1   = centers[start + 1].x;
		int y1   = centers[start + 1].y;
		int x2   = centers[start + 2].x;
		int y2   = centers[start + 2].y;
		int dx01 = x1 - x0;
		int dy01 = y1 - y0;
		int dx02 = x2 - x0;
		int dy02 = y2 - y0;
		int dx12 = x2 - x1;
		int dy12 = y2 - y1;
		int sa   = 0;
		int sb   = 0;

		int last;
		if (dy12 == 0) {
			last = y1;  // Include y1 scanline
		} else {
			last = y1 - 1; // Skip it
		}

		int y;
		for (y = y0; y <= last; y++) {
			int a = x0 + sa / dy01;
			int b = x0 + sb / dy02;
			sa += dx01;
			sb += dx02;

			if (a > b) {
				drawFastHLine(pixels, b, y, a - b + 1);
			} else {
				drawFastHLine(pixels, a, y, b - a + 1);
			}
		}

		// For lower part of triangle, find scanline crossings for segments
		// 0-2 and 1-2.  This loop is skipped if y1=y2.
		sa = dx12 * (y - y1);
		sb = dx02 * (y - y0);
		for (; y <= y2; y++) {
			int a = x1 + sa / dy12;
			int b = x0 + sb / dy02;
			sa += dx12;
			sb += dx02;

			if (a > b) {
				drawFastHLine(pixels, b, y, a - b + 1);
			} else {
				drawFastHLine(pixels, a, y, b - a + 1);
			}
		}
	}

	private static void drawFastHLine(int[] pixels, int x, int y, int w) {
		int p = y * 156 + x;
		for (int i = 0; i < w; i++) {
			int color = pixels[p++];

			if (isDOSColor(color)) {
				continue;
			}

			collectedColors.add(new Color3f(color));
		}
	}

	private static boolean isDOSColor(int color) {
		for (int i = 1; i <= 5; i++) {
			if (color == Color3fConst.DOS_PALETTE[i]) {
				return true;
			}
		}

		return false;
	}
}
