package org.digitalmodular.paotools.templater;

import org.digitalmodular.paotools.newpalettizer.palette.Palette;
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
public class StackTemplateProcessor implements TemplateProcessor {
	private final int bgColor;

	public StackTemplateProcessor(int bgColor) {
		this.bgColor = bgColor;
	}

	@Override
	public ColorTemplate[] process(Palette pao,
	                               LinearFrameBufferImage startImage,
	                               LinearFrameBufferImage targetImage,
	                               ColorTemplate[] templates) {
		ColorTemplate[] processedTemplates = new ColorTemplate[templates.length];

		LinearFrameBufferImage stack = LinearFrameBufferImage.makeCompatibleImage(templates[0].getImage());
		for (int i = 0; i < templates.length; i++) {
			int                    colorIndex    = templates[i].getColorIndex();
			LinearFrameBufferImage templateImage = templates[i].getImage();
			int                    count         = templates[i].getCount();

			stack = PaoUtilities.stackTwoImages(stack, templateImage);

			LinearFrameBufferImage template = LinearFrameBufferImage.fromImage(stack);

			if (bgColor != PaoUtilities.TRANSPARENT)
				PaoUtilities.replaceAll(template.getArray(), PaoUtilities.TRANSPARENT, bgColor);

			processedTemplates[i] = new ColorTemplate(colorIndex, template, count);
			processedTemplates[i].getImage().extraData = templateImage.extraData;
		}

		return processedTemplates;
	}
}
