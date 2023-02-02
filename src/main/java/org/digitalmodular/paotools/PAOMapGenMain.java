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

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.IntSummaryStatistics;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.WindowConstants;

import org.jetbrains.annotations.NotNull;

import org.digitalmodular.utilities.StringUtilities;
import org.digitalmodular.utilities.constant.NumberConstants;
import org.digitalmodular.utilities.container.UnsignedInteger;
import org.digitalmodular.utilities.graphics.GraphicsUtilities;
import org.digitalmodular.utilities.graphics.color.Color3f;
import org.digitalmodular.utilities.graphics.color.ColorUtilities;

import org.digitalmodular.paotools.common.HashArrayList;
import static org.digitalmodular.paotools.common.PaoUtilities.TRANSPARENT;

/**
 * @author Mark Jeronimus
 */
// Created 2020-10-25
public class PAOMapGenMain extends JPanel {
	private static final int       DIV       = 1;
	private static final int       NUM_TEAMS = 3;
	private static final Dimension SIZE      = new Dimension(2000 / DIV, 1000 / DIV);

	private static final int OUTSIDE_COLOR    = TRANSPARENT;
	private static final int WALL_COLOR       = 0xFF000001;
	private static final int BACKGROUND_COLOR = 0xFFFFFFFF;

	private static final int[] COLORS = new int[NUM_TEAMS];

	private static final BufferedImage img                  =
			new BufferedImage(SIZE.width, SIZE.height, BufferedImage.TYPE_INT_RGB);
	private static final int           HALF_WIDTH           = img.getWidth() / 2;
	private static final int           HALF_HEIGHT          = img.getHeight() / 2;
	private static final double        CENTER_CIRCLE_RADIUS = 0.7f;
	private static final int           RANDOM_BIAS          = 512;

	private static       double                     startAngle = 0;
	private static final List<HashArrayList<Point>> seeds      = new ArrayList<>(3);

	@SuppressWarnings({"OverlyBroadThrowsClause", "ProhibitedExceptionDeclared"})
	public static void main(String... args) throws Exception {
		SwingUtilities.invokeLater(() -> {
			GraphicsUtilities.setNiceLookAndFeel();

			JFrame f = new JFrame();
			f.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

			f.setContentPane(new PAOMapGenMain());

			f.pack();
			f.setLocationRelativeTo(null);
			f.setVisible(true);
		});

		for (int team = 0; team < NUM_TEAMS; team++) {
			seeds.add(new HashArrayList<>(img.getWidth() * 8));
		}

		//noinspection InfiniteLoopStatement
		while (true) {
			startAngle = ThreadLocalRandom.current().nextDouble();
			makeColors();

			makeMap();
			makeBorders();
			List<HashArrayList<Point>> teamPixels = findTeamPixels();
			eraseNonTeamPixels(teamPixels);

			int[] teamCounts = teamPixels.stream()
			                             .mapToInt(HashArrayList::size)
			                             .toArray();

			fillGaps(teamCounts);

			IntSummaryStatistics minMax = Arrays.stream(teamCounts).summaryStatistics();

			int err = minMax.getMax() - minMax.getMin();
			System.out.println(Arrays.toString(teamCounts) + ": " + err);

			String errString = StringUtilities.fixLeft(Integer.toString(err), 6);
			ImageIO.write(img,
			              "PNG",
			              new File("/home/zom-b/Pictures/Pixelart/PAO/Conquest#2/3teams-" + errString + ".png"));

			Thread.yield();
		}
	}

	private static void makeColors() {
		for (int i = 0; i < NUM_TEAMS; i++) {
			float   hue = i / (float)NUM_TEAMS + (float)startAngle;
			Color3f hsv = new Color3f(hue, 1.0f, 1.0f);
			COLORS[i] = 0xFF000000 | ColorUtilities.hsv2rgb(hsv).toInteger();
		}
	}

	public static void makeMap() {
		init();
		makeCenterCircle();
		makeSeeds();

		int iter = 0;
		do {
			iterateTeams(iter);
			iter++;
		} while (pixelsRemaining());
	}

