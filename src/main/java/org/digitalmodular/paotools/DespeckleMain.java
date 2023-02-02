package org.digitalmodular.paotools;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import org.digitalmodular.utilities.graphics.image.ImageMatrixFloat;
import org.digitalmodular.utilities.graphics.image.filter.ImageConvolutionFlatSymmetricFilter;

/**
 * @author Mark Jeronimus
 */
// Created 2020-09-06
public class DespeckleMain extends JPanel implements MouseMotionListener {
	private static final float PRECISION = 1 / 512.0f;

	public static void main(String[] args) {
		SwingUtilities.invokeLater(() -> {
			JFrame f = new JFrame();
			f.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

			f.setContentPane(new DespeckleMain());

			f.pack();
			f.setLocationRelativeTo(null);
			f.setVisible(true);
		});
	}

	private static int[] extractPalette(int[] pixels) {
		int[] palette = pixels.clone();
		Arrays.sort(palette);

		int ptr = 0;
		for (int i = 1; i < palette.length; i++) {
			if (palette[i] != palette[ptr]) {
				ptr++;
				palette[ptr] = palette[i];
			}
		}

		return Arrays.copyOf(palette, ptr + 1);
	}

	private final BufferedImage original;
	private final BufferedImage despeckled;
	private final int           width;
	private final int           height;
	private final int[]         originalPixels;
	private final int[]         despeckledPixels;
	private final int[]         palette;

	private float stdDeviation = 2.0f;

	public DespeckleMain() {
		super(null);

		BufferedImage loaded = null;
		try {
			loaded = ImageIO.read(new File("/home/zom-b/Pictures/Pixelart/PAO/Drunk Kimi-PAO.png"));
		} catch (IOException ex) {
			ex.printStackTrace();
			System.exit(1);
		}

		width = loaded.getWidth();
		height = loaded.getHeight();
		System.out.println(width + "\t" + height + "\t" + width * height);

		original = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		original.createGraphics().drawImage(loaded, 0, 0, null);
		original.setAccelerationPriority(0);
		originalPixels = ((DataBufferInt)original.getRaster().getDataBuffer()).getData();

		despeckled = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		despeckled.setAccelerationPriority(0);
		despeckledPixels = ((DataBufferInt)despeckled.getRaster().getDataBuffer()).getData();

		palette = extractPalette(originalPixels);
		System.out.println(Arrays.toString(palette));

		setPreferredSize(new Dimension(width * 4, height * 4));
		addMouseMotionListener(this);
	}

	@Override
	protected void paintComponent(Graphics g) {
		despeckle();

		g.drawImage(despeckled, 0, 0, getWidth(), getHeight(), 0, 0, width, height, null);

		try {
			ImageIO.write(despeckled,
			              "PNG",
			              new File("/home/zom-b/Pictures/Pixelart/PAO/Drunk Kimi-PAO-Despeckle.png"));
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	private void despeckle() {
		if (stdDeviation <= 0) {
			System.out.println(stdDeviation);

			System.arraycopy(originalPixels, 0, despeckledPixels, 0, originalPixels.length);
			return;
		}

		int filterRadius = ImageConvolutionFlatSymmetricFilter.getIdealRadius(stdDeviation, PRECISION);
		ImageConvolutionFlatSymmetricFilter blur =
				ImageConvolutionFlatSymmetricFilter.designGaussianBlur(stdDeviation, PRECISION, filterRadius);

		ImageMatrixFloat areas = new ImageMatrixFloat(width, height, palette.length, filterRadius);
		ImageMatrixFloat temp  = new ImageMatrixFloat(width, height, palette.length, filterRadius);
		for (int z = 0; z < palette.length; z++) {
			int p = 0;
			for (int y = 0; y < height; y++)
				for (int x = 0; x < width; x++) {
					int c = originalPixels[p++];
					areas.matrix[z][y + filterRadius][x + filterRadius] = c == palette[z] ? 1 : 0;
				}
		}

		blur.filter(areas, temp, areas);

		int p = 0;
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				float max      = Float.NEGATIVE_INFINITY;
				int   maxIndex = -1;
				for (int z = 0; z < palette.length; z++) {
					float value = areas.matrix[z][y + filterRadius][x + filterRadius];
					if (value > max) {
						max = value;
						maxIndex = z;
					}
				}

				despeckledPixels[p++] = max < 1.0 / 3 ? 0xFFFFFF : palette[maxIndex];
			}
		}

		int strayCount = countStrayPixels();
		System.out.println(stdDeviation + "\t" + strayCount);
	}

	private int countStrayPixels() {
		int strayCount = 0;

		int p = width;
		for (int y = 1; y < height - 1; y++) {
			p++;
			for (int x = 1; x < width - 1; x++) {
				int c = despeckledPixels[p];

				int u = despeckledPixels[p - width];
				int d = despeckledPixels[p + width];
				int l = despeckledPixels[p - 1];
				int r = despeckledPixels[p + 1];

				if (r != c && l != c && d != c && u != c)
					if (u == d && u == l && u == r)
						strayCount++;

				p++;
			}
			p++;
		}

		return strayCount;
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		stdDeviation = e.getX() * 4.0f / getWidth();

		repaint();
	}

	@Override
	public void mouseMoved(MouseEvent e) {

	}
}
