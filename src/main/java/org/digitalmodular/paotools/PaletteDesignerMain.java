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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.LinearGradientPaint;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.RectangularShape;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import static java.awt.BasicStroke.CAP_ROUND;
import static java.awt.BasicStroke.JOIN_ROUND;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.event.MouseInputListener;

import org.jetbrains.annotations.Nullable;

import org.digitalmodular.utilities.HexUtilities;
import org.digitalmodular.utilities.NumberUtilities;
import org.digitalmodular.utilities.graphics.GraphicsUtilities;
import org.digitalmodular.utilities.graphics.StaticStrokes;
import org.digitalmodular.utilities.graphics.color.Color3f;
import org.digitalmodular.utilities.graphics.color.ColorMode;
import org.digitalmodular.utilities.graphics.color.ColorUtilities;

/**
 * @author Mark Jeronimus
 */
// Created 2020-09-02
@SuppressWarnings("UseOfSystemOutOrSystemErr")
public class PaletteDesignerMain extends JPanel implements MouseInputListener, MouseWheelListener, KeyListener {

//	// PAO 30
//	private static final Color[] oldColors = {
//			new Color(0xFFFFFF),
//			new Color(0xC4C4C4),
//			new Color(0x555555),
//			new Color(0x282828),
//			new Color(0x000000),
//			new Color(0x006600),
//			new Color(0x22B14C),
//			new Color(0x02BE01),
//			new Color(0xFBFF5B),
//			new Color(0xE5D900),
//			new Color(0xF1C27D),
//			new Color(0xFFA500),
//			new Color(0xA06A42),
//			new Color(0x633C1F),
//			new Color(0x6B0000),
//			new Color(0x9F0000),
//			new Color(0xFF0000),
//			new Color(0xFF3904),
//			new Color(0xBB4F00),
//			new Color(0xFF755F),
//			new Color(0xFFC0CB),
//			new Color(0xCF6EE4),
//			new Color(0xEC08EC),
//			new Color(0x820080),
//			new Color(0x5100FF),
//			new Color(0x0000EA),
//			new Color(0x044BFF),
//			new Color(0x6583CF),
//			new Color(0x00D3DD),
//			new Color(0x45FFC8),
//	};

