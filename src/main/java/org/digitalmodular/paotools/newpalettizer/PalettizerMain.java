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
import java.awt.Graphics;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import org.digitalmodular.imageutilities.SizeInt;
import org.digitalmodular.imageutilities.resize.ImageResamplerShort;
import org.digitalmodular.imageutilities.resize.ResizerUtilities;
import org.digitalmodular.imageutilities.resize.resamplingcurve.LinearResamplingCurve;
import org.digitalmodular.paotools.common.ImageUtilities;
import org.digitalmodular.paotools.newpalettizer.palette.Palette;
import org.digitalmodular.paotools.common.PaoUtilities;
import org.digitalmodular.paotools.newpalettizer.palette.PRPaletteV1;
import org.digitalmodular.paotools.newpalettizer.palettizer.NearestPalettizer;

/**
 * @author Mark Jeronimus
 */
// Created 2020-11-08
@SuppressWarnings("ALL")
public final class PalettizerMain extends JPanel implements MouseMotionListener, KeyListener {
	private static final Palette             pao       = PRPaletteV1.instance();
	private static final ImageResamplerShort resampler = new ImageResamplerShort();
	private static final NearestPalettizer   palletizer = new NearestPalettizer(pao);

	private static JFrame frame;

	private static LinearFrameBufferImage originalImage;
	private static LinearFrameBufferImage srcImg;
	private static int                    background;

	private static      int     targetSize = 387;
	private final       float[] weights    = {0.2f, 0.5f, 0.3f};
	public static final float   GAMMA      = 1.5f;
	public static final float   SAT_FACTOR = 1f;
	public static final float   LUM_FACTOR = 1.5f;

	@SuppressWarnings("OverlyBroadThrowsClause")
	public static void main(String... args) throws Exception {
		String filename = "/home/zom-b/Pictures/Pixelart/PR/HE1_0945-brown.png";
		background = PaoUtilities.WHITE;

		originalImage = LinearFrameBufferImage.fromFile(filename);
		originalImage = ImageUtilities.makeOpaque(originalImage, background);

		resize();

		SwingUtilities.invokeLater(() -> {
			frame = new JFrame();
			frame.setTitle(filename);
			frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

			frame.setContentPane(new PalettizerMain());

			frame.pack();
			frame.setLocationRelativeTo(null);
			frame.setVisible(true);
		});
	}

	public static void resize() {
		resampler.setResamplingCurve(new LinearResamplingCurve());
		resampler.setOutputSize(ResizerUtilities.getScalingSize(new SizeInt(originalImage),
		                                                        new SizeInt(targetSize, targetSize)));
		try {
			srcImg = LinearFrameBufferImage.fromImage(resampler.resize(originalImage));
		} catch (InterruptedException ex) {
			ex.printStackTrace();
		}
	}

	public PalettizerMain() {
		super(null);
		setPreferredSize(new Dimension(srcImg.getWidth() * 2, srcImg.getHeight()));
		addMouseMotionListener(this);
		addKeyListener(this);
		setFocusable(true);

		Logger.getGlobal().info("Resizing");

		palletizer.setHSLWeights(weights);
		palletizer.setGamma(GAMMA);
		palletizer.setSatFactor(SAT_FACTOR);
		palletizer.setLumFactor(LUM_FACTOR);
	}

	public static LinearFrameBufferImage palletize() {
		LinearFrameBufferImage palletized = palletizer.palettize(srcImg);

		try {
			ImageIO.write(palletized, "PNG", new File("/home/zom-b/Pictures/Pixelart/PAO/HE1_0945-PAO.png"));
		} catch (IOException ex) {
			ex.printStackTrace();
		}

		return palletized;
	}

	@Override
	protected void paintComponent(Graphics g) {
		g.drawImage(srcImg, 0, 0, null);
		g.drawImage(palletize(), srcImg.getWidth(), 0, null);
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		float x = e.getX() / (float)(srcImg.getWidth() - 1) - 1;
		float y = e.getY() / (float)(srcImg.getHeight() - 1);

		if ((e.getModifiersEx() & InputEvent.SHIFT_DOWN_MASK) != 0) {
			if ((e.getModifiersEx() & InputEvent.BUTTON1_DOWN_MASK) != 0) {
				float gamma = (float)Math.exp((x - 0.5));
				System.out.println("GAMMA: " + gamma);
				palletizer.setGamma(gamma);
			}
			if ((e.getModifiersEx() & InputEvent.BUTTON2_DOWN_MASK) != 0) {
				float satFactor = 1 / Math.max(0.1f, 1 - x);
				System.out.println("SAT_FACTOR: " + satFactor);
				palletizer.setSatFactor(satFactor);
			}
			if ((e.getModifiersEx() & InputEvent.BUTTON3_DOWN_MASK) != 0) {
				float lumFactor = 1 / Math.max(0.1f, y);
				System.out.println("LUM_FACTOR: " + lumFactor);
				palletizer.setLumFactor(lumFactor);
			}
		} else {
			if ((e.getModifiersEx() & InputEvent.BUTTON1_DOWN_MASK) != 0) {
				weights[0] = Math.max(0, x);
			}
			if ((e.getModifiersEx() & InputEvent.BUTTON2_DOWN_MASK) != 0) {
				weights[1] = Math.max(0, 1 - y);
			}
			if ((e.getModifiersEx() & InputEvent.BUTTON3_DOWN_MASK) != 0) {
				weights[2] = Math.max(0, 1 - y);
			}

			palletizer.setHSLWeights(weights);
			System.out.println("weights: " + Arrays.toString(palletizer.getHslWeights()));
		}

		repaint();
	}

	@Override
	public void mouseMoved(MouseEvent e) {
	}

	@Override
	public void keyTyped(KeyEvent e) {
		int amount = (e.getModifiersEx() & InputEvent.SHIFT_DOWN_MASK) != 0 ? 10 : 1;
		switch (e.getKeyChar()) {
			case '-':
				targetSize -= amount;
				break;
			case '+':
				targetSize += amount;
				break;
		}

		resize();
		setPreferredSize(new Dimension(srcImg.getWidth() * 2, srcImg.getHeight()));
		frame.setTitle(Integer.toString(targetSize));
		frame.pack();
	}

	@Override
	public void keyPressed(KeyEvent e) {

	}

	@Override
	public void keyReleased(KeyEvent e) {

	}
}
