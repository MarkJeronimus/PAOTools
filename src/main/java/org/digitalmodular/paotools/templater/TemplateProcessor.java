package org.digitalmodular.paotools.templater;

import org.digitalmodular.paotools.newpalettizer.LinearFrameBufferImage;

/**
 * @author Mark Jeronimus
 */
// Created 2020-11-02
public interface TemplateProcessor {
	/**
	 * Processes the single-color templates into final templates.
	 */
	ColorTemplate[] process(LinearFrameBufferImage startImage,
	                        LinearFrameBufferImage targetImage,
	                        ColorTemplate[] templates);
}
