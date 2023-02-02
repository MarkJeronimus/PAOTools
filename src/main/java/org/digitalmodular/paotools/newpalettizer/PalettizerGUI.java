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

import java.awt.BorderLayout;
import java.awt.Color;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

/**
 * @author Zom-B
 */
// Created 2023-01-16
public class PalettizerGUI extends JPanel {
	public static final int SIDE_NAV_BG_COLOR = 0x112222; // 0x091515;
	public static final int BORDER_SIZE_PX    = 20;
	public static final int SEPARATOR_SIZE_PX = 10;

	private final JPanel imagePanel               = new PalettizerImagePanel();
	private final JPanel imageControlsPanel       = new ImageControlsPanel();
	private final JPanel colorAdjustControlsPanel = new ColorAdjustControlsPanel();
	private final JPanel sizeControlsPanel        = new SizeControlsPanel();
	private final JPanel outputControlsPanel      = new OutputControlsPanel();

	@SuppressWarnings("OverridableMethodCallDuringObjectConstruction")
	public PalettizerGUI() {
		super(new BorderLayout());
		setBackground(new Color(SIDE_NAV_BG_COLOR));
		setBorder(new EmptyBorder(BORDER_SIZE_PX, BORDER_SIZE_PX, BORDER_SIZE_PX, BORDER_SIZE_PX));

		// Center, fills available area
		add(imagePanel, BorderLayout.CENTER);

		// Top-right, fixed width, stacked
		JPanel rightPanel = new JPanel(new BorderLayout());
		rightPanel.setOpaque(false);
		rightPanel.setBorder(new EmptyBorder(0, SEPARATOR_SIZE_PX, 0, 0));
		add(rightPanel, BorderLayout.EAST);

		JPanel topRightStackPanel = new JPanel(null);
		topRightStackPanel.setLayout(new BoxLayout(topRightStackPanel, BoxLayout.Y_AXIS));
		topRightStackPanel.setOpaque(false);
		topRightStackPanel.add(imageControlsPanel);
		topRightStackPanel.add(colorAdjustControlsPanel);
		topRightStackPanel.add(sizeControlsPanel);
		topRightStackPanel.add(outputControlsPanel);
		rightPanel.add(topRightStackPanel, BorderLayout.NORTH);
	}
}
