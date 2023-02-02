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

package org.digitalmodular.paotools.newpalettizer.palettizer;

import java.util.Arrays;
import java.util.Objects;

import org.digitalmodular.utilities.NumberUtilities;

import org.digitalmodular.paotools.common.ColorUtilities;
import org.digitalmodular.paotools.newpalettizer.LinearFrameBufferImage;
import org.digitalmodular.paotools.newpalettizer.palette.Palette;

/**
 * @author Mark Jeronimus
 */
// Created 2020-11-08
public abstract class Palettizer {
	private final Palette   pao;
	private final float[][] hslPalette;

	private final float[] hslWeights = {1, 1, 1};
	private       float   satFactor  = 1;
	private       float   lumFactor  = 1;
	private       float   gamma      = 1;

	protected Palettizer(Palette pao) {
		this.pao = Objects.requireNonNull(pao, "pao");
		hslPalette = new float[pao.size()][];

		for (int i = 0; i < pao.size(); i++) {
			float[] fRGB = ColorUtilities.rgb2floats(pao.get(i).getRGB());
			hslPalette[i] = ColorUtilities.getPerceptualHSL(fRGB[0], fRGB[1], fRGB[2]);
		}
	}

	public Palette getPao() {
		return pao;
	}

	public void setHSLWeights(float[] hslWeights) {
		float sum = hslWeights[0] + hslWeights[1] + hslWeights[2];
		if (sum == 0) {
			Arrays.fill(this.hslWeights, 0);
			return;
		}

		for (int i = 0; i < 3; i++) {
			this.hslWeights[i] = hslWeights[i] / sum;
		}
	}

	public float[] getHslWeights() {
		return hslWeights.clone();
	}

	public float getSatFactor() {
		return satFactor;
	}

	public void setSatFactor(float satFactor) {

		this.satFactor = satFactor;
	}

	public float getLumFactor() {
		return lumFactor;
	}

	public void setLumFactor(float lumFactor) {
		this.lumFactor = lumFactor;
	}

	public float getGamma() {
		return gamma;
	}

	public void setGamma(float gamma) {
		this.gamma = gamma;
	}

	protected int nearestColorIndex(int rgb) {
		float[] fRGB = ColorUtilities.rgb2floats(rgb);
		float[] hsl  = ColorUtilities.getPerceptualHSL(fRGB[0], fRGB[1], fRGB[2]);

		hsl[1] = NumberUtilities.clamp(hsl[1] * satFactor, 0, 1);
		hsl[2] = NumberUtilities.clamp(hsl[2] * lumFactor, 0, 1);
		hsl[2] = (float)Math.pow(hsl[2], gamma);

		float closestDist  = Float.POSITIVE_INFINITY;
		int   closestIndex = -1;
		for (int i = 0; i < hslPalette.length; i++) {
			// The darker or the less saturated the color, the less important the hue.
			float hueWeight = hslWeights[0] * Math.min(hslPalette[i][1], hsl[1]) * Math.min(hslPalette[i][2], hsl[2]);
			// The darker the color, the less important the saturation.
			float satWeight = hslWeights[1] * Math.min(hslPalette[i][2], hsl[2]);

			float diffH  = hslPalette[i][0] - hsl[0];
			float diffS  = hslPalette[i][1] - hsl[1];
			float diffL  = hslPalette[i][2] - hsl[2];
			float diffHH = diffH * diffH * hueWeight;
			float diffSS = diffS * diffS * satWeight;
			float diffLL = diffL * diffL * hslWeights[2];
			float dist   = diffHH + diffSS + diffLL;
			if (dist < closestDist) {
				closestDist = dist;
				closestIndex = i;
			}
		}

		return closestIndex;
	}

	public abstract LinearFrameBufferImage palettize(LinearFrameBufferImage image);
}
