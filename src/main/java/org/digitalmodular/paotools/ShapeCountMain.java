package org.digitalmodular.paotools;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.WindowConstants;

import org.jetbrains.annotations.Nullable;

import org.digitalmodular.utilities.graphics.GraphicsUtilities;

import org.digitalmodular.paotools.common.HashArrayList;

/**
 * @author Mark Jeronimus
 */
// Created 2020-10-24
@SuppressWarnings("UseOfSystemOutOrSystemErr")
public final class ShapeCountMain extends JPanel {
	private static final String FILENAME = "PAO_Canvas_2020_10_27.png";

	private static final int TRANSPARENT = 0x00000000;

	private static BufferedImage img = null;

	@SuppressWarnings({"OverlyBroadThrowsClause", "ProhibitedExceptionDeclared"})
	public static void main(String... args) throws Exception {
		img = ImageIO.read(new File("/home/zom-b/Pictures/Pixelart/PAO/Canvases/" + FILENAME));

		SwingUtilities.invokeLater(() -> {
			GraphicsUtilities.setNiceLookAndFeel();

			JFrame f = new JFrame();
			f.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

			f.setContentPane(new ShapeCountMain());

			f.pack();
			f.setLocationRelativeTo(null);
			f.setVisible(true);
		});

		HashArrayList<PixelShape> shapes = new HashArrayList<>(65536);

		Point p = new Point();
		while (nextUnvisitedPixel(img, p)) {
			iteration(p, shapes);
		}

		shapes.sort(Comparator.comparingInt(PixelShape::getCount)
		                      .thenComparingInt(s -> s.pixels.size()));

		for (PixelShape shape : shapes) {
			System.out.println(shape.getCount() + ": " + shape.pixels.size() +
			                   " (" + shape.width + '×' + shape.height + ") @" + shape.coord);
			System.out.println(shape);
		}
	}

	public static void iteration(Point p, HashArrayList<PixelShape> shapes) {
		@Nullable PixelShape shape = findShape(p);
		if (shape == null)
			return;

		@Nullable PixelShape existingShape = shapes.get(shape);
		if (existingShape == null)
			shapes.add(shape);
		else
			existingShape.incrementCount();
	}

	@Nullable
	public static PixelShape findShape(Point p) {
		HashArrayList<Point> pixels = findShapePixels(img, p.x, p.y);
		eraseShape(img, pixels);

		if (pixels.size() <= 2 || pixels.size() >= 10000)
			return null;

		Point coord = moveToOrigin(pixels);

		PixelShape shape = new PixelShape(pixels, coord);
		if (shape.width == 1 || shape.height == 1)
			return null;

		return shape;
	}

	private static boolean nextUnvisitedPixel(BufferedImage img, Point p) {
		int width = img.getWidth();

		while (true) {
			int rgb = img.getRGB(p.x, p.y);
			if (rgb != TRANSPARENT)
				return true;

			p.x++;
			if (p.x == width) {
				p.x = 0;
				p.y++;
				if (p.y == img.getHeight())
					return false;
			}
		}
	}

	static HashArrayList<Point> findShapePixels(BufferedImage img, int x, int y) {
		int color = getRGBSafe(img, x, y);

		HashArrayList<Point> points = new HashArrayList<>(img.getWidth());

		points.add(new Point(x, y));
		int rescanFromIndex = 0;

		while (true) {
			int rescanToIndex = points.size();
			if (rescanToIndex == rescanFromIndex)
				break;

			findNeighbors(img, color, points, rescanFromIndex, rescanToIndex);

			rescanFromIndex = rescanToIndex;
		}

		points.sort(Comparator.comparingInt((Point p) -> p.x)
		                      .thenComparingInt(p -> p.y));

		return points;
	}

	private static void findNeighbors(BufferedImage img,
	                                  int color,
	                                  HashArrayList<Point> points,
	                                  int from,
	                                  int to) {
		for (int i = from; i < to; i++) {
			Point point = points.get(i);
			int   x     = point.x;
			int   y     = point.y;

			Point p = new Point(x, y - 1);
			if (!points.contains(p) && getRGBSafe(img, x, y - 1) == color)
				points.add(p);

			p = new Point(x, y + 1);
			if (!points.contains(p) && getRGBSafe(img, x, y + 1) == color)
				points.add(p);

			p = new Point(x - 1, y);
			if (!points.contains(p) && getRGBSafe(img, x - 1, y) == color)
				points.add(p);

			p = new Point(x + 1, y);
			if (!points.contains(p) && getRGBSafe(img, x + 1, y) == color)
				points.add(p);
		}
	}

	private static int getRGBSafe(BufferedImage img, int x, int y) {
		if (x < 0 || y < 0 || x >= img.getWidth() ||
		    y >= img.getHeight())
			return TRANSPARENT;

		return img.getRGB(x, y);
	}

	private static void eraseShape(BufferedImage img, Iterable<Point> points) {
		for (Point p : points)
			img.setRGB(p.x, p.y, TRANSPARENT);
	}