	// PAO 38
	private static final Color[] oldColors = {
			new Color(0xFFFFFF),
			new Color(0xCBC6C1),
			new Color(0x948F8C),
			new Color(0x595555),
			new Color(0x252529),
			new Color(0x000000),
			new Color(0x5F0B1A),
			new Color(0x991118),
			new Color(0xFF0000),
			new Color(0xFF5F00),
			new Color(0xFF9000),
			new Color(0xFFC600),
			new Color(0xFFFF35),
			new Color(0x43FE00),
			new Color(0x00D000),
			new Color(0x1A901A),
			new Color(0x115D25),
			new Color(0x00BB86),
			new Color(0x4DFFD3),
			new Color(0x00D2FF),
			new Color(0x598EF0),
			new Color(0x2553FF),
			new Color(0x0012E1),
			new Color(0x001076),
			new Color(0x5200FF),
			new Color(0x8C6EC1),
			new Color(0xC5AAEB),
			new Color(0xEFD6FA),
			new Color(0xE587DE),
			new Color(0xE233E2),
			new Color(0x690B69),
			new Color(0xA34500),
			new Color(0xFF7662),
			new Color(0xF3CDC7),
			new Color(0xFFDFB6),
			new Color(0xE4B369),
			new Color(0xA56638),
			new Color(0x64341F),
			};

//	// Hama beads
//	private static final Color[] oldColors = {
//			new Color(0xFFFFFF), // Hama001
//			new Color(0xFFEBB3), // Hama002
//			new Color(0xFFC741), // Hama003
//			new Color(0xFF431E), // Hama004
//			new Color(0xD5281B), // Hama005
//			new Color(0xFA7992), // Hama006
//			new Color(0x352878), // Hama007
//			new Color(0x112E7B), // Hama008
//			new Color(0x0E5AB3), // Hama009
//			new Color(0x0F8143), // Hama010
//			new Color(0x00B362), // Hama011
//			new Color(0x34231E), // Hama012
//			new Color(0x000000), // Hama013
//			new Color(0x000000), // Hama014
//			new Color(0x000000), // Hama015
//			new Color(0x000000), // Hama016
//			new Color(0x6A7473), // Hama017
//			new Color(0x000000), // Hama018
//			new Color(0x000000), // Hama019
//			new Color(0x853020), // Hama020
//			new Color(0xA84A31), // Hama021
//			new Color(0xAC2221), // Hama022
//			new Color(0x000000), // Hama023
//			new Color(0x000000), // Hama024
//			new Color(0x000000), // Hama025
//			new Color(0xEB9281), // Hama026
//			new Color(0xD8A288), // Hama027
//			new Color(0x223525), // Hama028
//			new Color(0xC9263E), // Hama029
//			new Color(0x40121A), // Hama030
//			new Color(0x348F94), // Hama031
//			new Color(0x000000), // Hama032
//			new Color(0xFF413F), // Hama033
//			new Color(0x000000), // Hama034
//			new Color(0x000000), // Hama035
//			new Color(0x000000), // Hama036
//			new Color(0x000000), // Hama037
//			new Color(0x000000), // Hama038
//			new Color(0x000000), // Hama039
//			new Color(0x000000), // Hama040
//			new Color(0x000000), // Hama041
//			new Color(0x000000), // Hama042
//			new Color(0xF0DB6D), // Hama043
//			new Color(0xFF5E54), // Hama044
//			new Color(0x655EA4), // Hama045
//			new Color(0x2FB9ED), // Hama046
//			new Color(0x78C74E), // Hama047
//			new Color(0xDE6BB4), // Hama048
//			new Color(0x16AFC6), // Hama049
//			new Color(0x000000), // Hama050
//			new Color(0x000000), // Hama051
//			new Color(0x000000), // Hama052
//			new Color(0x000000), // Hama053
//			new Color(0x000000), // Hama054
//			new Color(0x000000), // Hama055
//			new Color(0x000000), // Hama056
//			new Color(0x000000), // Hama057
//			new Color(0x000000), // Hama058
//			new Color(0x000000), // Hama059
//			new Color(0xFF8035), // Hama060
//			new Color(0x000000), // Hama061
//			new Color(0x000000), // Hama062
//			new Color(0x000000), // Hama063
//			new Color(0x000000), // Hama064
//			new Color(0x000000), // Hama065
//			new Color(0x000000), // Hama066
//			new Color(0x000000), // Hama067
//			new Color(0x000000), // Hama068
//			new Color(0x000000), // Hama069
//			new Color(0x8F9C9F), // Hama070
//			new Color(0x323737), // Hama071
//			new Color(0x000000), // Hama072
//			new Color(0x000000), // Hama073
//			new Color(0x000000), // Hama074
//			new Color(0xB3927C), // Hama075
//			new Color(0x806152), // Hama076
//			new Color(0xD1D3C8), // Hama077
//			new Color(0xFFAC87), // Hama078
//			new Color(0xFF7841), // Hama079
//			new Color(0x000000), // Hama080
//			new Color(0x000000), // Hama081
//			new Color(0x7A2C5D), // Hama082
//			new Color(0x006E86), // Hama083
//			new Color(0x356834), // Hama084
//			new Color(0x000000), // Hama085
//			new Color(0x000000), // Hama086
//			new Color(0x000000), // Hama087
//			new Color(0x000000), // Hama088
//			new Color(0x000000), // Hama089
//			new Color(0x000000), // Hama090
//			new Color(0x000000), // Hama091
//			new Color(0x000000), // Hama092
//			new Color(0x000000), // Hama093
//			new Color(0x000000), // Hama094
//			new Color(0xED9EBC), // Hama095
//			new Color(0xAE9FCB), // Hama096
//			new Color(0x7CD1F9), // Hama097
//			new Color(0x7DD3A7), // Hama098
//	};

//	// Ministek
//	private static final Color[] oldColors = {
//			new Color(0x0C108A),
//			new Color(0x146291),
//			new Color(0x00BF0E),
//			new Color(0xD60000),
//			new Color(0xF53A00),
//			new Color(0xFFFF00),
//			new Color(0xDFCC6B),
//			new Color(0xBF8900),
//			new Color(0x9D1E00),
//			new Color(0x360000),
//			new Color(0x8C8A8A),
//			new Color(0xFFB5CC),
//			new Color(0x444844),
//			new Color(0x1A3A00),
//			new Color(0xFFDB00),
//			new Color(0xFFA762),
//			new Color(0x6D0070),
//			new Color(0x038A02),
//			new Color(0xFFA500),
//			new Color(0xF51E62),
//			new Color(0x536FB3),
//			};

//	private static final Color[] oldColors;
//
//	static {
//		try {
//			oldColors = loadPal("016/C64 (more contrast).pal");
//		} catch (IOException ex) {
//			throw new RuntimeException(ex);
//		}
//	}

