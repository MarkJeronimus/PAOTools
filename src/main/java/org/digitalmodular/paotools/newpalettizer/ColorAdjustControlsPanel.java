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

package org.digitalmodular.paotools.newpalettizer;

import java.awt.Dimension;
import java.awt.GridLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

/**
 * @author Zom-B
 */
// Created 2023-01-16
public class ColorAdjustControlsPanel extends JPanel {
	public static final int CONTROLS_PANEL_WIDTH = 600;

	@SuppressWarnings({"OverridableMethodCallDuringObjectConstruction", "ThisEscapedInObjectConstruction"})
	public ColorAdjustControlsPanel() {
		super(null);
		setLayout(new GridLayout(1, 1));
		setOpaque(false);

		setBorder(new TitledBorder("Adjustments"));
		add(new JLabel("Controls"));
		setPreferredSize(new Dimension(CONTROLS_PANEL_WIDTH, getPreferredSize().height));
	}
}
