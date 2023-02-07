package org.digitalmodular.paotools.templater;

import org.digitalmodular.paotools.common.PaoUtilities;
import org.digitalmodular.paotools.newpalettizer.LinearFrameBufferImage;

/**
 * @author Mark Jeronimus
 */
// Created 2020-11-02
public class StackTemplateProcessor implements TemplateProcessor {
	private final int bgColor;

	public StackTemplateProcessor(int bgColor) {
		this.bgColor = bgColor;
	}

	@Override
	public ColorTemplate[] process(LinearFrameBufferImage startImage,
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