	// PR 40
	private static final Color[] newColors = {
			new Color(0x000000), // 0
			new Color(0x1A1C1E), // 1
			new Color(0x4C4E4F), // 2
			new Color(0x7E807E), // 3
			new Color(0xAFAFB1), // 4
			new Color(0xDEDCDA), // 5
			new Color(0xFFFFFF), // 6
			new Color(0x4A0610), // 7
			new Color(0x8F0D15), // 8
			new Color(0xF82327), // 9
			new Color(0xEF671A), // 10
			new Color(0xFFA82A), // 11
			new Color(0xF7ED05), // 12
			new Color(0xFDF996), // 13
			new Color(0xCC8774), // 14
			new Color(0xF0B39F), // 15
			new Color(0xF9DED4), // 16
			new Color(0x3B261B), // 17
			new Color(0x785332), // 18
			new Color(0xC09667), // 19
			new Color(0xE5C8A1), // 20
			new Color(0x093C09), // 21
			new Color(0x277A0B), // 22
			new Color(0x5EC22A), // 23
			new Color(0x6DFC6D), // 24
			new Color(0x154048), // 25
			new Color(0x3D9692), // 26
			new Color(0x7EF5B6), // 27
			new Color(0x0A197E), // 28
			new Color(0x134DEF), // 29
			new Color(0x4691FF), // 30
			new Color(0x6CD2FF), // 31
			new Color(0x3A178E), // 32
			new Color(0x8069BA), // 33
			new Color(0xC2AFFF), // 34
			new Color(0xE0DBFF), // 35
			new Color(0x6E1653), // 36
			new Color(0xF45DCE), // 37
			new Color(0xFCB3FF), // 38
			new Color(0x46523A), // 39
	};

	// PR 40
	private static final List<int[]> pairs = new ArrayList<>(Arrays.asList(
			new int[]{0, 1},
			new int[]{1, 2},
			new int[]{2, 3},
			new int[]{3, 4},
			new int[]{4, 5},
			new int[]{5, 6},
			new int[]{7, 8},
			new int[]{8, 9},
			new int[]{9, 10},
			new int[]{10, 12},
			new int[]{12, 13},
			new int[]{14, 15},
			new int[]{15, 16},
			new int[]{17, 18},
			new int[]{18, 19},
			new int[]{19, 20},
			new int[]{21, 22},
			new int[]{22, 23},
			new int[]{23, 24},
			new int[]{25, 26},
			new int[]{26, 27},
			new int[]{28, 29},
			new int[]{29, 30},
			new int[]{30, 31},
			new int[]{32, 33},
			new int[]{33, 34},
			new int[]{34, 35},
			new int[]{36, 37},
			new int[]{37, 38}
	));

//	// PR 40 v2
//	private static final Color[] newColors = {
//			new Color(0x000000), // 0
//			new Color(0x1A1C1E), // 1
//			new Color(0x4C4E4F), // 2
//			new Color(0x7E807E), // 3
//			new Color(0xAFAFAD), // 4
//			new Color(0xDEDCDA), // 5
//			new Color(0xFFFFFF), // 6
//			new Color(0x4A0610), // 7
//			new Color(0x8F0D15), // 8
//			new Color(0xF82428), // 9
//			new Color(0xF26414), // 10
//			new Color(0xFDA21F), // 11
//			new Color(0xF7ED05), // 12
//			new Color(0xFDF996), // 13
//			new Color(0xCC8774), // 14
//			new Color(0xF0B39F), // 15
//			new Color(0xF9DED4), // 16
//			new Color(0x3B261B), // 17
//			new Color(0x785332), // 18
//			new Color(0xC09667), // 19
//			new Color(0x093C09), // 20
//			new Color(0xE5C8A1), // 21
//			new Color(0x277A0B), // 22
//			new Color(0x5EC22A), // 23
//			new Color(0x6DFC6D), // 24
//			new Color(0x154048), // 25
//			new Color(0x3D9692), // 26
//			new Color(0x7EF5B6), // 27
//			new Color(0x0A197E), // 28
//			new Color(0x134DEF), // 29
//			new Color(0x4691FF), // 30
//			new Color(0x6CD2FF), // 31
//			new Color(0x3A178E), // 32
//			new Color(0x8069BA), // 33
//			new Color(0xC2AFFF), // 34
//			new Color(0xE0DBFF), // 35
//			new Color(0x6E1653), // 36
//			new Color(0xF45DCE), // 37
//			new Color(0xFCB3FF), // 38
//			new Color(0x46523A), // 39
//			new Color(0x46523A), // 39
//	};

//	// PAO 38
//	private static final int[][] pairs = {
//			{0, 1}, {0, 8}, {0, 32}, {0, 20}, {0, 29}, {0, 37}, {1, 30}, {2, 30}, {2, 3}, {3, 4},
//			{4, 13}, {4, 14}, {4, 34}, {5, 34}, {5, 35}, {6, 35}, {6, 29}, {7, 31}, {7, 35}, {8, 31}, {8, 9}, {9, 11},
//			{10, 12}, {10, 32}, {11, 17}, {17, 18}, {12, 13}, {14, 15}, {14, 23},
//			{15, 16}, {16, 17}, {16, 19}, {19, 20},
//			{21, 22}, {21, 37}, {22, 23}, {24, 34}, {24, 36},
//			{25, 26}, {25, 34}, {26, 27}, {27, 28}, {28, 29},
//			{33, 36}, {33, 37}};

//	private static final int[][] pairs = {};

