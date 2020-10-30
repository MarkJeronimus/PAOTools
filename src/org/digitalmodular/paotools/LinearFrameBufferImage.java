package org.digitalmodular.paotools;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Transparency;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferInt;
import java.awt.image.IndexColorModel;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;

/**
 * Extends {@link BufferedImage}, enforces an integer backing array, and adds the ability to directly access this array
 * in a safe manner.
 * <p>
 * There's a static convenience method to create an instance of any other image.
 *
 * @author zom-b
 */
// Created 2020-10-30
public class LinearFrameBufferImage extends BufferedImage {
	private final int[] array;

	/**
	 * Loads an image as if by calling {@link ImageIO#read(File)} and converts it to a {@code LinearFrameBufferImage}.
	 */
	public static LinearFrameBufferImage fromFile(String filename) throws IOException {
		BufferedImage image = ImageIO.read(new File(filename));
		return fromImage(image);
	}

	/**
	 * Makes a new {@code LinearFrameBufferImage} with the same width, height, and transparency as the specified image,
	 * and draws the image onto it.
	 */
	public static LinearFrameBufferImage fromImage(Image image) {
		LinearFrameBufferImage linearImage = makeCompatibleImage(image);

		drawImage(image, linearImage);

		return linearImage;
	}

	/**
	 * Makes a new {@code LinearFrameBufferImage} with the same width, height, and transparency as the specified image.
	 */
	public static LinearFrameBufferImage makeCompatibleImage(Image image) {
		int width  = image.getWidth(null);
		int height = image.getHeight(null);

		boolean transparent = image instanceof Transparency
		                      && ((Transparency)image).getTransparency() != Transparency.OPAQUE;
		int imageType = transparent ? BufferedImage.TYPE_INT_ARGB : BufferedImage.TYPE_INT_RGB;

		LinearFrameBufferImage linearImage = new LinearFrameBufferImage(width, height, imageType);

		linearImage.setAccelerationPriority(0);

		return linearImage;
	}

	/**
	 * Makes a new {@code LinearFrameBufferImage} with the same width and height specified image, and draws the image
	 * onto it.
	 *
	 * @param transparent determines whether the resulting image should have transparency, regardless of the specified
	 *                    image
	 */
	public static LinearFrameBufferImage fromImage(Image image, boolean transparent) {
		LinearFrameBufferImage linearImage = makeCompatibleImage(image, true);

		drawImage(image, linearImage);

		return linearImage;
	}

	/**
	 * Makes a new {@code LinearFrameBufferImage} with the same width and height as the specified image.
	 *
	 * @param transparent determines whether the resulting image should have transparency, regardless of the specified
	 *                    image
	 */
	public static LinearFrameBufferImage makeCompatibleImage(Image image, boolean transparent) {
		int width  = image.getWidth(null);
		int height = image.getHeight(null);

		int imageType = transparent ? BufferedImage.TYPE_INT_ARGB : BufferedImage.TYPE_INT_RGB;

		LinearFrameBufferImage linearImage = new LinearFrameBufferImage(width, height, imageType);

		linearImage.setAccelerationPriority(0);

		return linearImage;
	}

	public LinearFrameBufferImage(int width, int height, int imageType) {
		super(width, height, validateType(imageType));

		// Required to prevent the data array from being loaded into the GPU memory.
		setAccelerationPriority(0);

		// It's ok to use overridable methods from here on because the class is initialized. Although discouraged, this
		// doesn't pose any problems unless someone knowingly crafts a malicious subclass.
		//noinspection OverridableMethodCallDuringObjectConstruction
		array = ((DataBufferInt)getRaster().getDataBuffer()).getData();
	}

	private boolean isTransparent() {
		return getType() == TYPE_INT_ARGB;
	}

	/**
	 * Returns the linear pixel array. Each element is a pixel in either RGB or ARGB format.
	 */
	public int[] getArray() {
		// It's *meant* to be freely modifiable by caller.
		//noinspection AssignmentOrReturnOfFieldWithMutableType
		return array;
	}

	// Prevent overriding this method
	@Override
	public final void setAccelerationPriority(float priority) {
		super.setAccelerationPriority(priority);
	}

	private static int validateType(int imageType) {
		if (!(imageType == TYPE_INT_RGB || imageType == TYPE_INT_ARGB))
			throw new IllegalArgumentException("'imageType' must be either TYPE_INT_RGB or TYPE_INT_ARGB");

		return imageType;
	}