	private static void makeCenterCircle() {
		int reach = (int)Math.ceil(CENTER_CIRCLE_RADIUS);
		for (int v = -reach; v < reach; v++) {
			for (int u = -reach; u < reach; u++) {
				if (isCenterCircle(HALF_WIDTH + u, HALF_HEIGHT + v)) {
					img.setRGB(HALF_WIDTH + u, HALF_HEIGHT + v, WALL_COLOR);
				}
			}
		}
	}

	public static void init() {
		clear(BACKGROUND_COLOR);

		seeds.forEach(HashArrayList::clear);
	}

	public static void clear(int rgb) {
		img.setAccelerationPriority(0);
		int[] pixels = ((DataBufferInt)img.getRaster().getDataBuffer()).getData();
		Arrays.fill(pixels, rgb);
	}

	public static void makeSeeds() {
		for (int team = 0; team < NUM_TEAMS; team++) {
			double theta = ((team / (double)NUM_TEAMS) + startAngle) * NumberConstants.TAU;
			int    x     = HALF_WIDTH + (int)Math.rint(CENTER_CIRCLE_RADIUS * Math.sin(theta) - 0.5);
			int    y     = HALF_HEIGHT - (int)Math.rint(CENTER_CIRCLE_RADIUS * Math.cos(theta) - 0.5);

			addTeamPixel(x, y, team);
		}
	}

	private static void iterateTeams(int iter) {
		for (int team = 0; team < NUM_TEAMS; team++) {
			iterateTeam(team);
		}
	}

	private static void iterateTeam(int team) {
		while (true) {
			HashArrayList<Point> seedsForTeam = seeds.get(team);
			if (seedsForTeam.isEmpty()) {
				return;
			}

			int   i = biasedRandom(seedsForTeam.size());
			Point p = seedsForTeam.get(i);
			seedsForTeam.remove(i);

			if (img.getRGB(p.x, p.y) != BACKGROUND_COLOR) {
				continue;
			}

			addTeamPixel(p.x, p.y, team);

			return;
		}
	}

	private static int biasedRandom(int bound) {
		return ThreadLocalRandom.current().nextInt(Math.max(0, bound - RANDOM_BIAS), bound);
	}

	public static void addTeamPixel(int x, int y, int team) {
		HashArrayList<Point> seedsForTeam = seeds.get(team);

		for (int dy = -1; dy <= 1; dy++) {
			for (int dx = -1; dx <= 1; dx++) {
				if ((dx == 0 || dy == 0) && (dx != 0 || dy != 0)) {
					int rgb = getRGBSafe(x + dx, y + dy);
					if (rgb == BACKGROUND_COLOR) {
						seedsForTeam.add(new Point(x + dx, y + dy));
					}
				}
			}
		}

		img.setRGB(x, y, COLORS[team]);
	}

	private static boolean pixelsRemaining() {
		for (int team = 0; team < NUM_TEAMS; team++) {
			if (!seeds.get(team).isEmpty()) {
				return true;
			}
		}

		return false;
	}

	private static void makeBorders() {
		List<Point> borderPixels = findBorderPixels();

		for (Point p : borderPixels) {
			img.setRGB(p.x, p.y, WALL_COLOR);
		}
	}

	@NotNull
	private static List<Point> findBorderPixels() {
		List<Point> borderPixels = new ArrayList<>(Math.max(img.getWidth(), img.getHeight()) * 32);

		for (int y = 0; y < img.getHeight(); y++) {
			for (int x = 0; x < img.getWidth(); x++) {
				int rgb = img.getRGB(x, y);
				if (rgb == WALL_COLOR) {
					continue;
				}
				if (isBorderPixel(x, y)) {
					borderPixels.add(new Point(x, y));
				}
			}
		}
		return borderPixels;
	}

	private static boolean isBorderPixel(int x, int y) {
		int foundTeam = -1;
		for (int dy = -1; dy <= 1; dy++) {
			for (int dx = -1; dx <= 1; dx++) {
				if (dx == 0 && dy == 0) {
					continue;
				}

				int rgb = getRGBSafe(x + dx, y + dy);
				if (rgb == OUTSIDE_COLOR || rgb == WALL_COLOR) {
					continue;
				}

				int team = teamOf(rgb);

				if (foundTeam == -1) {
					foundTeam = team;
				} else if (foundTeam != team) {
					return true;
				}
			}
		}

		return false;
	}