	private static final int SIZE   = 256 * 6;
	private static final int BORDER = 64;

	public static final  Stroke DEFAULT_ROUND_STROKE =
			new BasicStroke(1, CAP_ROUND, JOIN_ROUND);
	private static final Stroke NORMAL_STROKE        =
			new BasicStroke(2, CAP_ROUND, JOIN_ROUND);
	private static final Stroke DASHED_STROKE        =
			new BasicStroke(1, CAP_ROUND, JOIN_ROUND, 0, new float[]{8.0f, 8.0f}, 0.0f);
	private static final Stroke DOTTED_STROKE        =
			new BasicStroke(2, CAP_ROUND, JOIN_ROUND, 0, new float[]{2.0f, 14.0f}, 0.0f);

	private static final Comparator<Dot> SORT_BY_ID  = Comparator.comparingInt(dot -> dot.id);
	private static final Comparator<Dot> SORT_BY_LUM = Comparator.comparingDouble(dot -> dot.hsi.b);

	private static JFrame frame;

	private static final List<Dot> oldDots = makeDots(oldColors, false);
	private static final List<Dot> newDots = makeDots(newColors, true);

	private final BufferedImage img  = new BufferedImage(SIZE + BORDER * 2, SIZE, BufferedImage.TYPE_INT_RGB);
	private final Graphics2D    imgG = img.createGraphics();

	private static final Color[] bgColors     = {Color.WHITE, Color.GRAY, Color.BLACK};
	private              int     bgColorIndex = 2;

	private int mouseX = 0;
	private int mouseY = 0;

	private final Dot     cursorDot  = new Dot();
	private       boolean drawCursor = false;

	private @Nullable Dot selectedDot;

	private static Color[] loadPal(String filename) throws IOException {
		Path         path  = Paths.get("/home/zom-b/Data/Palettes/" + filename);
		List<String> lines = Files.readAllLines(path);

		Color[] colors = new Color[lines.size() - 3];

		for (int i = 0; i < colors.length; i++) {
			String[] parts = lines.get(i + 3).split(" ");

			int r = Integer.parseInt(parts[0]);
			int g = Integer.parseInt(parts[1]);
			int b = Integer.parseInt(parts[2]);
			colors[i] = new Color(r, g, b);
		}

		return colors;
	}

	private static List<Dot> makeDots(Color[] colors, boolean newColors) {
		float radiusFactor = newColors ? 0.02f : 0.04f;

		int id = newColors ? 0 : -colors.length;

		List<Dot> dots = new ArrayList<>(colors.length);
		for (Color color : colors) {
			dots.add(new Dot(color, id, radiusFactor));
			id++;
		}

		return dots;
	}

