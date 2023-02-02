package org.digitalmodular.paotools.templater;

import org.digitalmodular.paotools.newpalettizer.LinearFrameBufferImage;

/**
 * @author Mark Jeronimus
 */
// Created 2020-11-02 pulled up from ColorSplitMain
public class ColorTemplate {
	private final int                    colorIndex;
	private final LinearFrameBufferImage image;
	private final int                    count;

	ColorTemplate(int colorIndex, LinearFrameBufferImage image, int count) {
		this.colorIndex = colorIndex;
		this.image = image;
		this.count = count;
	}

	public int getColorIndex() {
		return colorIndex;
	}

	public LinearFrameBufferImage getImage() {
		return image;
	}

	public int getCount() {
		return count;
	}
}
