package org.digitalmodular.paotools.common;

/**
 * @author Mark Jeronimus
 */
// Created 2020-11-02
public final class ColorUtilities {
	private ColorUtilities() { throw new AssertionError(); }

	private static final int     SRGB_PRECISION  = 4096;
	private static final float[] TO_SRGB_TABLE   = new float[SRGB_PRECISION + 2];
	private static final float[] FROM_SRGB_TABLE = new float[SRGB_PRECISION + 2];

	static {
		for (int i = 0; i <= SRGB_PRECISION; i++) {
			float f = i / (float)SRGB_PRECISION;
			TO_SRGB_TABLE[i] = f < 0.0031308f ? f * 12.92f : (float)Math.pow(f, 1 / 2.4) * 1.055f - 0.055f;
			FROM_SRGB_TABLE[i] = f < 0.04045f ? f / 12.92f : (float)Math.pow((f + 0.055f) / 1.055f, 2.4);
		}

		TO_SRGB_TABLE[0] = 0;
		FROM_SRGB_TABLE[0] = 0;
		TO_SRGB_TABLE[SRGB_PRECISION + 1] = 1;
		FROM_SRGB_TABLE[SRGB_PRECISION + 1] = 1;
	}

	public static boolean isTransparent(int col) {
		return (col & 0xFF000000) == 0;
	}

	/**
	 * Inverts a color.
	 * <p>
	 * Note that this inverts the components directly in sRGB color space, so the result is not a linear inversion of
	 * the color.
	 * For example, the linear inverse of 188 (50.3% gray) would be 187 (49.7%), but this function will return 255-188 =
	 * 67 (dark gray).
	 */
	public static int invert(int rgb) {
		int r = 255 - (rgb >> 16 & 0xFF);
		int g = 255 - (rgb >> 8 & 0xFF);
		int b = 255 - (rgb & 0xFF);
		return r << 16 | g << 8 | b;
	}

//	/**
//	 * Combines (after clamping) three separate color components into a packed RGB integer.
//	 * <p>
//	 * Components should be in sRGB color space.
//	 */
//	public static int toRGB(int r, int g, int b) {
//		r = Math.max(0, Math.min(255, r));
//		g = Math.max(0, Math.min(255, g));
//		b = Math.max(0, Math.min(255, b));
//		return r << 16 | g << 8 | b;
//	}

	public static float[] rgb2floats(int rgb) {
		return new float[]{(rgb >> 16 & 0xFF) / 255.0f,
		                   (rgb >> 8 & 0xFF) / 255.0f,
		                   (rgb & 0xFF) / 255.0f};
	}

	public static int floats2rgb(float r, float g, float b) {
		return Math.min(255, Math.round(r * 255)) << 16 |
		       Math.min(255, Math.round(g * 255)) << 8 |
		       Math.min(255, Math.round(b * 255));
	}

	/**
	 * Calculate linear perceptual luminosity from a color in sRGB color space.
	 */
	public static float getPerceptualLuminosity(int rgb) {
		return getPerceptualLuminosity((rgb >> 16 & 0xFF) / 255.0f,
		                               (rgb >> 8 & 0xFF) / 255.0f,
		                               (rgb & 0xFF) / 255.0f);
	}

	/**
	 * Calculate linear perceptual luminosity from a color in sRGB color space.
	 */
	public static float getPerceptualLuminosity(float r, float g, float b) {
		return fromSRGB(r) * 0.2126f +
		       fromSRGB(g) * 0.7152f +
		       fromSRGB(b) * 0.0722f;
	}

	public static float[] rgb2hsl(float r, float g, float b) {
		float minimum = Math.min(Math.min(r, g), b);
		float maximum = Math.max(Math.max(r, g), b);
		float span    = maximum - minimum;

		float lum = (minimum + maximum) / 2;

		if (span < 1.0e-7)
			return new float[]{0, 0, lum};

		float sat = span / (1 - Math.abs(minimum + maximum - 1));

		if (maximum == r) {
			if (minimum == b)
				return new float[]{((g - b) / span) / 6, sat, lum};
			else
				return new float[]{((g - b) / span + 6) / 6, sat, lum};
		} else {
			if (maximum == g)
				return new float[]{((b - r) / span + 2) / 6, sat, lum};
			else
				return new float[]{((r - g) / span + 4) / 6, sat, lum};
		}
	}

	public static float[] getPerceptualHSL(float r, float g, float b) {
		float[] hsl = rgb2hsl(r, g, b);

		// Adjust SAT down when LUM != 0.5, since darker and more pastel colors (i.e. closer to white) appear less
		// saturated to the human eye.
		// I.e. (0, 128, 0), (64, 192, 64), and (128, 255, 128) all look approximately half-saturated.
		hsl[1] *= 1 - Math.abs(hsl[2] * 2 - 1);

		// Replace LUM
		hsl[2] = getPerceptualLuminosity(r, g, b);

		return hsl;
	}

	/**
	 * Convert from sRGB values (the normal 0..255 range computers use) to linear values.
	 */
	public static float fromSRGB(float f) {
//		return f < 0.04045f ? f / 12.92f : (float)Math.pow((f + 0.055f) / 1.055f, 2.4);
		f = Math.max(0, Math.min(1, f)) * SRGB_PRECISION;
		int   fInt = (int)f;
		float fPos = f - fInt;
		return lerp(FROM_SRGB_TABLE[fInt], FROM_SRGB_TABLE[fInt + 1], fPos);
	}

	/**
	 * Convert from linear values to sRGB values (the normal 0..255 range computers use).
	 */
	public static float toSRGB(float f) {
//		return f < 0.0031308f ? f * 12.92f : (float)Math.pow(f, 1 / 2.4) * 1.055f - 0.055f;
		f = Math.max(0, Math.min(1, f)) * SRGB_PRECISION;
		int   fInt = (int)f;
		float fPos = f - fInt;
		return lerp(TO_SRGB_TABLE[fInt], TO_SRGB_TABLE[fInt + 1], fPos);
	}

	/**
	 * Linearly interpolate two values based on a third value in the range [0, 1].
	 */
	public static float lerp(float first, float second, float position) {
		return first + (second - first) * position;
	}
}