	public static void main(String... args) {
		SwingUtilities.invokeLater(() -> {
			frame = new JFrame();
			frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

			frame.setContentPane(new PaletteDesignerMain());

			frame.pack();
			frame.setLocationRelativeTo(null);
			frame.setVisible(true);
		});
	}

	@SuppressWarnings("OverridableMethodCallDuringObjectConstruction")
	public PaletteDesignerMain() {
		super(null);
		setPreferredSize(new Dimension(BORDER * 2 + SIZE + 500, BORDER * 2 + SIZE));
		setBackground(new Color(0x222222));

		GraphicsUtilities.setAntialiased(imgG, true);

		addMouseListener(this);
		addMouseMotionListener(this);
		addMouseWheelListener(this);
		addKeyListener(this);

		setFocusable(true);
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);

		if (bgColorIndex == -1) {
			imgG.setPaint(new LinearGradientPaint(BORDER,
			                                      0,
			                                      BORDER + SIZE,
			                                      0,
			                                      new float[]{0, 1},
			                                      new Color[]{Color.BLACK, Color.WHITE}));
		} else {
			imgG.setPaint(bgColors[bgColorIndex]);
		}
		imgG.fillRect(0, 0, SIZE + BORDER * 2, SIZE);

		imgG.setStroke(StaticStrokes.DEFAULT_ROUND_STROKE);

		for (int i = 0; i <= 6; i++) {
			int y = SIZE * i / 6;
			imgG.setPaint(ColorMode.HSL.toRGB(new Color3f(i / 6.0f, 1.0f, 0.5f)).toColor());
			imgG.drawLine(BORDER, y, BORDER + SIZE, y);
		}

		imgG.setPaint(Color.BLACK);
		imgG.drawLine(BORDER, 0, BORDER, SIZE);
		imgG.drawLine(BORDER + SIZE, 0, BORDER + SIZE, SIZE);

		imgG.setStroke(NORMAL_STROKE);

		oldDots.sort(SORT_BY_ID);
		newDots.sort(SORT_BY_ID);

		imgG.setPaint(bgColorIndex == 2 ? Color.GRAY : Color.BLACK);
		for (int[] pair : pairs) {
			lineBetweenDots(findDot(pair[0]), findDot(pair[1]));
		}

//		imgG.setPaint(Color.WHITE);
//		imgG.setStroke(DOTTED_STROKE);
//		for (int i = 0; i < oldColors.length; i++) {
//			lineBetweenDots(oldColors[i], newColors[i]);
//		}

		oldDots.sort(SORT_BY_LUM);
		newDots.sort(SORT_BY_LUM);

		imgG.setStroke(NORMAL_STROKE);

		for (Dot dot : oldDots) {
			dot.paint(imgG);
		}

		for (Dot dot : newDots) {
			dot.paint(imgG);
		}

		imgG.setPaint(bgColorIndex == 2 ? Color.GRAY : Color.BLACK);
		for (Dot dot : newDots) {
			dot.paintSat(imgG);
		}

		if (drawCursor) {
			cursorDot.paint(imgG);
		}

		g.drawImage(img, 0, BORDER - SIZE, null);
		g.drawImage(img, 0, BORDER, null);
		g.drawImage(img, 0, BORDER + SIZE, null);

