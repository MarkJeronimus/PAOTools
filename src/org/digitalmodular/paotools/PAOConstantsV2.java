package org.digitalmodular.paotools;

import java.awt.Color;

/**
 * @author zom-b
 */
// Created 2020-10-30
public final class PAOConstantsV2 implements PAOConstants {
	private static final PAOConstants INSTANCE = new PAOConstantsV2();

	public static PAOConstants instance() {
		return INSTANCE;
	}

	private static final PAOColor[] COLORS = {
			new PAOColor(new Color(0xFFFFFF).getRGB(), "White"),
			new PAOColor(new Color(0xCBC6C1).getRGB(), "Gray 4"),
			new PAOColor(new Color(0x948F8C).getRGB(), "Gray 3"),
			new PAOColor(new Color(0x595555).getRGB(), "Gray 2"),
			new PAOColor(new Color(0x252529).getRGB(), "Gray 1"),
			new PAOColor(new Color(0x000000).getRGB(), "Black"),
			new PAOColor(new Color(0x5F0B1A).getRGB(), "Red 1"),
			new PAOColor(new Color(0x991118).getRGB(), "Red 2"),
			new PAOColor(new Color(0xFF0000).getRGB(), "Red 3"),
			new PAOColor(new Color(0xFF5F00).getRGB(), "Red-Orange"),
			new PAOColor(new Color(0xFF9000).getRGB(), "Orange"),
			new PAOColor(new Color(0xFFC600).getRGB(), "Yellow-Orange"),
			new PAOColor(new Color(0xFFFF35).getRGB(), "Yellow"),
			new PAOColor(new Color(0x43FE00).getRGB(), "Green 4"),
			new PAOColor(new Color(0x00D000).getRGB(), "Green 3"),
			new PAOColor(new Color(0x1A901A).getRGB(), "Green 2"),
			new PAOColor(new Color(0x115D25).getRGB(), "Green 1"),
			new PAOColor(new Color(0x00BB86).getRGB(), "Turquoise 1"),
			new PAOColor(new Color(0x4DFFD3).getRGB(), "Turquoise 2"),
			new PAOColor(new Color(0x00D2FF).getRGB(), "Cyan"),
			new PAOColor(new Color(0x598EF0).getRGB(), "Blue 4"),
			new PAOColor(new Color(0x2553FF).getRGB(), "Blue 3"),
			new PAOColor(new Color(0x0012E1).getRGB(), "Blue 2"),
			new PAOColor(new Color(0x001076).getRGB(), "Blue 1"),
			new PAOColor(new Color(0x5200FF).getRGB(), "Purple"),
			new PAOColor(new Color(0x8C6EC1).getRGB(), "Lavender 1"),
			new PAOColor(new Color(0xC5AAEB).getRGB(), "Lavender 2"),
			new PAOColor(new Color(0xEFD6FA).getRGB(), "Lavender 3"),
			new PAOColor(new Color(0xE587DE).getRGB(), "Magenta 3"),
			new PAOColor(new Color(0xE233E2).getRGB(), "Magenta 2"),
			new PAOColor(new Color(0x690B69).getRGB(), "Magenta 1"),
			new PAOColor(new Color(0xA34500).getRGB(), "Saturated Brown"),
			new PAOColor(new Color(0xFF7662).getRGB(), "Salmon"),
			new PAOColor(new Color(0xF3CDC7).getRGB(), "Pink"),
			new PAOColor(new Color(0xFFDFB6).getRGB(), "Brown 4"),
			new PAOColor(new Color(0xE4B369).getRGB(), "Brown 3"),
			new PAOColor(new Color(0xA56638).getRGB(), "Brown 2"),
			new PAOColor(new Color(0x64341F).getRGB(), "Brown 1"),
			};

	private PAOConstantsV2() { }

	@Override
	public int getNumColors() {
		return COLORS.length;
	}

	@Override
	public int getColor(int index) {
		return COLORS[index].rgb;
	}

	@Override
	public String getColorName(int index) {
		return COLORS[index].name;
	}

	@Override
	public int getColorIndex(int rgb) {
		rgb &= 0xFFFFFF;

		for (int i = 0; i < COLORS.length; i++)
			if (COLORS[i].rgb == rgb)
				return i;

		return -1;
	}

	@SuppressWarnings("PackageVisibleField")
	private static final class PAOColor {
		final int    rgb;
		final String name;

		private PAOColor(int rgb, String name) {
			this.rgb = rgb & 0xFFFFFF;
			this.name = name;
		}
	}
}
