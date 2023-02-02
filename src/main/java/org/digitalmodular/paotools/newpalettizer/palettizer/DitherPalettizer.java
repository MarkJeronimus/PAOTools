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
public class DitherPalettizer extends Palettizer {
//	@SuppressWarnings("SpellCheckingInspection")
//	public enum DitheringMethod {
//		DITHER_YLILUOMA1_ITERATIVE,
//		DITHER_YLILUOMA1,
//		DITHER_YLILUOMA2,
//		DITHER_YLILUOMA3
//	}
//
//	@SuppressWarnings("SpellCheckingInspection")
//	public enum DiffusionMethod {
//		DIFFUSION_NONE,
//		DIFFUSION_FLOYD_STEINBERG,
//		DIFFUSION_JARVIS_JUDICE_NINKE,
//		DIFFUSION_STUCKI,
//		DIFFUSION_BURKES,
//		DIFFUSION_SIERRA3,
//		DIFFUSION_SIERRA2,
//		DIFFUSION_SIERRA24A,
//		DIFFUSION_STEVENSON_ARCE,
//		DIFFUSION_ATKINSON
//	}
//
//	private double          DitherErrorFactor               = 1.0;   // 0 .. 1
//	private int             DitherMatrixWidth               = 8;
//	private int             DitherMatrixHeight              = 8;
//	private int             TemporalDitherSize              = 1;     // 1 = no temporal dithering
//	private boolean         TemporalDitherMSB               = false; // Use MSB rather than LSB for temporal dithering
//	private int             DitherColorListSize             = 0;
//	private double          DitherCombinationContrast       = -1.0;
//	private int             DitherCombinationRecursionLimit = 0;
//	private int             DitherCombinationChangesLimit   = 0;
//	private boolean         DitherCombinationAllowSame      = false;
//	private DitheringMethod Dithering                       = DitheringMethod.DITHER_YLILUOMA1_ITERATIVE;
//	private DiffusionMethod Diffusion                       = DiffusionMethod.DIFFUSION_NONE;

	protected DitherPalettizer(Palette pao) {
		super(pao);
	}

//	List<Integer> CreatePowerofTwoDitheringMatrix(int Width, int Height) {
//		// Find M=ceil(log2(x)) and L=ceil(log2(y))
//		int M = 0;
//		while (Width > (1 << M))
//			++M;
//
//		int L = 0;
//		while (Height > (1 << L))
//			++L;
//
//		int           RoundedWidth  = 1 << M;
//		int           RoundedHeight = 1 << L;
//		List<Integer> result        = new ArrayList<>(Width * Height);
//
//		for (int y = 0; y < Height; ++y)
//			for (int x = 0; x < Width; ++x) {
//				int v = 0, offset = 0, xmask = M, ymask = L;
//				if (M == 0 || (M > L && L != 0)) {
//					int xc = x ^ (y << M >> L), yc = y;
//					for (int bit = 0; bit < M + L; ) {
//						v |= ((yc >> --ymask) & 1) << bit++;
//						for (offset += M; offset >= L; offset -= L)
//							v |= ((xc >> --xmask) & 1) << bit++;
//					}
//				} else {
//					int xc = x, yc = y ^ (x << L >> M);
//					for (int bit = 0; bit < M + L; ) {
//						v |= ((xc >> --xmask) & 1) << bit++;
//						for (offset += L; offset >= M; offset -= M)
//							v |= ((yc >> --ymask) & 1) << bit++;
//					}
//				}
//				result.add(v);
//			}
//
//		if (Height != RoundedHeight
//		    || Width != RoundedWidth) {
//			// If the user requested e.g. 3x3 and we used 4x4 algorithm,
//			// convert the numbers so that the resulting range is ungapped:
//			//
//			// We generated:  We saved:   We compress the number range:
//			//  0 12  3 15     0 12  3     0 7 3
//			//  8  4 11  7     8  4 11     5 4 6
//			//  2 14  1 13     2 14  1     2 8 1
//			// 10  6  9  5
//			int max_value   = RoundedWidth * RoundedHeight;
//			int matrix_size = Height * Width;
//			int n_missing   = 0;
//			for (int v = 0; v < max_value; ++v) {
//				boolean found = false;
//				for (int a = 0; a < matrix_size; ++a) {
//					if (result.get(a) == v) {
//						result.set(a, result.get(a) - n_missing);
//						found = true;
//						break;
//					}
//				}
//				if (!found)
//					++n_missing;
//			}
//		}
//		return result;
//	}
//
//	private List<Integer> CreateDispersedDitheringMatrix() {
//		return CreatePowerofTwoDitheringMatrix(DitherMatrixWidth, DitherMatrixHeight);
//	}
//
//	/* FindBestMixingPlan:
//	 *   Task: Find the combination of palette colors that,
//	 *   when mixed together, the average of them (gamma-corrected)
//	 *   best resembles the input color (using ColorCompare).
//	 */
//	public MixingPlan FindBestMixingPlan(ColorInfo input, Palette pal) {
//		switch (Dithering) {
//			case DITHER_YLILUOMA1:
//				return FindBestMixingPlan_Yliluoma1(input, pal);
//			case DITHER_YLILUOMA1_ITERATIVE:
//			default:
//				return FindBestMixingPlan_Yliluoma1_Iterative(input, pal);
//			case DITHER_YLILUOMA2:
//				return FindBestMixingPlan_Yliluoma2(input, pal);
//			case DITHER_YLILUOMA3:
//				return FindBestMixingPlan_Yliluoma3(input, pal);
//		}
//	}
//
//	////////////
//	/* Algorithm 1: Find best-matching combination.
//	 *              Single-shot, extremely fast, especially if KD-tree is used.
//	 * KD-Tree can be used. Because of that, this algorithm is fast.
//	 */
//	MixingPlan FindBestMixingPlan_Yliluoma1(ColorInfo input, Palette pal) {
//		int chosen = pal.FindClosestCombinationIndex(input).first;
//		MixingPlan result = new MixingPlan(
//				pal.Combinations[chosen].indexlist.begin(),
//				pal.Combinations[chosen].indexlist.end());
//
//		return result;
//	}

	@Override
	public LinearFrameBufferImage palettize(LinearFrameBufferImage image) {
		LinearFrameBufferImage palletizedImage = LinearFrameBufferImage.makeCompatibleImage(image, false);

		int[] srcArray = image.getArray();
		int[] dstArray = palletizedImage.getArray();

		for (int i = 0; i < srcArray.length; i++) {
		}

		return palletizedImage;
	}
}