		g.setColor(new Color(0xBBBBBB));
		int x = BORDER * 3 + SIZE;
		int y = 3;
		g.drawString("Solid dots are current palette", x, BORDER * y++);
		g.drawString("Hollow dots are old palette", x, BORDER * y++);
		g.drawString("Dotted lines indicate saturation, as well as the dot size", x, BORDER * y++);
		g.drawString("Solid lines between dots are hardcoded and serve no meaning", x, BORDER * y++);
		y++;
		g.drawString("Left drag - change hue", x, BORDER * y++);
		g.drawString("Right drag - change lightness", x, BORDER * y++);
		g.drawString("Middle drag - change saturation", x, BORDER * y++);
		g.drawString("Insert - Add point", x, BORDER * y++);
		g.drawString("Delete - Delete point (can't be undone)", x, BORDER * y++);
		g.drawString("Scroll wheel - Change point index", x, BORDER * y++);
		g.drawString("Click - Change background", x, BORDER * y++);
		y++;
		g.drawString("Mouse buttons can be combined when dragging", x, BORDER * y++);
		y++;
		g.drawString("Sometimes it registers a click when", x, BORDER * y++);
		g.drawString("you intended to drag a little", x, BORDER * y++);
	}

	private void lineBetweenDots(Dot dot0, Dot dot1) {
		assert dot0.hsi != null;
		assert dot1.hsi != null;

		float x0 = BORDER + SIZE * dot0.hsi.b;
		float x1 = BORDER + SIZE * dot1.hsi.b;
		float y0 = dot0.hsi.r;
		float y1 = dot1.hsi.r;

		if (dot0.hsi.b < 0.999f && dot1.hsi.b < 0.001f) {
			y1 = y0;
		} else if (dot0.hsi.b > 0.001f && dot1.hsi.b > 0.999f) {
			y1 = y0;
		} else if (dot1.hsi.b < 0.999f && dot0.hsi.b < 0.001f) {
			y0 = y1;
		} else if (dot1.hsi.b > 0.001f && dot0.hsi.b > 0.999f) {
			y0 = y1;
		}

		Line2D.Float line = new Line2D.Float();
		if (Math.abs(y0 - y1) <= 0.5) {
			line.setLine(x0, SIZE * y0,
			             x1, SIZE * y1);
		} else if (y0 < y1) {
			line.setLine(x0, SIZE * y0,
			             x1, SIZE * (y1 - 1));
			imgG.draw(line);
			line.setLine(x0, SIZE * (y0 + 1),
			             x1, SIZE * y1);
		} else {
			line.setLine(x0, SIZE * y0,
			             x1, SIZE * (y1 + 1));
			imgG.draw(line);
			line.setLine(x0, SIZE * (y0 - 1),
			             x1, SIZE * y1);
		}
		imgG.draw(line);
	}

	private static Dot findDot(int id) {
		for (Dot dot : newDots) {
			if (dot.id == id) {
				return dot;
			}
		}

		throw new AssertionError("Dot with id not found: " + id);
	}

	private static class Dot {
		int     id;
		Color3f hsi;
		float   radius;
		boolean filled;

		// Selection
		Dot() {
			id = 0;
			hsi = new Color3f(0, 1.0f, 0);
			radius = SIZE * 0.03f;
			filled = true;
		}

		// Palette color
		Dot(Color color, int id, float radiusFactor) {
			this.id = id;
			hsi = ColorUtilities.toPerceptualHSL(new Color3f(color));
			radius = SIZE * radiusFactor;
			filled = id >= 0;
		}

		// Copy constructor
		Dot(Dot other, int id) {
			this.id = id;
			hsi = other.hsi;
			radius = SIZE * 0.02f;
			filled = other.filled;
		}

		public void paintSat(Graphics2D g) {
			if (hsi == null) {
				return;
			}

			assert id >= 0;

			float x         = BORDER + hsi.b * SIZE;
			float y         = hsi.r * SIZE;
			float maxSat    = Math.min(0.5f, 1 - ColorMode.HSL.fromRGB(getRGB()).b) * 2;
			float visualSat = hsi.g * maxSat;
			float satY      = SIZE * (1 - visualSat);

			g.setStroke(DASHED_STROKE);
			g.draw(new Line2D.Float(x, y, x, satY));
			g.setStroke(NORMAL_STROKE);

			Shape satDot = new Ellipse2D.Float(x - 5, satY - 5, 10, 10);
			g.fill(satDot);

//			dotSat(g, x, y, visualSat);
//			dotSat(g, x, y + SIZE, visualSat);
//			dotSat(g, x, y - SIZE, visualSat);
		}

		public void paint(Graphics2D g) {
			if (hsi == null) {
				return;
			}

			float x = BORDER + hsi.b * SIZE;
			float y = hsi.r * SIZE;

			dot(g, x, y);
			dot(g, x, y + SIZE);
			dot(g, x, y - SIZE);

			if (id < 0) {
				g.setPaint(getRGB().toColor());
				g.drawString(Integer.toString(oldDots.size() + id + 1), (int)x - 5, (int)y + 5);
			} else if (filled) {
				g.setPaint(hsi.b > 0.25 ? Color.BLACK : Color.GRAY);
				g.drawString(Integer.toString(id), (int)x - 5, (int)y + 5);
			}
		}

		private void dotSat(Graphics2D g, float x, float y, float visualSat) {
			//0 -> 0.75f
			//  -> satRX / 2
			//1 -> satRX
			float h = (float)Math.pow(visualSat, 0.06125);
			if (h < 0.2f) {
				return;
			}

//			float satRX = SIZE / (float)Math.sqrt(newColors.length) / 4.0f;
			float satRX = SIZE / 64.0f;
			float satRY = NumberUtilities.lerp(SIZE, satRX, h);

			RectangularShape ellipse = new Ellipse2D.Float();
			ellipse.setFrameFromCenter(x, y, x + satRX, y + satRY);

			g.draw(ellipse);
		}

		private void dot(Graphics2D g, float x, float y) {
			g.setPaint(getRGB().toColor());

			float maxSat = Math.min(0.5f, 1 - ColorMode.HSL.fromRGB(getRGB()).b) * 2;
			float radius = NumberUtilities.lerp(this.radius / 3, this.radius, hsi.g * maxSat);

			Shape circle = new Ellipse2D.Float(x - radius, y - radius, radius * 2, radius * 2);
			if (filled) {
				g.fill(circle);
			} else {
				g.draw(circle);
			}
		}

		public float distanceSquared(float x, float y) {
			if (hsi == null) {
				return Float.NaN;
			}

			float dx = hsi.b - x;
			float dy = hsi.r - y;
			return dx * dx + dy * dy;
		}

		public Color3f getRGB() {
			if (hsi == null) {
				return new Color3f(0);
			}

			return toRGB(hsi.r, hsi.g, ColorUtilities.fromSRGB(hsi.b));
		}

		@Override
		public String toString() {
			return String.format("%d - 0x%s (%5.3f, %5.3f, %5.3f)",
			                     id, HexUtilities.toColorString(getRGB().toInteger()),
			                     hsi.r, hsi.g, hsi.b);
		}
	}

	private static Color3f toRGB(float hue, float sat, float targetLum) {
		float minLum = 0;
		float maxLum = 1;

		Color3f hsl = new Color3f(hue, sat, targetLum);
		Color3f rgb = null;

		while (maxLum - minLum > 0.000001) {
			float lum = (maxLum + minLum) / 2;
			hsl = new Color3f(hsl.r, hsl.g, lum);
			rgb = ColorMode.HSL.toRGB(hsl);

			lum = ColorUtilities.getPerceptualLuminosity(rgb);

			if (lum < targetLum) {
				minLum = hsl.b;
			} else {
				maxLum = hsl.b;
			}
		}

		assert rgb != null;
		return rgb;
	}

	@Override
	public void mouseEntered(MouseEvent e) {
	}

	@Override
	public void mouseExited(MouseEvent e) {
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		mouseX = e.getX();
		mouseY = e.getY();
		float x = (mouseY - BORDER) / (float)SIZE;
		float y = (mouseX - BORDER) / (float)SIZE;

		float minDist = 0.5f * BORDER * BORDER / SIZE / SIZE;
		selectedDot = null;
		for (int i = 0; i < newDots.size(); i++) {
			Dot   dot  = newDots.get(i);
			float dist = dot.distanceSquared(y, x);
			if (dist <= minDist) {
				minDist = dist;
				selectedDot = dot;
			}
		}

		if (selectedDot != null) {
			frame.setTitle(selectedDot.toString());
			cursorDot.hsi = selectedDot.hsi;
			cursorDot.filled = false;
			drawCursor = true;
		} else {
			frame.setTitle("");
			cursorDot.hsi = new Color3f(x, cursorDot.hsi.g, y);
			cursorDot.filled = true;
			drawCursor = false;
		}

		repaint();
	}

	@Override
	public void mousePressed(MouseEvent e) {
		mouseX = e.getX();
		mouseY = e.getY();
		drawCursor = true;
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		int oldY = mouseY;
		mouseX = e.getX();
		mouseY = e.getY();
		float x  = (mouseY - BORDER) / (float)SIZE;
		float y  = (mouseX - BORDER) / (float)SIZE;
		float dy = (oldY - mouseY) / (float)SIZE;

		if (selectedDot != null) {
			float hue = selectedDot.hsi.r;
			float sat = selectedDot.hsi.g;
			float lum = selectedDot.hsi.b;

			if ((e.getModifiersEx() & InputEvent.BUTTON2_DOWN_MASK) != 0) {
				sat += dy;
			} else if ((e.getModifiersEx() & InputEvent.BUTTON1_DOWN_MASK) != 0) {
				hue = x;
			}

			if ((e.getModifiersEx() & InputEvent.BUTTON3_DOWN_MASK) != 0) {
				lum = y;
			}

			hue = NumberUtilities.clamp(hue, 0.0f, 1.0f);
			sat = NumberUtilities.clamp(sat, 0.0f, 1.0f);
			lum = NumberUtilities.clamp(lum, 0.0f, 1.0f);
			selectedDot.hsi = new Color3f(hue, sat, lum);

			frame.setTitle(selectedDot.toString());
		} else {
			cursorDot.hsi = new Color3f(x, cursorDot.hsi.g, y);
		}

		repaint();
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		drawCursor = selectedDot != null;

		repaint();
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		newDots.sort(SORT_BY_ID);

		dumpColors();

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

		bgColorIndex = (bgColorIndex + 2) % (bgColors.length + 1) - 1;
		repaint();
	}

	@Override
	public void keyTyped(KeyEvent e) {
	}

	@Override
	public void keyPressed(KeyEvent e) {
		if (e.getKeyCode() == KeyEvent.VK_INSERT) {
			if (selectedDot == null) {
				newDots.add(new Dot(cursorDot, newDots.size()));
			}
		} else if (e.getKeyCode() == KeyEvent.VK_DELETE) {
			if (selectedDot != null) {
				deleteDot(selectedDot.id);
			}
		}

		repaint();
	}

	@Override
	public void keyReleased(KeyEvent e) {
	}

	@Override
	public void mouseWheelMoved(MouseWheelEvent e) {
		if (selectedDot == null) {
			return;
		}

		int from = selectedDot.id;
		int to   = NumberUtilities.clamp(from - e.getWheelRotation(), 0, newDots.size() - 1);
		moveDot(from, to);

		repaint();
	}

	private void deleteDot(int id) {
		if (id != newDots.size() - 1) {
			moveDot(id, newDots.size() - 1);
			id = newDots.size() - 1;
		} else {
			newDots.sort(SORT_BY_ID);
		}

		newDots.remove(id);

		for (Iterator<int[]> iterator = pairs.iterator(); iterator.hasNext(); ) {
			int[] pair = iterator.next();
			if (pair[0] == id || pair[1] == id) {
				iterator.remove();
			}
		}
	}

	private void moveDot(int from, int to) {
		if (from == to) {
			return;
		}

		int direction = from < to ? 1 : -1;

		newDots.sort(SORT_BY_ID);

		for (int i = from; i != to; i += direction) {
			swapDots(i, i + direction);
		}

		selectedDot = newDots.get(to);
	}

	private static void swapDots(int firstID, int secondID) {
		Dot firstDot  = newDots.get(firstID);
		Dot secondDot = newDots.get(secondID);

		firstDot = new Dot(firstDot, secondID);
		secondDot = new Dot(secondDot, firstID);

		newDots.set(firstID, secondDot);
		newDots.set(secondID, firstDot);

		for (int[] pair : pairs) {
			for (int j = 0; j < pair.length; j++) {
				if (pair[j] == firstID) {
					pair[j] = secondID;
				} else if (pair[j] == secondID) {
					pair[j] = firstID;
				}
			}
		}
	}

	private static void dumpColors() {
		System.out.println();

		for (int[] pair : pairs) {
			if (pair[0] > pair[1]) {
				int temp = pair[0];
				pair[0] = pair[1];
				pair[1] = temp;
			}
		}

		pairs.sort(Comparator.<int[]>comparingInt(pair -> pair[0]).thenComparingInt(pair -> pair[1]));

		for (int i = 0; i < pairs.size(); i++) {
			int[] pair = pairs.get(i);
			System.out.print("new int[]{" + pair[0] + ", " + pair[1] + '}');

			if (i < pairs.size() - 1) {
				System.out.println(',');
			} else {
				System.out.println();
			}
		}

		System.out.println();

		for (Dot dot : newDots) {
			System.out.println("new Color(0x" + HexUtilities.toColorString(dot.getRGB().toInteger()) +
			                   "), // " + dot.id);
		}
	}
}
