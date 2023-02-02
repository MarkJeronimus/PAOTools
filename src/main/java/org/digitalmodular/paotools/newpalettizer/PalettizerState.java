/*
 * This file is part of PAO.
 *
 * Copyleft 2023 Mark Jeronimus. All Rights Reversed.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.digitalmodular.paotools.newpalettizer;

import java.awt.image.BufferedImage;
import java.util.function.BiFunction;

/**
 * @author Zom-B
 */
// Created 2023-01-18
@SuppressWarnings({"MethodMayBeStatic", "FieldMayBeFinal", "InstanceVariableMayNotBeInitialized"})
public class PalettizerState {
	private BiFunction<BufferedImage, PalettizerState, BufferedImage> colorAdjuster;
	private BiFunction<BufferedImage, PalettizerState, BufferedImage> resizer;
	private BiFunction<BufferedImage, PalettizerState, BufferedImage> palettizer;

	private BufferedImage originalImage      = null;
	private BufferedImage colorAdjustedImage = null;
	private BufferedImage rescaledImage      = null;
	private BufferedImage palettizedImage    = null;

	public void setImage(BufferedImage image) {
		int width  = image.getWidth();
		int height = image.getHeight();

		originalImage = newImage(width, height);
		colorAdjustedImage = newImage(width, height);

		colorAdjustedImage = colorAdjuster.apply(originalImage, this);
		rescaledImage = resizer.apply(colorAdjustedImage, this);
		palettizedImage = palettizer.apply(rescaledImage, this);
	}

	public void setColorAdjuster(BiFunction<BufferedImage, PalettizerState, BufferedImage> colorAdjuster) {
		this.colorAdjuster = colorAdjuster;
	}

	public void setResizer(BiFunction<BufferedImage, PalettizerState, BufferedImage> resizer) {
		this.resizer = resizer;
	}

	public void setPalettizer(BiFunction<BufferedImage, PalettizerState, BufferedImage> palettizer) {
		this.palettizer = palettizer;
	}

	public BufferedImage getOriginalImage() {
		return originalImage;
	}

	public BufferedImage getColorAdjustedImage() {
		return colorAdjustedImage;
	}

	public BufferedImage getRescaledImage() {
		return rescaledImage;
	}

	public BufferedImage getPalettizedImage() {
		return palettizedImage;
	}

	private BufferedImage newImage(int width, int height) {
		return new BufferedImage(width, height, BufferedImage.TYPE_4BYTE_ABGR);
	}
}
