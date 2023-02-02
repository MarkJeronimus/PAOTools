package org.digitalmodular.paotools.templater;

import java.util.Arrays;
import java.util.Comparator;

import org.digitalmodular.paotools.common.ColorUtilities;
import org.digitalmodular.paotools.newpalettizer.palette.Palette;

/**
 * @author Mark Jeronimus
 */
// Created 2020-11-02
public class PerceptualLuminositySorter implements TemplateSorter {
	@Override
	public ColorTemplate[] sortTemplates(Palette pao, ColorTemplate[] templates) {
		templates = templates.clone();
		Arrays.sort(templates, makeComparator(pao));
		return templates;
	}

	private static Comparator<ColorTemplate> makeComparator(Palette pao) {
		return Comparator.comparingDouble(a -> {
			int colorIndex = a.getColorIndex();
			int rgb        = pao.get(colorIndex).getRGB();
			return ColorUtilities.getPerceptualLuminosity(rgb);
		});
	}
}
