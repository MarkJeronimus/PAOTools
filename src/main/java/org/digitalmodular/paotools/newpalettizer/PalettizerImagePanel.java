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
import java.awt.GridLayout;
import java.awt.image.BufferedImage;
import java.util.concurrent.ThreadLocalRandom;
import javax.swing.ButtonGroup;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

import org.digitalmodular.utilities.graphics.swing.ZoomPanel2;

/**
 * @author Zom-B
 */
// Created 2023-01-16
public class PalettizerImagePanel extends JPanel {
	public static final int LEFT_SPACER_PX = PalettizerGUI.BORDER_SIZE_PX / 2;

	private final JToggleButton originalButton = new JToggleButton("Original");
	private final JToggleButton adjustedButton = new JToggleButton("Adjusted");
	private final JToggleButton resizedButton  = new JToggleButton("Resized");
	private final JToggleButton resultButton   = new JToggleButton("Result");
	private final ZoomPanel2    imagePanel     = new ZoomPanel2();

	@SuppressWarnings("OverridableMethodCallDuringObjectConstruction")
	public PalettizerImagePanel() {
		super(new BorderLayout());
		setOpaque(false);

		setBorder(new EmptyBorder(0, LEFT_SPACER_PX, 0, 0));

		JPanel p = new JPanel(new GridLayout(1, 4));
		add(p, BorderLayout.NORTH);
		p.add(originalButton);
		p.add(adjustedButton);
		p.add(resizedButton);
		p.add(resultButton);

		add(imagePanel, BorderLayout.CENTER);

		ButtonGroup group = new ButtonGroup();
		group.add(originalButton);
		group.add(adjustedButton);
		group.add(resizedButton);
		group.add(resultButton);

		originalButton.addActionListener(e -> setImage("original"));
		adjustedButton.addActionListener(e -> setImage("adjusted"));
		resizedButton.addActionListener(e -> setImage("resized"));
		resultButton.addActionListener(e -> setImage("result"));

		// Initial state
		SwingUtilities.invokeLater(() -> {
			resultButton.setSelected(true);
			setImage("result");
		});
	}

	private void setImage(String which) {
		Color color = switch (which) {
			case "original" -> Color.RED;
			case "adjusted" -> Color.YELLOW;
			case "resized" -> Color.GREEN;
			case "result" -> Color.BLUE;
			default -> Color.BLACK;
		};

		BufferedImage image = new BufferedImage(100, 100, BufferedImage.TYPE_4BYTE_ABGR);
		for (int y = 0; y < 100; y++) {
			for (int x = 0; x < 100; x++) {
				image.setRGB(x, y, color.getRGB() & ThreadLocalRandom.current().nextInt() | 0xFF000000);
			}
		}

		imagePanel.setImage(image);
		imagePanel.zoomFit();
	}
}