	private static Point moveToOrigin(Iterable<Point> shape) {
		int minX = Integer.MAX_VALUE;
		int minY = Integer.MAX_VALUE;
		for (Point p : shape) {
			minX = Math.min(minX, p.x);
			minY = Math.min(minY, p.y);
		}

		for (Point p : shape) {
			p.x -= minX;
			p.y -= minY;
		}

		return new Point(minX, minY);
	}

	@SuppressWarnings("PackageVisibleField")
	private static class PixelShape {
		final int         width;
		final int         height;
		final List<Point> pixels;
		final byte[]      bitmap;
		final Point       coord;

		int count;

		PixelShape(Collection<Point> pixels, Point coord) {
			Dimension shapeSize      = findShapeSize(pixels);
			byte[][]  rotatedBitmaps = makeRotatedBitmaps(pixels, shapeSize.width, shapeSize.height);
			int       rotationIndex  = findCanonicalRotation(rotatedBitmaps, shapeSize);
			boolean   sameWidth      = (rotationIndex & 1) == 0;

			bitmap = rotatedBitmaps[rotationIndex];
			width = sameWidth ? shapeSize.width : shapeSize.height;
			height = sameWidth ? shapeSize.height : shapeSize.width;
			this.pixels = toPixels(bitmap, pixels.size(), width);
			this.coord = coord;

			count = 1;
		}

		private static Dimension findShapeSize(Iterable<Point> pixels) {
			int maxX = Integer.MIN_VALUE;
			int maxY = Integer.MIN_VALUE;
			for (Point p : pixels) {
				maxX = Math.max(maxX, p.x);
				maxY = Math.max(maxY, p.y);
			}

			return new Dimension(maxX + 1, maxY + 1);
		}

		private static byte[][] makeRotatedBitmaps(Iterable<Point> pixels, int width, int height) {
			byte[][] rotations = new byte[4][width * height];

			for (Point p : pixels) {
				rotations[0][width * p.y + p.x] = 1;
				rotations[1][height * p.x + (height - p.y - 1)] = 1;
				rotations[2][width * (height - p.y - 1) + (width - p.x - 1)] = 1;
				rotations[3][height * (width - p.x - 1) + p.y] = 1;
			}

			return rotations;
		}

		private static int findCanonicalRotation(byte[][] rotatedBitmaps, Dimension shapeSize) {
			boolean firstIsLandscape = shapeSize.width > shapeSize.height;

			byte[] bestRotationBitmap = rotatedBitmaps[0];
			int    bestRotationIndex  = 0;
			for (int i = 1; i < rotatedBitmaps.length; i++) {
				int diff = compareRotation(rotatedBitmaps[i], bestRotationBitmap);

				if (diff == 0)
					diff = compareAspectRatio(firstIsLandscape, bestRotationIndex, i);

				if (diff < 0) {
					bestRotationBitmap = rotatedBitmaps[i];
					bestRotationIndex = i;
				}
			}

			return bestRotationIndex;
		}

		private static int compareRotation(byte[] lhs, byte[] rhs) {
			for (int i = 0; i < lhs.length; i++)
				if (lhs[i] != rhs[i])
					return lhs[i] - rhs[i];

			return 0;
		}

		private static int compareAspectRatio(boolean firstIsLandscape, int lhsRotationIndex, int rhsRotationIndex) {
			boolean rotationMatters = (rhsRotationIndex - lhsRotationIndex & 1) != 0;
			if (!rotationMatters)
				return 0;

			boolean rhsIsLandscape = ((rhsRotationIndex & 1) == 0) == firstIsLandscape;
			return rhsIsLandscape ? -1 : 1;
		}

		private static List<Point> toPixels(byte[] bitmap, int numPixels, int stride) {
			List<Point> bestShape = new ArrayList<>(numPixels);

			for (int i = 0; i < bitmap.length; i++)
				if (bitmap[i] > 0)
					bestShape.add(new Point(i % stride, i / stride));

			return bestShape;
		}

		public void incrementCount() {
			count++;
		}

		public int getCount() {
			return count;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o)
				return true;

			if (!(o instanceof PixelShape))
				return false;

			PixelShape other = (PixelShape)o;
			return pixels.equals(other.pixels);
		}

		@Override
		public int hashCode() {
			return pixels.hashCode();
		}

		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder((width * 2 + 1) * height);

			for (int i = 0; i < bitmap.length; i++) {
				sb.append(bitmap[i] != 0 ? "██" : "░░");
				if ((i + 1) % width == 0)
					sb.append('\n');
			}

			return sb.toString();
		}
	}

	public ShapeCountMain() {
		super(null);
		setPreferredSize(new Dimension(1920, 1200));

		new Timer(50, ignored -> repaint()).start();
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		g.drawImage(img, 0, 0, getWidth(), getHeight(), 0, 0, img.getWidth(), img.getHeight(), null);
	}
}
