package org.digitalmodular.paotools;

import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.Comparator;
import java.util.stream.IntStream;

/**
 * @author zom-b
 */
// Created 2020-09-27
// Changed 2020-10-30 made more object-oriented
public final class ColorSplitMain {
	@SuppressWarnings("OverlyBroadThrowsClause")
	public static void main(String... args) throws Exception {
		String path = "/home/zom-b/Pictures/Pixelart/PAO/";

		String baseFilename        = "Conquest#1-small";
		String startImageFilename  = path + baseFilename + "-BG.png";
		String targetImageFilename = path + baseFilename + "-PAO.png";

		PAOConstants pao = PAOConstantsV2.instance();

		LinearFrameBufferImage startImage  = LinearFrameBufferImage.fromFile(startImageFilename);
		LinearFrameBufferImage targetImage = LinearFrameBufferImage.fromFile(targetImageFilename);
		checkImageCompatibility(startImage, targetImage);

		LinearFrameBufferImage differenceImage = makeDifferenceImage(startImage, targetImage);
		ColorTemplate[]        colorTemplates  = splitColors(pao, differenceImage);
		Arrays.sort(colorTemplates);

		layerTemplates(colorTemplates);

		Thread.yield();

//
//			String colorDesc = StringUtilities.fixLeft(Integer.toString(i), 2) + ' ' + COLORS[i].name;
//			ImageIO.write(colImg,
//			              "PNG",
//			              new File(path + targetImageFilename + "-PAO-" + colorDesc + " (" +
//			                       numPixels + ").png"));
//		}
	}

	private static void layerTemplates(ColorTemplate[] colorTemplates) {
	}

	public static void checkImageCompatibility(BufferedImage startImage, BufferedImage targetImage) {
		if (startImage.getWidth() != targetImage.getWidth() ||
		    startImage.getHeight() != targetImage.getHeight())
			throw new IllegalArgumentException("Images have different sizes: (" +
			                                   startImage.getWidth() + '×' + targetImage.getWidth() + ") != (" +
			                                   startImage.getHeight() + '×' + targetImage.getHeight() + ')');
	}

	private static LinearFrameBufferImage makeDifferenceImage(LinearFrameBufferImage startImage,
	                                                          LinearFrameBufferImage targetImage) {
		LinearFrameBufferImage differenceImage = LinearFrameBufferImage.makeCompatibleImage(startImage, true);

		int[] startArray      = startImage.getArray();
		int[] targetArray     = targetImage.getArray();
		int[] differenceArray = differenceImage.getArray();

		for (int i = 0; i < differenceArray.length; i++) {
			boolean alreadyCorrectColor = startArray[i] == targetArray[i];
			differenceArray[i] = alreadyCorrectColor ?
			                     PAOUtilities.TRANSPARENT :
			                     0xFF000000 | targetArray[i];
		}

		return differenceImage;
	}

	private static ColorTemplate[] splitColors(PAOConstants pao, LinearFrameBufferImage colorsImage) {
		int numColors = pao.getNumColors();

		LinearFrameBufferImage[] splitColorImages =
				IntStream.range(0, numColors)
				         .mapToObj(ignored -> LinearFrameBufferImage.makeCompatibleImage(colorsImage))
				         .toArray(LinearFrameBufferImage[]::new);
		int[] counts = new int[numColors];

		int[] srcArray = colorsImage.getArray();
		int[][] dstArrays = Arrays.stream(splitColorImages)
		                          .map(LinearFrameBufferImage::getArray)
		                          .toArray(int[][]::new);

		for (int i = 0; i < srcArray.length; i++) {
			int rgb = srcArray[i];

			int index = pao.getColorIndex(rgb);
			if (index < 0)
				throw new PAOColorException("Target image contains a color not in the PAO palette: " +
				                            PAOUtilities.toHexColor(rgb));

			dstArrays[index][i] = rgb;
			counts[index]++;
		}

		ColorTemplate[] templates = IntStream
				.range(0, numColors)
				.mapToObj(index -> new ColorTemplate(index, splitColorImages[index], counts[index]))
				.toArray(ColorTemplate[]::new);

		return templates;
	}

	private static class ColorTemplate implements Comparable<ColorTemplate> {
		private final int           colorIndex;
		private final BufferedImage image;
		private final int           count;

		ColorTemplate(int colorIndex, BufferedImage image, int count) {
			this.colorIndex = colorIndex;
			this.image = image;
			this.count = count;
		}

		public int getColorIndex() {
			return colorIndex;
		}

		public BufferedImage getImage() {
			return image;
		}

		public int getCount() {
			return count;
		}

		private static final Comparator<ColorTemplate> SORTER =
				Comparator.comparingInt(ColorTemplate::getCount).reversed()
				          .thenComparing(ColorTemplate::getColorIndex);

		@Override
		public int compareTo(ColorTemplate o) {
			return SORTER.compare(this, o);
		}

		@Override
		public boolean equals(Object o) {
			if (this == o)
				return true;

			if (!(o instanceof ColorTemplate))
				return false;

			ColorTemplate other = (ColorTemplate)o;
			return getColorIndex() == other.getColorIndex() &&
			       getCount() == other.getCount() &&
			       getImage().equals(other.getImage());
		}

		@Override
		public int hashCode() {
			int hashCode = 0x811C9DC5;
			hashCode = 0x01000193 * (hashCode ^ getColorIndex());
			hashCode = 0x01000193 * (hashCode ^ getImage().hashCode());
			hashCode = 0x01000193 * (hashCode ^ getCount());
			return hashCode;
		}
	}
}
