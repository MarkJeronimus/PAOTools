package org.digitalmodular.paotools.ditherer;

/**
 * @author Zom-B
 */
// Created 2020-11-14
public class Globals {
	private enum ColorCompareMethod {
		Compare_RGB,
		Compare_CIE76_DeltaE,
		Compare_CIE94_DeltaE,
		Compare_CMC_lc,
		Compare_BFD_lc,
		Compare_CIEDE2000_DeltaE,
		Compare_fparser
	}

	public static ColorCompareMethod ColorComparing;

	private static final float[][] ILLUMINANTS =
			{{ // CIE C illuminant
			   0.488718f, 0.1762040f, 0.0000000f,
			   0.310680f, 0.8129850f, 0.0102048f,
			   0.200602f, 0.0108109f, 0.989795f},
			 { // Adobe D65 illuminant
			   0.576700f, 0.2973610f, 0.0270328f,
			   0.185556f, 0.6273550f, 0.0706879f,
			   0.188212f, 0.0752847f, 0.9912400f},
			 { // What is this?
			   0.412453f, 0.3575800f, 0.1804230f,
			   0.212671f, 0.7151600f, 0.0721690f,
			   0.019334f, 0.1191930f, 0.9502270f}};

	public static float[] GetIlluminant() {
		switch (ColorComparing) {
			case Compare_CIE76_DeltaE:
				return ILLUMINANTS[2];
			default:
				return ILLUMINANTS[0];
		}
	}
}

