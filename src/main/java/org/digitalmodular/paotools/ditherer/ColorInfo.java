package org.digitalmodular.paotools.ditherer;

import org.digitalmodular.paotools.common.ColorUtilities;

/**
 * @author Zom-B
 */
// Created 2020-11-09
public class ColorInfo {
	public static class LAB {
		float L, a, b;
	}

	// 32-bit ARGB, as it is saved/loaded with a file:
	public int rgb;

	public int A, R, G, B;

	// Luma in 0..255000 range:
	public int luma;

	// ARGB, gamma corrected, all values in 0..1 range:
	public float[] gammac = new float[4];

	// CIE XYZ value
	public float X, Y, Z;

	// CIE L*a*b* value
	public LAB   lab;
	public float C, h;

	public ColorInfo(int rgb) {
		this.rgb = rgb;
		A = rgb >>> 24 & 0xFF;
		R = rgb >> 16 & 0xFF;
		G = rgb >> 8 & 0xFF;
		B = rgb & 0xFF;

		luma = R * 299 + G * 587 + B * 114;

		float Rn = R / 255.0f;
		float Gn = G / 255.0f;
		float Bn = B / 255.0f;

		gammac[0] = A / 127.0f;
		gammac[1] = ColorUtilities.fromSRGB(Rn);
		gammac[2] = ColorUtilities.fromSRGB(Gn);
		gammac[3] = ColorUtilities.fromSRGB(Bn);

		// Use Profile illuminant - D65 */
		float[] illum = Globals.GetIlluminant();
		// float Rn = gammac.r, Gn = gammac.g, Bn = gammac.b;
		X = (illum[0]) * Rn + (illum[3]) * Gn + (illum[6]) * Bn;
		Y = (illum[1]) * Rn + (illum[4]) * Gn + (illum[7]) * Bn;
		Z = (illum[2]) * Rn + (illum[5]) * Gn + (illum[8]) * Bn;

		float Xn         = 1.0f / (illum[0] + illum[1] + illum[2]);
		float Yn         = 1.0f / (illum[3] + illum[4] + illum[5]);
		float Zn         = 1.0f / (illum[6] + illum[7] + illum[8]);
		float x          = X * Xn;
		float y          = Y * Yn;
		float z          = Z * Zn;
		float threshold1 = (6 * 6 * 6.0f) / (29 * 29 * 29.0f);
		float threshold2 = (29 * 29.0f) / (6 * 6 * 3.0f);
		float x1         = (x > threshold1) ? (float)Math.pow(x, 1.0f / 3.0f) : (threshold2 * x) + (4 / 29.0f);
		float y1         = (y > threshold1) ? (float)Math.pow(y, 1.0f / 3.0f) : (threshold2 * y) + (4 / 29.0f);
		float z1         = (z > threshold1) ? (float)Math.pow(z, 1.0f / 3.0f) : (threshold2 * z) + (4 / 29.0f);
		lab.L = (29 * 4) * y1 - (4 * 4);
		lab.a = (500 * (x1 - y1));
		lab.b = (200 * (y1 - z1));

		C = (float)Math.sqrt(lab.a * lab.a + lab.b + lab.b);
		h = (float)Math.atan2(lab.b, lab.a);
	}
}
