package org.digitalmodular.paotools.templater;

import org.digitalmodular.paotools.newpalettizer.palette.Palette;

/**
 * @author Mark Jeronimus
 */
// Created 2020-11-02
public interface TemplateSorter {
	ColorTemplate[] sortTemplates(Palette pao, ColorTemplate[] templates);
}
