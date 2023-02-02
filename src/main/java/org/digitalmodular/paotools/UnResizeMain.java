package org.digitalmodular.paotools;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.io.File;
import javax.imageio.ImageIO;

import org.digitalmodular.utilities.graphics.image.ImageUtilities;

/**
 * @author Mark Jeronimus
 */
// Created 2020-09-27
public final class UnResizeMain {
	public static void main(String... args) throws Exception {
		BufferedImage img = ImageIO.read(new File("/home/zom-b/Pictures/Pixelart/PAO/widthmismatch.png"));

		img = ImageUtilities.toIntRasterImage(img);

		img.setAccelerationPriority(0);
		int   width  = img.getWidth();
		int   height = img.getHeight();
		int[] pixels = ((DataBufferInt)img.getRaster().getDataBuffer()).getData();

		shrinkHorizontally(pixels, width, height);
		shrinkVertically(pixels, width, height);

		ImageIO.write(img, "PNG", new File("/home/zom-b/Pictures/Pixelart/PAO/widthmismatch-shrink.png"));
	}

	private static void shrinkHorizontally(int[] pixels, int width, int height) {
		int dstX = 0;
		for (int x = 0; x < width; x++) {
			boolean same = true;
			for (int y = 0; y < height; y++) {
				if (pixels[dstX + width * y] != pixels[x + width * y]) {
					same = false;
					break;
				}
			}

			if (same)
				continue;
			else
				dstX++;

			for (int y = 0; y < height; y++)
				pixels[dstX + width * y] = pixels[x + width * y];
		}
	}

	private static void shrinkVertically(int[] pixels, int width, int height) {
		int dstY = 0;
		for (int y = 0; y < height; y++) {
			boolean same = true;
			for (int x = 0; x < width; x++) {
				if (pixels[x + width * dstY] != pixels[x + width * y]) {
					same = false;
					break;
				}
			}

			if (same)
				continue;
			else
				dstY++;

			for (int x = 0; x < width; x++)
				pixels[x + width * dstY] = pixels[x + width * y];
		}
	}
}
