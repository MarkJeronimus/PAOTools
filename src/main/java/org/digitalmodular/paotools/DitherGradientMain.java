/*
 * This file is part of PAO.
 *
 * Copyleft 2022 Mark Jeronimus. All Rights Reversed.
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

package org.digitalmodular.paotools;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import org.jetbrains.annotations.Nullable;

import org.digitalmodular.utilities.NumberUtilities;
import org.digitalmodular.utilities.graphics.GraphicsUtilities;

@SuppressWarnings("All")
public class DitherGradientMain extends JPanel implements MouseMotionListener {
	private static final int[] colors = {
			new Color(0, 0, 0).getRGB(),
			new Color(0, 0, 234).getRGB(),
			new Color(4, 75, 255).getRGB(),
			new Color(0, 211, 221).getRGB(),
			new Color(69, 255, 200).getRGB(),
			new Color(255, 255, 255).getRGB(),
			};

	private static final int[][] minidither = {{0, 3}, {2, 1}};
	private static final int     ditherBits = 2;
	private static final int     ditherSize = 1 << ditherBits;
	private static final int     ditherMask = ditherSize - 1;
	private static final int     ditherMax  = ditherSize * ditherSize;
	private static final int[][] dither     = new int[ditherSize][ditherSize];

	private double mouseX = 1;
	private double mouseY = 1;
	private double mouseZ = 2;

	private @Nullable BufferedImage img    = null;
	private @Nullable int[]         pixels = null;

	public static void main(String[] args) throws Exception {
		makeDither();

		SwingUtilities.invokeLater(() -> {
			GraphicsUtilities.setNiceLookAndFeel();

			JFrame f = new JFrame();
			f.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

			f.setContentPane(new DitherGradientMain());

			f.pack();
			f.setLocationRelativeTo(null);
			f.setVisible(true);
		});
	}

	private static void makeDither() {
		for (int x = 0; x < ditherSize; x++) {
			for (int y = 0; y < ditherSize; y++) {
				int u = x;
				int v = y;//(y + (((x + ditherSize / 8) / (ditherSize / 4)) & 1)) % ditherSize;
				dither[v][u] = xy2f(ditherSize, x, y);
			}
		}

		for (int y = 0; y < ditherSize; y++) {
			for (int x = 0; x < ditherSize; x++) {
				System.out.print(dither[y][x] + "\t");
			}
			System.out.println();
		}
	}

	private static int xy2f(int size, int x, int y) {
		int d    = 0;
		int flip = 0;
		for (int i = size >>> 1; i > 0; i >>>= 1) {
			int rx = (x & i) != 0 ? 1 : 0;
			int ry = (y & i) != 0 ? 1 : 0;

			int modification = minidither[ry][rx];
			modification ^= flip;
			d = d << 2 | modification;

			flip ^= 1;
		}
		return d;
	}

	public DitherGradientMain() {
		super(null);
		setPreferredSize(new Dimension(380, 342));
		addMouseMotionListener(this);
	}

	@Override
	protected void paintComponent(Graphics g) {
		int width  = getWidth();
		int height = getHeight();

		if (img == null || img.getWidth() != width || img.getHeight() != height) {
			img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
			img.setAccelerationPriority(0);
			pixels = ((DataBufferInt)img.getRaster().getDataBuffer()).getData();
		}

		double zoom = (getHeight() - 1.0) / (colors.length - 1) / mouseX;

		System.out.println(mouseX + "\t" + mouseY + "\t" + mouseZ);

		int p = 0;
		for (int y = 0; y < img.getHeight(); y++) {
			for (int x = 0; x < img.getWidth(); x++) {
				double index = (1 - y / (img.getHeight() - 1.0)) * (colors.length - 1);
				double f     = NumberUtilities.floorMod(index, 1);

				double zoomdither = dither[y & ditherMask][x & ditherMask] / (double)ditherMax;

				double halftone = (0.5 - Math.cos(x * Math.PI / zoom) *
				                         Math.cos(y * Math.PI / zoom) * 0.5) * mouseY;

				double dith = NumberUtilities.lerp(zoomdither, halftone, mouseZ);

				int col = NumberUtilities.clamp((int)Math.floor(index + dith), 0, colors.length - 1);
//				col = (col & 1) != 0 ? 9 : 0;
				pixels[p++] = colors[col];
			}
		}

		g.drawImage(img, 0, 0, null);

		try {
			ImageIO.write(img, "PNG", new File("/home/zom-b/Pictures/Pixelart/PAO/Dither.png"));
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		double x = e.getX() / (double)(getWidth() / 2) - 1;
		double y = e.getY() / (double)(getHeight() / 2) - 1;

		if ((e.getModifiersEx() & InputEvent.BUTTON1_DOWN_MASK) != 0) {
			mouseX = Math.rint(Math.pow(2, x * 4));
		}

		if ((e.getModifiersEx() & InputEvent.BUTTON3_DOWN_MASK) != 0) {
			mouseY = Math.rint(Math.pow(2, x * 4));
		}

		if ((e.getModifiersEx() & InputEvent.BUTTON2_DOWN_MASK) != 0) {
			mouseZ = x;
		}

		repaint();
	}

	@Override
	public void mouseMoved(MouseEvent e) {
	}
}
