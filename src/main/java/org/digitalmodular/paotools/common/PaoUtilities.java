package org.digitalmodular.paotools.common;

import org.digitalmodular.paotools.newpalettizer.LinearFrameBufferImage;

/**
 * @author Mark Jeronimus
 */
// Created 2020-10-30
public final class PaoUtilities {

	private PaoUtilities() { throw new AssertionError(); }

	public static final int TRANSPARENT = 0x00000000;
	public static final int WHITE       = 0xFFFFFFFF;
	public static final int BLACK       = 0xFF000000;

	/**
	 * Place non-transparent pixels of {@code topImage} on top of {@code bottomImage}
	 */
	public static LinearFrameBufferImage stackTwoImages(LinearFrameBufferImage bottomImage,
	                                                    LinearFrameBufferImage topImage) {
		LinearFrameBufferImage stackedImage = LinearFrameBufferImage.makeCompatibleImage(bottomImage);

		int[] src1 = bottomImage.getArray();
		int[] src2 = topImage.getArray();
		int[] dst  = stackedImage.getArray();

		for (int i = 0; i < dst.length; i++)
			dst[i] = src2[i] != TRANSPARENT ? src2[i] : src1[i];

		return stackedImage;
	}

	public static void replaceAll(int[] array, int toReplace, int replaceWith) {
		for (int i = 0; i < array.length; i++)
			if (array[i] == toReplace)
				array[i] = replaceWith;
	}
}
