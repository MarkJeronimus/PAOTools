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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.LinearGradientPaint;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.RectangularShape;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.util.Arrays;
import java.util.Comparator;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.event.MouseInputListener;

import org.jetbrains.annotations.Nullable;

import org.digitalmodular.utilities.ClipboardUtilities;
import org.digitalmodular.utilities.HexUtilities;
import org.digitalmodular.utilities.NumberUtilities;
import org.digitalmodular.utilities.graphics.GraphicsUtilities;
import org.digitalmodular.utilities.graphics.StaticStrokes;
import org.digitalmodular.utilities.graphics.color.Color3f;
import org.digitalmodular.utilities.graphics.color.ColorMode;
import org.digitalmodular.utilities.graphics.color.ColorUtilities;
import org.digitalmodular.utilities.math.FastTrig;

/**
 * @author Mark Jeronimus
 */
// Created 2020-09-02
public class SolarizedPaletteDesignerMain extends JPanel implements MouseInputListener, KeyListener {
	private static final int SIZE             = 256 * 6;
	private static final int BORDER           = 64;
	private static final int COLOR_WHEEL_SIZE = SIZE / 3;

	private static final Stroke NORMAL_STROKE =
			new BasicStroke(2, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
	private static final Stroke DASHED_STROKE =
			new BasicStroke(2, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 0, new float[]{8.0f, 8.0f}, 0.0f);
	private static final Stroke DOTTED_STROKE =
			new BasicStroke(2, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 0, new float[]{2.0f, 14.0f}, 0.0f);

	private static final FastTrig fastTrig = new FastTrig(4096, 1.0f);

	private static JFrame frame;

	public static void main(String... args) {
		SwingUtilities.invokeLater(() -> {
			GraphicsUtilities.setNiceLookAndFeel();

			frame = new JFrame();
			frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

			frame.setContentPane(new SolarizedPaletteDesignerMain());

			frame.pack();
			frame.setLocationRelativeTo(null);
			frame.setVisible(true);
		});
	}

	private static final Dot[] oldColors = {
	};

	private static final Dot[] newColors = {
			new Dot(0, new Color(0x9F0E0E), "F.Cu"),
//			new Dot(1, new Color(0x44290E), "In1.Cu"),
//			new Dot(2, new Color(0x707000), "In2.Cu"),
//			new Dot(3, new Color(0x1A1A35), "In3.Cu"),
//			new Dot(4, new Color(0x323C00), "In4.Cu"),
//			new Dot(5, new Color(0x610961), "In5.Cu"),
//			new Dot(6, new Color(0x003232), "In6.Cu"),
//			new Dot(7, new Color(0x1A821A), "B.Cu"),
//			new Dot(8, new Color(0xDCDC00), "Edge.Cuts"),
			new Dot(9, new Color(0xD7A135), "Dwgs.User"),
//			new Dot(10, new Color(0x0D2E3E), "Cmts.User"),
//			new Dot(11, new Color(0x935EFF), "Margin"),
			new Dot(12, new Color(0xA0947A), "F.SilkS"),
//			new Dot(13, new Color(0x514A3A), "B.SilkS"),
			new Dot(14, new Color(0x48FF67), "F.Mask"),
//			new Dot(15, new Color(0x00B75D), "B.Mask"),
			new Dot(16, new Color(0x00E0E0), "F.Paste"),
//			new Dot(17, new Color(0x0097B5), "B.Paste"),
			new Dot(18, new Color(0xFFA8A8), "F.CrtYd"),
//			new Dot(19, new Color(0xF05454), "B.CrtYd"),
			new Dot(20, new Color(0x45CF00), "Eco1.User"),
//			new Dot(21, new Color(0x0046D3), "Eco2.User"),
			new Dot(22, new Color(0xD6A3E0), "F.Adhesive"),
//			new Dot(23, new Color(0xBF40AA), "B.Adhesive"),
			new Dot(24, new Color(0x757575), "F.Fab"),
//			new Dot(25, new Color(0x555555), "B.Fab"),
	};

	private static final int[][] pairs = {
			{0, 1}, {1, 2}, {2, 3}, {3, 4}, {4, 5}, {5, 6}, {6, 7},
			{12, 13}, {14, 15}, {16, 17}, {18, 19}, {20, 21}, {22, 23}, {24, 25},
			};

	private final BufferedImage img  = new BufferedImage(SIZE + BORDER * 2, SIZE, BufferedImage.TYPE_INT_RGB);
	private final Graphics2D    imgG = img.createGraphics();

	private final BufferedImage colorWheel         =
			new BufferedImage(COLOR_WHEEL_SIZE, COLOR_WHEEL_SIZE, BufferedImage.TYPE_INT_RGB);
	private final int[]         colorWheelPixels   = ((DataBufferInt)colorWheel.getRaster().getDataBuffer()).getData();
	private final Rectangle     colorWheelLocation = new Rectangle(-1, 0, COLOR_WHEEL_SIZE, COLOR_WHEEL_SIZE);
	private final Graphics2D    colorWheelGraphics = colorWheel.createGraphics();

	private static final Color[] bgColors     = {Color.BLACK, Color.GRAY, Color.WHITE};
	private              int     bgColorIndex = 1;

	private final     Dot selection = new Dot(BORDER * 0.7f);
	private @Nullable Dot selectedDot;
	private           int mouseX    = 0;
	private           int mouseY    = 0;

	@SuppressWarnings("OverridableMethodCallDuringObjectConstruction")
	public SolarizedPaletteDesignerMain() {
		super(null);
		setPreferredSize(new Dimension(BORDER * 2 + SIZE, BORDER * 2 + SIZE));

		colorWheel.setAccelerationPriority(0);

		addMouseListener(this);
		addMouseMotionListener(this);
		addKeyListener(this);

		setFocusable(true);
	}

	@Override
	protected void paintComponent(Graphics g) {
		if (bgColorIndex == -1) {
			imgG.setPaint(new LinearGradientPaint(BORDER,
			                                      0,
			                                      BORDER + SIZE,
			                                      0,
			                                      new float[]{0, 1},
			                                      new Color[]{Color.BLACK, Color.WHITE}));
		} else {
			imgG.setColor(bgColors[bgColorIndex]);
		}
		imgG.fillRect(0, 0, SIZE + BORDER * 2, SIZE);

		GraphicsUtilities.setAntialiased(imgG, true);
		imgG.setStroke(StaticStrokes.DEFAULT_ROUND_STROKE);

		for (int i = 0; i <= 6; i++) {
			int y = SIZE * i / 6;
			imgG.setColor(ColorMode.HSL.toRGB(new Color3f(i / 6.0f, 1.0f, 0.5f)).toColor());
			imgG.drawLine(BORDER, y, BORDER + SIZE, y);
		}

		imgG.setColor(Color.BLACK);
		imgG.drawLine(BORDER, 0, BORDER, SIZE);
		imgG.drawLine(BORDER + SIZE, 0, BORDER + SIZE, SIZE);

		imgG.setStroke(NORMAL_STROKE);

		Arrays.sort(oldColors, Comparator.comparingInt(dot -> dot.id));
		Arrays.sort(newColors, Comparator.comparingInt(dot -> dot.id));

		imgG.setColor(Color.BLACK);
		for (int[] pair : pairs)
			lineBetweenDots(findDot(pair[0]), findDot(pair[1]));

		imgG.setColor(Color.WHITE);
		imgG.setStroke(DOTTED_STROKE);
		int smallest = Math.min(oldColors.length, newColors.length);
		for (int i = 0; i < smallest; i++)
			lineBetweenDots(oldColors[i], newColors[i]);

		//noinspection ConstantConditions
		Arrays.sort(oldColors, Comparator.comparingDouble(dot -> dot.hsi.b));
		//noinspection ConstantConditions
		Arrays.sort(newColors, Comparator.comparingDouble(dot -> dot.hsi.b));

		imgG.setStroke(NORMAL_STROKE);

		for (Dot dot : newColors)
			dot.paintSat(imgG);

		for (Dot dot : oldColors)
			dot.paint(imgG);

		for (Dot dot : newColors)
			dot.paint(imgG);

		selection.paintSat(imgG);
		selection.paint(imgG);

		g.drawImage(img, 0, BORDER - SIZE, null);
		g.drawImage(img, 0, BORDER, null);
		g.drawImage(img, 0, BORDER + SIZE, null);

		if (colorWheelLocation.x >= 0) {
			drawColorWheel();
			g.drawImage(colorWheel, colorWheelLocation.x, colorWheelLocation.y, null);
		}
	}

	private void lineBetweenDots(Dot dot0, Dot dot1) {
		assert dot0.hsi != null;
		assert dot1.hsi != null;

		float x1 = BORDER + SIZE * dot0.hsi.b;
		float x2 = BORDER + SIZE * dot1.hsi.b;
		float y1 = dot0.hsi.r;
		float y2 = dot1.hsi.r;

		Line2D.Float line = new Line2D.Float();
		if (Math.abs(y1 - y2) <= 0.5) {
			line.setLine(x1, SIZE * y1,
			             x2, SIZE * y2);
		} else if (y1 < y2) {
			line.setLine(x1, SIZE * y1,
			             x2, SIZE * (y2 - 1));
			imgG.draw(line);
			line.setLine(x1, SIZE * (y1 + 1),
			             x2, SIZE * y2);
		} else {
			line.setLine(x1, SIZE * y1,
			             x2, SIZE * (y2 + 1));
			imgG.draw(line);
			line.setLine(x1, SIZE * (y1 - 1),
			             x2, SIZE * y2);
		}
		imgG.draw(line);
	}

	private static Dot findDot(int id) {
		for (Dot dot : newColors)
			if (dot.id == id)
				return dot;

		throw new AssertionError("Dot with id not found: " + id);
	}

	private static class Dot {
		final     int     id;
		@Nullable Color3f hsi;
		float   radius;
		boolean filled;
		String  name;

		Dot(int id, Color color, String name) {
			this.id = id;
			Color3f rgb = new Color3f(color);
			hsi = ColorMode.HSL.fromRGB(rgb);
			float lum = ColorUtilities.toSRGB(ColorUtilities.getPerceptualLuminosity(rgb));
			hsi = new Color3f(hsi.r, hsi.g, lum);
			radius = BORDER / 2.0f;
			this.filled = true;
			this.name = name;
		}

		Dot(int id, Color color, boolean filled) {
			this.id = id;
			Color3f rgb = new Color3f(color);
			hsi = ColorMode.HSL.fromRGB(rgb);
			float lum = ColorUtilities.toSRGB(ColorUtilities.getPerceptualLuminosity(rgb));
			hsi = new Color3f(hsi.r, hsi.g, lum);
			radius = BORDER / 2.0f;
			this.filled = filled;
			name = "";
		}

		Dot(float radius) {
			id = 0;
			hsi = null;
			this.radius = radius;
			filled = false;
		}

		public void paintSat(Graphics2D g) {
			if (hsi == null)
				return;

			assert id >= 0;

			float x      = BORDER + hsi.b * SIZE;
			float y      = hsi.r * SIZE;
			float maxSat = Math.min(0.5f, 1 - ColorMode.HSL.fromRGB(getRGB()).b) * 2;
			float satY   = SIZE * (1 - hsi.g * maxSat);

			g.setColor(id == 0 ? Color.WHITE : Color.DARK_GRAY);

			g.setStroke(DASHED_STROKE);
			g.draw(new Line2D.Float(x, y, x, satY));
			g.setStroke(NORMAL_STROKE);

			g.fill(new Ellipse2D.Float(x - 5, satY - 5, 10, 10));

			if (hsi.g >= 0.09 && id > 0) {
				dotSat(g, x, y);
				dotSat(g, x, y + SIZE);
				dotSat(g, x, y - SIZE);
			}
		}

		public void paint(Graphics2D g) {
			if (hsi == null)
				return;

			float x = BORDER + hsi.b * SIZE;
			float y = hsi.r * SIZE;

			dot(g, x, y);
			dot(g, x, y + SIZE);
			dot(g, x, y - SIZE);

			if (filled && id >= 0) {
				g.setPaint(Color.GRAY);
				g.drawString(newColors[id].name, (int)x - newColors[id].name.length() * 3.5f, (int)y + 5);
			}
		}

		private void dotSat(Graphics2D g, float x, float y) {
			float satRX = SIZE / (float)newColors.length / 1.5f;
			float satRY = (float)(SIZE / NumberUtilities.lerp(1.0f, 6.0f, Math.sqrt(hsi.g)));

			RectangularShape ellipse = new Ellipse2D.Float();
			ellipse.setFrameFromCenter(x, y, x + satRX, y + satRY);

			g.setColor(Color.BLACK);
			g.draw(ellipse);
			g.setColor(new Color(0, 0, 0, 0.05f));
			g.fill(ellipse);
		}

		private void dot(Graphics2D g, float x, float y) {
			g.setColor(getRGB().toColor());

			Shape circle = new Ellipse2D.Float(x - radius, y - radius, radius * 2, radius * 2);
			if (filled)
				g.fill(circle);
			else
				g.draw(circle);
		}

		public float distanceSquared(float x, float y) {
			if (hsi == null)
				return Float.NaN;

			float dx = hsi.b - x;
			float dy = hsi.r - y;
			return dx * dx + dy * dy;
		}

		public Color3f getRGB() {
			if (hsi == null)
				return new Color3f(0, 0, 0);

			return toRGB(hsi.r, hsi.g, ColorUtilities.fromSRGB(hsi.b));
		}

		@Override
		public String toString() {
			Color3f rgb   = getRGB();
			Color   color = rgb.toColor();
			return newColors[id].name + " - 0x" + HexUtilities.toUnsignedString(rgb.toInteger()).substring(2) +
			       " - " + color.getRed() + ' ' + color.getGreen() + ' ' + color.getBlue();
		}
	}

	private static Color3f toRGB(float hue, float sat, float targetLum) {
		float minLum = 0;
		float maxLum = 1;

		Color3f hsl = new Color3f(hue, sat, targetLum);
		Color3f rgb = null;

		while (maxLum - minLum > 0.000001) {
			float lightness = (maxLum + minLum) / 2;
			hsl = new Color3f(hsl.r, hsl.g, lightness);

			rgb = ColorMode.HSL.toRGB(hsl);

			float lum = ColorUtilities.getPerceptualLuminosity(rgb);

			if (lum < targetLum)
				minLum = hsl.b;
			else
				maxLum = hsl.b;
		}

		assert rgb != null;
		return rgb;
	}

	private void drawColorWheel() {
		float mid = (COLOR_WHEEL_SIZE - 1) / 2.0f;

		int p = 0;
		for (int y = 0; y < COLOR_WHEEL_SIZE; y++) {
			float v = mid - y;
			for (int x = 0; x < COLOR_WHEEL_SIZE; x++) {
				float u = x - mid;

				float a = fastTrig.atan2(v, u);
				float r = Math.min(1, fastTrig.sqrt(u * u + v * v) / mid);

				Color3f rgb = ColorMode.HSL.toRGB(new Color3f(a, 1, r));
				colorWheelPixels[p++] = rgb.toInteger();
			}
		}

		if (selection.hsi != null) {
			Color3f hsl = ColorMode.HSL.fromRGB(selection.getRGB());

			double x = mid + fastTrig.sin(hsl.r) * hsl.b * mid;
			double y = mid - fastTrig.cos(hsl.r) * hsl.b * mid;

			float     radius = BORDER / 2.0f;
			Ellipse2D focus  = new Ellipse2D.Double(x - radius, y - radius, radius * 2, radius * 2);
			colorWheelGraphics.setPaint(Color.BLACK);
			colorWheelGraphics.setStroke(NORMAL_STROKE);
			colorWheelGraphics.draw(focus);
			colorWheelGraphics.setPaint(Color.LIGHT_GRAY);
			colorWheelGraphics.setStroke(StaticStrokes.DEFAULT_ROUND_STROKE);
			colorWheelGraphics.draw(focus);
		}
	}

	private void moveColorWheelIfNecessary() {
		if (colorWheelLocation.x == -1 || !colorWheelLocation.contains(mouseX, mouseY))
			return;

		int half = BORDER + SIZE / 2;

		colorWheelLocation.setLocation(mouseX >= half ? 0 : 2 * BORDER + SIZE - COLOR_WHEEL_SIZE,
		                               colorWheelLocation.y);
	}

	private void moveColorWheel() {
		int half = BORDER + SIZE / 2;

		colorWheelLocation.setLocation(mouseX >= half ? 0 : 2 * BORDER + SIZE - COLOR_WHEEL_SIZE,
		                               mouseY >= half ? 0 : 2 * BORDER + SIZE - COLOR_WHEEL_SIZE);
	}

	@Override
	public void mouseClicked(MouseEvent e) {
//		Arrays.sort(newColors, Comparator.comparingInt(dot -> dot.id));
//
//		System.out.println();
//		for (Dot dot : newColors)
//			System.out.println(
//					"new Dot(" + dot.id + ", new Color(0x" +
//					HexUtilities.toUnsignedString(dot.getRGB().toInteger()).substring(2) + "), true),");
//
//		try (BufferedWriter out = Files.newBufferedWriter(Paths.get(
//				"/home/zom-b/Projects/Palettes/032/PAO zomb-Proposal.pal"))) {
//			out.write("JASC-PAL\r\n0100\r\n256\r\n");
//			for (int i = 0; i < 256; i++) {
//				if (i < newColors.length) {
//					Color col = newColors[i].getRGB().toColor();
//					out.write(col.getRed() + " " + col.getGreen() + ' ' + col.getBlue() + "\r\n");
//				} else {
//					out.write("0 0 0\r\n");
//				}
//			}
//		} catch (IOException ex) {
//			ex.printStackTrace();
//		}
//		try (OutputStream out = Files.newOutputStream(Paths.get(
//				"/home/zom-b/Projects/Palettes/032/PAO zomb-Proposal-RGB.act"))) {
//			for (int i = 0; i < 256; i++) {
//				if (i < newColors.length) {
//					Color col = newColors[i].getRGB().toColor();
//					out.write(col.getRed());
//					out.write(col.getGreen());
//					out.write(col.getBlue());
//				} else {
//					out.write(0);
//					out.write(0);
//					out.write(0);
//				}
//			}
//		} catch (IOException ex) {
//			ex.printStackTrace();
//		}
//		try (OutputStream out = Files.newOutputStream(Paths.get(
//				"/home/zom-b/Projects/Palettes/032/PAO zomb-Proposal-BGR.act"))) {
//			for (int i = 0; i < 256; i++) {
//				if (i < newColors.length) {
//					Color col = newColors[i].getRGB().toColor();
//					out.write(col.getBlue());
//					out.write(col.getGreen());
//					out.write(col.getRed());
//				} else {
//					out.write(0);
//					out.write(0);
//					out.write(0);
//				}
//			}
//		} catch (IOException ex) {
//			ex.printStackTrace();
//		}
//
//		bgColorIndex = (bgColorIndex + 2) % (bgColors.length + 1) - 1;
//		repaint();
	}

	@Override
	public void mousePressed(MouseEvent e) {
		mouseX = e.getX();
		mouseY = e.getY();
		mouseDragged(e);
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		selection.filled = false;

		repaint();
	}

	@Override
	public void mouseEntered(MouseEvent e) {
	}

	@Override
	public void mouseExited(MouseEvent e) {
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		if (selection.hsi == null)
			selection.hsi = new Color3f(0, 1.0f, 0);

		if (selectedDot == null) {
			float hue = (e.getY() - BORDER) / (float)SIZE;
			float lum = (e.getX() - BORDER) / (float)SIZE;

			selection.hsi = new Color3f(hue, selection.hsi.g, lum);
			selection.filled = true;

			repaint();
			return;
		}

		assert selectedDot.hsi != null;
		selectedDot.hsi = selection.hsi;

		if ((e.getModifiersEx() & InputEvent.BUTTON2_DOWN_MASK) != 0) {
			float dy  = (mouseY - e.getY()) / (float)SIZE;
			float sat = NumberUtilities.clamp(selection.hsi.g + dy, 0, 1);
			selection.hsi = new Color3f(selection.hsi.r, sat, selection.hsi.b);
			sat = Math.round(sat * 6) / 6.0f;
			selectedDot.hsi = new Color3f(selection.hsi.r, sat, selection.hsi.b);
		} else {
			boolean lmb = (e.getModifiersEx() & InputEvent.BUTTON1_DOWN_MASK) != 0;
			boolean rmb = (e.getModifiersEx() & InputEvent.BUTTON3_DOWN_MASK) != 0;

			float hue = selection.hsi.r;
			if (lmb || selectedDot.id < 8) {
				hue = NumberUtilities.clamp((e.getY() - BORDER) / (float)SIZE, 0, 1);
			}

			float lum = selection.hsi.b;
			if (rmb || selectedDot.id < 8) {
				lum = NumberUtilities.clamp((e.getX() - BORDER) / (float)SIZE, 0, 1);
			}

			selection.hsi = new Color3f(hue, selection.hsi.g, lum);

			hue = Math.round(hue * 36) / 36.0f;
//			if (selectedDot.id < 8) {
//				lum = (float)(Math.cos((hue * 6 - 1.633333333333) * Math.PI / 3) * 0.5 + 0.5);
//				lum = (float)(lum * 0.25 + 0.2);
//				if ((selectedDot.id & 1) != (selectedDot.id & 4) >> 2) {
//					lum = (float)(Math.cos((hue * 6 - 1.66666666666) * Math.PI / 3) * 0.5 + 0.5);
//					lum = (float)(lum * 0.25 + 0.2);
//					lum *=0.5f;
//				}
//			} else {
			lum = Math.round(lum * 24) / 24.0f;
//			}

			selectedDot.hsi = new Color3f(hue, selection.hsi.g, lum);
		}

		mouseX = e.getX();
		mouseY = e.getY();
		frame.setTitle(selectedDot.toString());

		moveColorWheelIfNecessary();

		repaint();
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		float x = (e.getX() - BORDER) / (float)SIZE;
		float y = (e.getY() - BORDER) / (float)SIZE;

		float distThreshold = 1.0f * BORDER * BORDER / SIZE / SIZE;
		float minDist       = Float.POSITIVE_INFINITY;
		selectedDot = null;
		for (Dot dot : newColors) {
			float dist = dot.distanceSquared(x, y);
			if (dist < distThreshold && dist < minDist) {
				minDist = dist;
				selectedDot = dot;
			}
		}

		if (selectedDot != null) {
			//noinspection ObjectEquality
			if (selection.hsi == null || selection.hsi == selectedDot.hsi)
				selection.hsi = new Color3f(0, 0, 0);

			assert selectedDot.hsi != null;
			selection.hsi = selectedDot.hsi;
			frame.setTitle(selectedDot.toString());
			ClipboardUtilities.setString(HexUtilities.toUnsignedString(selectedDot.getRGB().toInteger()).substring(2));
		} else {
			selection.hsi = null;
			frame.setTitle("");
		}

		mouseX = e.getX();
		mouseY = e.getY();

		moveColorWheelIfNecessary();

		repaint();
	}

	@Override
	public void keyTyped(KeyEvent e) {
	}

	@Override
	public void keyPressed(KeyEvent e) {
		if (e.getKeyCode() == KeyEvent.VK_SHIFT)
			moveColorWheel();
	}

	@Override
	public void keyReleased(KeyEvent e) {
		if (e.getKeyCode() == KeyEvent.VK_SHIFT)
			colorWheelLocation.x = -1;
	}
}
