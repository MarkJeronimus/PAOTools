package org.digitalmodular.paotools.common;

import org.digitalmodular.paotools.newpalettizer.LinearFrameBufferImage;

/**
 * @author Mark Jeronimus
 */
// Created 2020-11-08
public final class ImageUtilities {
	private ImageUtilities() { throw new AssertionError(); }

	public static LinearFrameBufferImage makeOpaque(LinearFrameBufferImage image, int backgroundColor) {
		if (!image.isTransparent())
			return image;

		LinearFrameBufferImage opaqueImage = LinearFrameBufferImage.makeCompatibleImage(image, false);

		int[] srcArray = image.getArray();
		int[] dstArray = opaqueImage.getArray();

		int backgroundR = backgroundColor >> 16 & 0xFF;
		int backgroundG = backgroundColor >> 8 & 0xFF;
		int backgroundB = backgroundColor & 0xFF;

		for (int i = 0; i < srcArray.length; i++) {
			int alpha = srcArray[i] >>> 24;
			int r     = backgroundR + (((srcArray[i] >> 16 & 0xFF) - backgroundR) * alpha + 127) / 255;
			int g     = backgroundG + (((srcArray[i] >> 8 & 0xFF) - backgroundG) * alpha + 127) / 255;
			int b     = backgroundB + (((srcArray[i] & 0xFF) - backgroundB) * alpha + 127) / 255;
			dstArray[i] = r << 16 | g << 8 | b;
		}

		return opaqueImage;
	}
}