	private static int getRGBSafe(int x, int y) {
		if (x < 0 || y < 0 || x >= img.getWidth() || y >= img.getHeight()) {
			return OUTSIDE_COLOR;
		}

		return img.getRGB(x, y);
	}

	private static List<HashArrayList<Point>> findTeamPixels() {
		List<HashArrayList<Point>> teamPixels = new ArrayList<>(NUM_TEAMS);

		for (int team = 0; team < NUM_TEAMS; team++) {
			Point p = findTeamPixel(team);

			teamPixels.add(floodFill(p.x, p.y));
		}

		return teamPixels;
	}

	private static Point findTeamPixel(int team) {
		double theta = ((team / (double)NUM_TEAMS) + startAngle) * NumberConstants.TAU;
		int    x     = HALF_WIDTH + (int)Math.rint(CENTER_CIRCLE_RADIUS * Math.sin(theta) - 0.5);
		int    y     = HALF_HEIGHT - (int)Math.rint(CENTER_CIRCLE_RADIUS * Math.cos(theta) - 0.5);

		int rgb = img.getRGB(x, y);
		if (rgb == COLORS[team]) {
			return new Point(x, y);
		}

		for (int r = 8; r <= 256; r += 8) {
			for (int i = 0; i < 8; i++) {
				theta = (i / 8.0) * NumberConstants.TAU;
				int x2 = x + (int)Math.rint(r * Math.sin(theta));
				int y2 = y - (int)Math.rint(r * Math.cos(theta));

				boolean isTeam = isSolid3x3(x2, y2, COLORS[team]);
				if (isTeam) {
					return new Point(x2, y2);
				}

//				img.setRGB(x2, y2, img.getRGB(x2, y2) | 0xFFAAAAAA);
			}
		}

		throw new IllegalArgumentException(
				"Can't find team " + UnsignedInteger.toHexString(COLORS[team], 6) + " @ " + x + ", " + y);
	}

	private static boolean isSolid3x3(int x, int y, int rgbToFind) {
		for (int dy = -1; dy <= 1; dy++) {
			for (int dx = -1; dx <= 1; dx++) {
				int rgb = getRGBSafe(x + dx, y + dy);
				if (rgb != rgbToFind) {
					return false;
				}
			}
		}

		return true;
	}

	private static HashArrayList<Point> floodFill(int x, int y) {
		int rgb = img.getRGB(x, y);
		try {
			teamOf(rgb);
		} catch (IllegalArgumentException ex) {
			throw new IllegalArgumentException(ex.getMessage() + " @  " + x + ", " + y);
		}

		HashArrayList<Point> points = new HashArrayList<>(img.getWidth() * img.getHeight());

		points.add(new Point(x, y));
//		img.setRGB(x,y,BACKGROUND_COLOR);
		int rescanFromIndex = 0;

		while (true) {
			int rescanToIndex = points.size();
			if (rescanToIndex == rescanFromIndex) {
				break;
			}

			findNeighbors(rgb, points, rescanFromIndex, rescanToIndex);

			rescanFromIndex = rescanToIndex;
		}

		points.sort(Comparator.comparingInt((Point p) -> p.y)
		                      .thenComparingInt(p -> p.x));

		return points;
	}

	private static void findNeighbors(int rgb, HashArrayList<Point> points, int from, int to) {
		for (int i = from; i < to; i++) {
			Point point = points.get(i);
			int   x     = point.x;
			int   y     = point.y;

			Point p = new Point(x, y - 1);
			if (!points.contains(p) && getRGBSafe(x, y - 1) == rgb) {
				img.setRGB(x, y - 1, BACKGROUND_COLOR);
				points.add(p);
			}

			p = new Point(x, y + 1);
			if (!points.contains(p) && getRGBSafe(x, y + 1) == rgb) {
				img.setRGB(x, y + 1, BACKGROUND_COLOR);
				points.add(p);
			}

			p = new Point(x - 1, y);
			if (!points.contains(p) && getRGBSafe(x - 1, y) == rgb) {
				img.setRGB(x - 1, y, BACKGROUND_COLOR);
				points.add(p);
			}

			p = new Point(x + 1, y);
			if (!points.contains(p) && getRGBSafe(x + 1, y) == rgb) {
				img.setRGB(x + 1, y, BACKGROUND_COLOR);
				points.add(p);
			}
		}
	}

