package org.digitalmodular.paotools.templater;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.stream.IntStream;
import javax.imageio.ImageIO;

import org.digitalmodular.paotools.common.ColorUtilities;
import org.digitalmodular.paotools.common.PaoColorException;
import org.digitalmodular.paotools.common.PaoUtilities;
import org.digitalmodular.paotools.newpalettizer.LinearFrameBufferImage;
import org.digitalmodular.paotools.newpalettizer.palette.PRPaletteV1;
import org.digitalmodular.paotools.newpalettizer.palette.Palette;

/**
 * @author Mark Jeronimus
 */
// Created 2020-09-27
// Changed 2020-10-30 made more object-oriented
public final class ColorSplitterMain {
	private ColorSplitterMain() {
		throw new AssertionError();
	}

	private static Palette palette = PRPaletteV1.instance();

	@SuppressWarnings({"OverlyBroadThrowsClause", "ProhibitedExceptionDeclared"})
	public static void main(String... args) throws Exception {
		String path = "/home/zom-b/Pictures/Pixelart/PR/";

		String baseFilename = "HE1_0945-brown";

		LinearFrameBufferImage targetImage = LinearFrameBufferImage.fromFile(path + baseFilename + "-PAO.png");
		LinearFrameBufferImage startImage;

		try {
			startImage = LinearFrameBufferImage.fromFile(path + baseFilename + "-BG.png");
			checkImageCompatibility(startImage, targetImage);
		} catch (IOException ex) {
			ex.printStackTrace();
			startImage = LinearFrameBufferImage.makeCompatibleImage(targetImage);
		}

//		Arrays.fill(startImage.getArray(), PaoUtilities.WHITE);

		LinearFrameBufferImage differenceImage = makeDifferenceImage(startImage, targetImage);
		ColorTemplate[]        rawTemplates    = splitColors(palette, differenceImage);

//		TemplateSorter sorter = new PerceptualLuminositySorter();
		TemplateSorter  sorter          = new PixelCountSorter(false);

		ColorTemplate[] sortedTemplates = sorter.sortTemplates(palette, rawTemplates);
//		Collections.reverse(Arrays.asList(rawTemplates));
//		ColorTemplate[] sortedTemplates = rawTemplates;

		TemplateProcessor templateProcessor = new NullTemplateProcessor();
//		TemplateProcessor templateProcessor  = new StackTemplateProcessor(PaoUtilities.WHITE);
//		TemplateProcessor templateProcessor = new AnnihilatingTemplateProcessor();
		ColorTemplate[] processedTemplates =
				templateProcessor.process(palette, startImage, targetImage, sortedTemplates);

		saveTemplates(palette, path, baseFilename, processedTemplates);
	}

	public static void checkImageCompatibility(BufferedImage startImage, BufferedImage targetImage) {
		if (startImage.getWidth() != targetImage.getWidth() ||
		    startImage.getHeight() != targetImage.getHeight()) {
			throw new IllegalArgumentException("Images have different sizes: (" +
			                                   startImage.getWidth() + '×' + targetImage.getWidth() + ") != (" +
			                                   startImage.getHeight() + '×' + targetImage.getHeight() + ')');
		}
	}

	private static LinearFrameBufferImage makeDifferenceImage(LinearFrameBufferImage startImage,
	                                                          LinearFrameBufferImage targetImage) {
		LinearFrameBufferImage differenceImage = LinearFrameBufferImage.makeCompatibleImage(startImage, true);

		int[] startArray      = startImage.getArray();
		int[] targetArray     = targetImage.getArray();
		int[] differenceArray = differenceImage.getArray();

		for (int i = 0; i < differenceArray.length; i++) {
			if (ColorUtilities.isTransparent(targetArray[i])) {
				differenceArray[i] = startArray[i] == 0xFFFFFFFF ? PaoUtilities.TRANSPARENT : PaoUtilities.WHITE;
			} else {
				differenceArray[i] = startArray[i] == targetArray[i] ?
				                     PaoUtilities.TRANSPARENT :
				                     PaoUtilities.BLACK | targetArray[i];
			}
		}

		return differenceImage;
	}

	private static ColorTemplate[] splitColors(Palette pao, LinearFrameBufferImage colorsImage) {
		int numColors = pao.size();

		LinearFrameBufferImage[] splitColorImages =
				IntStream.range(0, numColors)
				         .mapToObj(ignored -> LinearFrameBufferImage.makeCompatibleImage(colorsImage))
				         .toArray(LinearFrameBufferImage[]::new);
		int[] counts = new int[numColors];

		int[] srcArray = colorsImage.getArray();
		int[][] dstArrays = Arrays.stream(splitColorImages)
		                          .map(LinearFrameBufferImage::getArray)
		                          .toArray(int[][]::new);

		int[][] coordsToPrint = new int[numColors][100];

		for (int i = 0; i < srcArray.length; i++) {
			int rgb = srcArray[i];
			if (rgb == PaoUtilities.TRANSPARENT) {
				continue;
			}

			int index = pao.getIndexOfColor(rgb);
			if (index < 0) {
				throw new PaoColorException(
						String.format("Target image contains a color not in the PAO palette: #%06X", rgb));
			}

			dstArrays[index][i] = rgb;
			counts[index]++;

			if (coordsToPrint[index][0] < 99) {
				coordsToPrint[index][++coordsToPrint[index][0]] = i;
			}
		}

		ColorTemplate[] templates = IntStream
				.range(0, numColors)
				.peek(index -> splitColorImages[index].extraData = coordsToPrint[index])
				.mapToObj(index -> new ColorTemplate(index, splitColorImages[index], counts[index]))
				.toArray(ColorTemplate[]::new);

		return templates;
	}

	public static void saveTemplates(Palette palette,
	                                 String path,
	                                 String baseFilename,
	                                 ColorTemplate[] templates) throws IOException {
		for (int i = 0; i < templates.length; i++) {
			ColorTemplate template = templates[i];

			int count = template.getCount();

			int                    colorIndex = template.getColorIndex();
			LinearFrameBufferImage image      = template.getImage();

			String colorName = palette.get(colorIndex).getName();
			String filename = String.format("%s%s-PAO-%02d %s (col %d, N=%d).png",
			                                path, baseFilename, i, colorName, colorIndex, count);

			if (count > 0) {
				int[] coordsToPrint = (int[])image.extraData;
				System.out.print(filename);
				System.out.print('\t');
				for (int j = 1; j <= coordsToPrint[0]; j++) {
					System.out.printf("|%.3f,%.3f",
					                  (coordsToPrint[j] % image.getWidth()) / (float)image.getWidth(),
					                  (coordsToPrint[j] / image.getWidth()) / (float)image.getHeight());
				}
				System.out.println();

				ImageIO.write(image, "PNG", new File(filename));
			}
		}
	}
}
