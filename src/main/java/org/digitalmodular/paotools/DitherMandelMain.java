package org.digitalmodular.paotools;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
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

import org.jetbrains.annotations.NotNull;

import org.digitalmodular.utilities.NumberUtilities;
import org.digitalmodular.utilities.graphics.GraphicsUtilities;

@SuppressWarnings("All")
public class DitherMandelMain extends JPanel implements MouseMotionListener {
	private static final int[] colors = {
			new Color(255, 255, 255).getRGB(),
			new Color(196, 196, 196).getRGB(),
			new Color(85, 85, 85).getRGB(),
			new Color(40, 40, 40).getRGB(),
			new Color(0, 0, 0).getRGB(),
			new Color(107, 0, 0).getRGB(),
			new Color(159, 0, 0).getRGB(),
			new Color(255, 0, 0).getRGB(),
			new Color(187, 79, 0).getRGB(),
			new Color(255, 57, 4).getRGB(),
			new Color(255, 117, 95).getRGB(),
			new Color(255, 165, 0).getRGB(),
			new Color(229, 217, 0).getRGB(),
			new Color(251, 255, 91).getRGB(),
			new Color(241, 194, 125).getRGB(),
			new Color(160, 106, 66).getRGB(),
			new Color(99, 60, 31).getRGB(),
			new Color(0, 102, 0).getRGB(),
			new Color(34, 177, 76).getRGB(),
			new Color(2, 190, 1).getRGB(),
			new Color(255, 192, 203).getRGB(),
			new Color(207, 110, 228).getRGB(),
			new Color(236, 8, 236).getRGB(),
			new Color(130, 0, 128).getRGB(),
			new Color(81, 0, 255).getRGB(),
			new Color(0, 0, 234).getRGB(),
			new Color(4, 75, 255).getRGB(),
			new Color(101, 131, 207).getRGB(),
			new Color(0, 211, 221).getRGB(),
			new Color(69, 255, 200).getRGB(),
			};

	private static final int[][] minidither = {{0, 3}, {2, 1}};
	private static final int     ditherBits = 3;
	private static final int     ditherSize = 1 << ditherBits;
	private static final int     ditherMask = ditherSize - 1;
	private static final int     ditherMax  = ditherSize * ditherSize;
	private static final int[][] dither     = new int[ditherSize][ditherSize];

	private static double mouseX = 1;
	private static double mouseY = 0;

	public static void main(String[] args) throws Exception {
		makeDither();

		SwingUtilities.invokeLater(() -> {
			GraphicsUtilities.setNiceLookAndFeel();

			JFrame f = new JFrame();
			f.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

			f.setContentPane(new DitherMandelMain());

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

	public DitherMandelMain() {
		super(null);
		setPreferredSize(new Dimension(600, 540));
		addMouseMotionListener(this);
	}

	@Override
	protected void paintComponent(Graphics g) {
		int           maxiter = 256;
		BufferedImage img     = makeMandel(maxiter);
		g.drawImage(img, 0, 0, null);

		if (maxiter >= 16384) {
			try {
				ImageIO.write(img, "PNG", new File("/home/zom-b/Pictures/Pixelart/PAO/Mandel-233.png"));
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
	}

	@NotNull
	private static BufferedImage makeMandel(int maxiter) {
		BufferedImage img = new BufferedImage(600, 540, BufferedImage.TYPE_INT_RGB);
		img.setAccelerationPriority(0);
		int[] pixels = ((DataBufferInt)img.getRaster().getDataBuffer()).getData();

		double scale   = 3.0 / img.getHeight();
		double offsetx = -img.getWidth() / 2 * scale - 4.0 / 6;
		double offsety = -img.getHeight() / 2 * scale;

		int p = 0;
		for (int y = 0; y < img.getHeight(); y++) {
			for (int x = 0; x < img.getWidth(); x++) {
				double cx = x * scale + offsetx;
				double cy = y * scale + offsety;
				double zx = 0;
				double zy = 0;
				double xx = 0;
				double yy = 0;
				int    i;
				for (i = 0; i < maxiter; i++) {
					zy = 2 * zx * zy + cy;
					zx = xx - yy + cx;

					xx = zx * zx;
					yy = zy * zy;
					if (xx + yy >= 131072)
						break;
				}

				double index = i >= maxiter ? 4 :
				               (13 - i + Math.log(Math.log(Math.sqrt(xx + yy))) / Math.log(2)) / 2;
				double f = NumberUtilities.floorMod(index, 1);

				double dith2 = dither[y & ditherMask][x & ditherMask] / (double)ditherMax;

				double zoom = mouseX;
				double dith3 = Math.cos((x + 1.0 / zoom) * Math.PI / zoom) *
				               Math.cos((y + 1.0 / zoom) * Math.PI / zoom) * 0.499 + 0.5;

				zoom = 2 + (-cx * cx - cy * cy) / 3;
				double dith1 = Math.cos((x + 1.0 / zoom) * Math.PI / zoom) *
				               Math.cos((y + 1.0 / zoom) * Math.PI / zoom) * 0.499 + 0.5;
				double dith = NumberUtilities.lerp(dith2, dith3, 0.333333333333);

				pixels[p++] = colors[Math.floorMod((int)Math.floor(index + dith), colors.length)];
			}
		}
		return img;
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		double x = e.getX() / (double)(getWidth() / 2) - 1;
		double y = e.getY() / (double)(getHeight() / 2) - 1;

		mouseX = Math.pow(2, x * 4);
		mouseY = y;
		System.out.println(mouseX + "\t" + mouseY);
		repaint();
	}

	@Override
	public void mouseMoved(MouseEvent e) { }
}
