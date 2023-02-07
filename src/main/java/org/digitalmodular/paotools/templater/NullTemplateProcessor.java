package org.digitalmodular.paotools.templater;

import org.digitalmodular.paotools.newpalettizer.LinearFrameBufferImage;

/**
 * Does not process the templates and returns the same single-color with transparent background templates it's given.
 * <p>
 * The returned array will be identical to (not a copy of) the array to process.
 *
 * @author Mark Jeronimus
 */
// Created 2020-11-02
public class NullTemplateProcessor implements TemplateProcessor {
	@Override
	public ColorTemplate[] process(LinearFrameBufferImage startImage,
	                               LinearFrameBufferImage targetImage,
	                               ColorTemplate[] templates) {
		return templates;
	}
}
