package org.digitalmodular.paotools.templater;

import org.digitalmodular.paotools.common.ColorUtilities;
import org.digitalmodular.paotools.common.PaoUtilities;
import org.digitalmodular.paotools.newpalettizer.LinearFrameBufferImage;

/**
 * Generates a sequence of so-called 'annihilating' templates.
 * <p>
 * These templates are the inverse of the underlying image (so when used with opacity 50%, they appear gray), with the
 * exception of the color that the user is to draw.
 * When the user draws the designated color on the canvas, that pixel also becomes gray.
 * This means that the templates for each color are the inverse of what the image would be when the user is finished
 * with <em>that</em> color.
 * <p>
 * The templates are sorted (decreasing) by the number of pixels the player has to draw for each color.
 * In case of ties, the templates are sorted by color index (increasing).
 *
 * @author Mark Jeronimus
 */
// Created 2020-11-02
public class AnnihilatingTemplateProcessor implements TemplateProcessor {
	@Override
	public ColorTemplate[] process(LinearFrameBufferImage startImage,
	                               LinearFrameBufferImage targetImage,
	                               ColorTemplate[] templates) {
		LinearFrameBufferImage simulatedDrawing   = LinearFrameBufferImage.fromImage(startImage);
		ColorTemplate[]        processedTemplates = new ColorTemplate[templates.length];

		for (int i = 0; i < templates.length; i++) {
			int                    colorIndex    = templates[i].getColorIndex();
			LinearFrameBufferImage templateImage = templates[i].getImage();
			int                    count         = templates[i].getCount();

			simulatedDrawing = PaoUtilities.stackTwoImages(simulatedDrawing, templateImage);

			LinearFrameBufferImage annihilatingImage = makeAnnihilatingImage(simulatedDrawing, targetImage);

			processedTemplates[i] = new ColorTemplate(colorIndex, annihilatingImage, count);
			processedTemplates[i].getImage().extraData = templateImage.extraData;
		}

		return processedTemplates;
	}

	private static LinearFrameBufferImage makeAnnihilatingImage(LinearFrameBufferImage image,
	                                                            LinearFrameBufferImage targetImage) {
		LinearFrameBufferImage annihilatingImage =
				LinearFrameBufferImage.makeCompatibleImage(image, targetImage.isTransparent());

		int[] src = image.getArray();
		int[] dst = annihilatingImage.getArray();

		for (int i = 0; i < dst.length; i++)
			if (src[i] == PaoUtilities.TRANSPARENT)
				dst[i] = PaoUtilities.WHITE;
			else
				dst[i] = 0xFF000000 | ColorUtilities.invert(src[i]);

		return annihilatingImage;
	}

}