	private static void eraseNonTeamPixels(List<HashArrayList<Point>> teamPixels) {
		clear(WALL_COLOR);

		for (int team = 0; team < NUM_TEAMS; team++) {
			HashArrayList<Point> pixels = teamPixels.get(team);
			for (Point p : pixels) {
				img.setRGB(p.x, p.y, COLORS[team]);
			}
		}
	}

	private static void fillGaps(int[] teamCounts) {
		while (true) {
			boolean changed = fillGapsIteration(teamCounts, true);
			if (!changed) {
				changed = fillGapsIteration(teamCounts, false);
				if (!changed) {
					break;
				}
			}
		}
	}

	private static int findWorstTeam(int[] teamCounts) {
		int worstTeam  = -1;
		int worstCount = Integer.MAX_VALUE;
		for (int team = 0; team < NUM_TEAMS; team++) {
			if (teamCounts[team] < worstCount) {
				worstCount = teamCounts[team];
				worstTeam = team;
			}
		}
		return worstTeam;
	}

	private static boolean fillGapsIteration(int[] teamCounts, boolean doTeamFilter) {
		List<Point> candidates = findFillGapCandidates();

		Collections.shuffle(candidates, ThreadLocalRandom.current());

		int worstTeam = -1;
		if (doTeamFilter) {
			worstTeam = findWorstTeam(teamCounts);
		}

		boolean changed = false;

		for (Point p : candidates) {
			int team = findFillTeam(p.x, p.y);
			if (team == -1) {
				continue;
			}

			if (doTeamFilter && team != worstTeam) {
				continue;
			}

			img.setRGB(p.x, p.y, COLORS[team]);

			teamCounts[team]++;
			if (doTeamFilter) {
				worstTeam = findWorstTeam(teamCounts);
			}

			changed = true;
		}

		return changed;
	}

	private static List<Point> findFillGapCandidates() {
		List<Point> candidates = new ArrayList<>(Math.max(img.getWidth(), img.getHeight()) * 32);

		for (int y = 0; y < img.getHeight(); y++) {
			for (int x = 0; x < img.getWidth(); x++) {
				int rgb = img.getRGB(x, y);
				if (rgb != WALL_COLOR) {
					continue;
				}

				if (isCenterCircle(x, y)) {
					continue;
				}

				candidates.add(new Point(x, y));

			}
		}

		return candidates;
	}

	private static boolean isCenterCircle(int x, int y) {
		float dx = x - HALF_WIDTH + 0.5f;
		float dy = y - HALF_HEIGHT + 0.5f;
		return dx * dx + dy * dy < CENTER_CIRCLE_RADIUS * (CENTER_CIRCLE_RADIUS - 1);
	}

	private static int findFillTeam(int x, int y) {
		int foundTeam = -1;

		for (int dy = -2; dy <= 2; dy++) {
			for (int dx = -2; dx <= 2; dx++) {
				int rgb = getRGBSafe(x + dx, y + dy);
				if (rgb == OUTSIDE_COLOR || rgb == WALL_COLOR) {
					continue;
				}

				int team = teamOf(rgb);

				if (foundTeam == -1) {
					foundTeam = team;
				} else if (foundTeam != team) {
					return -1;
				}
			}
		}

		return foundTeam;
	}

	private static int teamOf(int rgb) {
		for (int team = 0; team < NUM_TEAMS; team++) {
			if (rgb == COLORS[team]) {
				return team;
			}
		}

		throw new IllegalArgumentException("Not a team color: " + UnsignedInteger.toHexString(rgb, 8));
	}

	@SuppressWarnings("OverridableMethodCallDuringObjectConstruction")
	public PAOMapGenMain() {
		super(null);
		setPreferredSize(new Dimension(2000, 1000));

		new Timer(50, ignored -> repaint()).start();
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		g.drawImage(img, 0, 0, getWidth(), getHeight(), 0, 0, SIZE.width, SIZE.height, null);
	}
}