	/**
	 * Draws a regular image on top of a LinearFrameBuffer.
	 * <p>
	 * The sizes are assumed to be equal.
	 * This implementation attempts to avoid Java2D methods is possible.
	 */
	private static void drawImage(Image src, LinearFrameBufferImage target) {
		if (src instanceof BufferedImage) {
			BufferedImage bufferedImage = (BufferedImage)src;
			DataBuffer    dataBuffer    = bufferedImage.getRaster().getDataBuffer();

			// Handle some common image types
			switch (bufferedImage.getType()) {
				case TYPE_INT_RGB:
					copyIntRGBImage(target, (DataBufferInt)dataBuffer);
					return;
				case TYPE_INT_ARGB:
					copyIntARGBImage(target, (DataBufferInt)dataBuffer);
					return;
//				case TYPE_INT_BGR:
//					drawIntArrayImage(target, (DataBufferInt)dataBuffer);
//					return;
//				case TYPE_3BYTE_BGR:
//					drawBGRArrayImage(target, (DataBufferByte)dataBuffer);
//					return;
//				case TYPE_4BYTE_ABGR:
//					drawABGRArrayImage(target, (DataBufferByte)dataBuffer);
//					return;
				case TYPE_BYTE_GRAY:
					drawByteGrayImage(target, (DataBufferByte)dataBuffer);
					return;
				case TYPE_BYTE_INDEXED:
					drawByteIndexedImage(target,
					                     (DataBufferByte)dataBuffer,
					                     (IndexColorModel)bufferedImage.getColorModel());
					return;
				default:
					Logger.getLogger(LinearFrameBufferImage.class.getName())
					      .log(Level.WARNING, "Unimplemented conversion from image type {0}", bufferedImage.getType());

					// Expensive fallback method which may screw up colors
					Graphics2D g = target.createGraphics();
					try {
						g.drawImage(src, 0, 0, null);
					} finally {
						g.dispose();
					}
			}
		}
	}

	private static void copyIntRGBImage(LinearFrameBufferImage target, DataBufferInt dataBuffer) {
		int[] srcArray = dataBuffer.getData();
		int[] dstArray = target.getArray();

		assert srcArray.length == dstArray.length : srcArray.length + " != " + dstArray.length;

		if (!target.isTransparent()) {
			System.arraycopy(srcArray, 0, dstArray, 0, srcArray.length);
		} else {
			for (int i = 0; i < dstArray.length; i++)
				dstArray[i] = 0xFF000000 | srcArray[i];
		}
	}

	private static void copyIntARGBImage(LinearFrameBufferImage target, DataBufferInt dataBuffer) {
		int[] srcArray = dataBuffer.getData();
		int[] dstArray = target.getArray();

		assert srcArray.length == dstArray.length : srcArray.length + " != " + dstArray.length;

		System.arraycopy(srcArray, 0, dstArray, 0, srcArray.length);
	}

//	@SuppressWarnings("ValueOfIncrementOrDecrementUsed")
//	private static void drawBGRArrayImage(LinearFrameBuffer target, DataBufferByte dataBuffer) {
//		byte[] srcArray    = dataBuffer.getData();
//		int[]  dstArray = target.getArray();
//
//		assert srcArray.length == 3 * dstArray.length : srcArray.length + " != 3 * " + dstArray.length;
//
//		int j = 0;
//		for (int i = 0; i < dstArray.length; i++) {
//			int b = srcArray[j++] & 0xFF;
//			int g = srcArray[j++] & 0xFF;
//			int r = srcArray[j++] & 0xFF;
//			dstArray[i] = 0xFF000000 | r << 16 | g << 8 | b;
//		}
//	}
//
//	@SuppressWarnings("ValueOfIncrementOrDecrementUsed")
//	private static void drawABGRArrayImage(LinearFrameBuffer target, DataBufferByte dataBuffer) {
//		byte[] srcArray    = dataBuffer.getData();
//		int[]  dstArray = target.getArray();
//
//		assert srcArray.length == 4 * dstArray.length : srcArray.length + " != 4 * " + dstArray.length;
//
//		int j = 0;
//		for (int i = 0; i < dstArray.length; i++) {
//			int a = srcArray[j++] & 0xFF;
//			int b = srcArray[j++] & 0xFF;
//			int g = srcArray[j++] & 0xFF;
//			int r = srcArray[j++] & 0xFF;
//			dstArray[i] = a << 24 | r << 16 | g << 8 | b;
//		}
//	}

	@SuppressWarnings("ValueOfIncrementOrDecrementUsed")
	private static void drawByteGrayImage(LinearFrameBufferImage target, DataBufferByte dataBuffer) {
		byte[] srcArray = dataBuffer.getData();
		int[]  dstArray = target.getArray();

		assert srcArray.length == 4 * dstArray.length : srcArray.length + " != 4 * " + dstArray.length;

		int j = 0;
		for (int i = 0; i < dstArray.length; i++) {
			int y = srcArray[j++] & 0xFF;
			dstArray[i] = 0xFF000000 | 0x00010101 * y;
		}
	}

	private static void drawByteIndexedImage(
			LinearFrameBufferImage target, DataBufferByte dataBuffer, IndexColorModel colorModel) {
		byte[] srcArray = dataBuffer.getData();
		int[]  dstArray = target.getArray();

		assert srcArray.length == dstArray.length : srcArray.length + " != " + dstArray.length;

		int   numColors = colorModel.getMapSize();
		int[] colors    = new int[numColors];
		colorModel.getRGBs(colors);

		for (int i = 0; i < dstArray.length; i++)
			dstArray[i] = colors[Math.min(srcArray[i] & 0xFF, numColors)];
	}
}
