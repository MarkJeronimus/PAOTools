package org.digitalmodular.paotools.templater;

import org.digitalmodular.paotools.newpalettizer.LinearFrameBufferImage;

/**
 * @author Mark Jeronimus
 */
// Created 2020-11-02
public class FloodFriendlyMakingTemplateProcessor implements TemplateProcessor {
	@Override
	public ColorTemplate[] process(LinearFrameBufferImage startImage,
	                               LinearFrameBufferImage targetImage,
	                               ColorTemplate[] templates) {
		templates = templates.clone();

		for (int i = 0; i < templates.length; i++) {
			templates[i] = fillGaps(templates, i);
		}

		return templates;
	}

	private static ColorTemplate fillGaps(ColorTemplate[] templates, int index) {
		LinearFrameBufferImage image = templates[index].getImage();

		LinearFrameBufferImage processedImage =
				LinearFrameBufferImage.makeCompatibleImage(image, image.isTransparent());

		int   width           = image.getWidth();
		int[] pixels          = image.getArray();
		int[] processedPixels = processedImage.getArray();
		int   count           = templates[index].getCount();

		for (int p = 0; p < pixels.length; p++) {
			if (pixels[p] != 0 || !willBeOverPainted(templates, index, p)) {
				processedPixels[p] = pixels[p];
				continue;
			}

			int a = getPixel(pixels, p, width, 0, -1);
			int b = getPixel(pixels, p, width, -1, 0);
			int c = getPixel(pixels, p, width, 1, 0);
			int d = getPixel(pixels, p, width, 0, 1);

			if (a != 0 && (a == b || a == c || a == d)) {
				processedPixels[p] = a;
				count++;
			} else if (b != 0 && (b == c || b == d)) {
				processedPixels[p] = b;
				count++;
			} else if (c != 0 && c == d) {
				processedPixels[p] = c;
				count++;
			} else {
				processedPixels[p] = pixels[p];
			}
		}

		if (count == templates[index].getCount()) {
			return templates[index];
		}

		processedImage.extraData = image.extraData;

		return new ColorTemplate(templates[index].getColorIndex(), processedImage, count);
	}

	private static boolean willBeOverPainted(ColorTemplate[] templates, int index, int p) {
		for (int i = index + 1; i < templates.length; i++) {
			if (templates[i].getImage().getArray()[p] != 0) {
				return true;
			}
		}

		return false;
	}

	private static int getPixel(int[] template, int p, int width, int dx, int dy) {
		int x      = p % width + dx;
		int y      = p / width + dy;
		int height = template.length / width;

		if (x < 0 || y < 0 || x >= width || y >= height) {
			return 0;
		}

		return template[x + width * y];
	}
}
