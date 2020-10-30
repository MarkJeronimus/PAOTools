package org.digitalmodular.paotools;

/**
 * @author zom-b
 */
// Created 2020-10-30
public final class PAOUtilities {
	private PAOUtilities() { throw new AssertionError(); }

	public static final int TRANSPARENT = 0x00000000;

	private static final char[] HEX_DIGITS = {
			'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};

	public static String toHexColor(int value) {
		StringBuilder out = new StringBuilder(7);
		out.append('#');

		for (int i = 20; i >= 0; i -= 4)
			out.append(HEX_DIGITS[value >>> i & 0xF]);

		return out.toString();
	}

	/**
	 * Combines (after clamping) three separate color components into a packed RGB integer.
	 */
	public static int toRGB(int r, int g, int b) {
		r = Math.max(0, Math.min(255, r));
		g = Math.max(0, Math.min(255, g));
		b = Math.max(0, Math.min(255, b));
		return r << 16 | g << 8 | b;
	}

	/**
	 * Returns the color that, when averaged with {@code baseRGB}, would yield {@code targetRGB}.
	 */
	public static int unmerge(int targetRGB, int baseRGB) {
		int r = 2 * (targetRGB >> 16 & 0xFF) - (baseRGB >> 16 & 0xFF);
		int g = 2 * (targetRGB >> 8 & 0xFF) - (baseRGB >> 8 & 0xFF);
		int b = 2 * (targetRGB & 0xFF) - (baseRGB & 0xFF);
		return toRGB(r, g, b);
	}
}
