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

import org.digitalmodular.paotools.newpalettizer.LinearFrameBufferImage;
import org.digitalmodular.paotools.newpalettizer.palette.Palette;

/**
 * @author Zom-B
 */
// Created 2020-11-08
public class NearestPalettizer extends Palettizer {
	public NearestPalettizer(Palette pao) {
		super(pao);
	}

	@Override
	public LinearFrameBufferImage palettize(LinearFrameBufferImage image) {
		LinearFrameBufferImage palettizedImage = LinearFrameBufferImage.makeCompatibleImage(image, false);

		int[] srcArray = image.getArray();
		int[] dstArray = palettizedImage.getArray();

		for (int i = 0; i < srcArray.length; i++) {
			int closestIndex = nearestColorIndex(srcArray[i]);
			dstArray[i] = getPao().get(closestIndex).getRGB();
//			dstArray[i] = getPao().get(closestIndex).getGroup().getRepresentativeColor();
		}

		return palettizedImage;
	}
}
