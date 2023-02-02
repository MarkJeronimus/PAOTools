package org.digitalmodular.paotools.templater;

import java.util.Arrays;
import java.util.Comparator;

import org.digitalmodular.paotools.newpalettizer.palette.Palette;

/**
 * @author Mark Jeronimus
 */
// Created 2020-11-02
public class PixelCountSorter implements TemplateSorter {
	private static final Comparator<ColorTemplate> ASCENDING_COMPARATOR =
			Comparator.comparingInt(ColorTemplate::getCount)
			          .thenComparing(ColorTemplate::getColorIndex);

	private static final Comparator<ColorTemplate> DESCENDING_COMPARATOR =
			Comparator.comparingInt(ColorTemplate::getCount).reversed()
			          .thenComparing(ColorTemplate::getColorIndex);
	private final        boolean                   descending;

	public PixelCountSorter(boolean descending) {
		this.descending = descending;
	}

	public boolean isDescending() {
		return descending;
	}

	@Override
	public ColorTemplate[] sortTemplates(Palette pao, ColorTemplate[] templates) {
		templates = templates.clone();
		Arrays.sort(templates, descending ? DESCENDING_COMPARATOR : ASCENDING_COMPARATOR);
		return templates;
	}
}
