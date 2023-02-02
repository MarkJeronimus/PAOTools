/*
 * This file is part of OWOPJavaBot.
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

package org.digitalmodular.owop;

import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.http.WebSocket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import javax.imageio.ImageIO;

import org.jetbrains.annotations.Nullable;

import org.digitalmodular.utilities.container.CacheMap;

import org.digitalmodular.owop.data.Chunk;
import org.digitalmodular.owop.data.PixelChange;
import org.digitalmodular.owop.data.RandomWalkPixelSorter;
import org.digitalmodular.owop.data.Region;
import org.digitalmodular.owop.data.TileKey;
import static org.digitalmodular.owop.data.Chunk.CHUNK_SIZE;

/**
 * @author Mark Jeronimus
 */
// Created 2022-12-13
@SuppressWarnings({"UseOfSystemOutOrSystemErr", "CallToPrintStackTrace"})
public class OWOPImagePasteMain implements WebSocket.Listener {
	public static final  String  FILENAME                = "/home/zom-b/Pictures/Maze/Andrew Bernhardt/mm5bw.png";
	private static final String  CANVAS                  = "beta";
	private static final int     OFFSET_X                = 2528;
	private static final int     OFFSET_Y                = 1440;
	private static final boolean SKIP_TRANSPARENT_PIXELS = true;

//	private static final String  FILENAME                =
// 	    "/home/zom-b/Pictures/Pixelart/PAO/Artworks/SFW/SFW-templatev3.png";
//	private static final String  CANVAS                  = "main";
//	private static final int     OFFSET_X                = 100;
//	private static final int     OFFSET_Y                = 1730;
//	private static final boolean SKIP_TRANSPARENT_PIXELS = true;

	private static final int     CACHE_SIZE             = 4096;
	private static final int     FILENAME_NUMBER_LENGTH = 8;
	private static final boolean SIMULATION             = true;

	private static BufferedImage image       = null;
	private static int           imageWidth  = 0;
	private static int           imageHeight = 0;

	private static RandomWalkPixelSorter points = null;

	private static       OWOPClient               client = null;
	private static final CacheMap<TileKey, Chunk> cache  = new CacheMap<>(CACHE_SIZE);

	private static int pixelCounter = 0;

	public static void main(String... args) throws IOException, InterruptedException {
		doStuff();
	}

	private static void doStuff() throws InterruptedException, IOException {
		image = ImageIO.read(new File(FILENAME));
		imageWidth = image.getWidth();
		imageHeight = image.getHeight();

		points = findPoints();

		if (client == null) {
			if (SIMULATION) {
				client = new OWOPClientDummy();
			} else {
				client = new OWOPClientWS();
			}

//		    client.setDoLogging(true);
			client.setChunkDownloadedCallback(OWOPImagePasteMain::chunkDownloadedCallback);
			client.sendJoinWorld(CANVAS);

			while (!client.isReady()) {
				TimeUnit.MILLISECONDS.sleep(1);
			}
		}

		placePoints();
	}

	private static Chunk createChunkCallback(TileKey key) {
		return new Chunk(key, false);
	}

	private static @Nullable Chunk loadChunkCallback(TileKey key) {
		try {
			BufferedImage image = ImageIO.read(makeFileName(key));
			return new Chunk(key, image, false);
		} catch (IOException ex) {
			ex.printStackTrace();
		}

		return null;
	}

	private static void saveChunkCallback(Region chunk) {
		try {
			ImageIO.write(chunk.getImage(), "PNG", makeFileName(chunk.makeKey()));
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	private static void chunkDownloadedCallback(Chunk chunk) {
		cache.put(chunk.makeKey(), chunk);
	}

	private static File makeFileName(TileKey key) {
		String xString = Integer.toString(Math.abs(key.getX()));
		String yString = Integer.toString(Math.abs(key.getY()));

		StringBuilder sb = new StringBuilder(CANVAS.length() + 32);

		sb.append(CANVAS);
		sb.append('_');

		sb.append('y');
		sb.append(key.getY() < 0 ? '-' : '+');
		sb.append("0".repeat(Math.max(FILENAME_NUMBER_LENGTH - yString.length(), 0)));
		sb.append(yString);

		sb.append('x');
		sb.append(key.getX() < 0 ? '-' : '+');
		sb.append("0".repeat(Math.max(FILENAME_NUMBER_LENGTH - xString.length(), 0)));
		sb.append(xString);

		return new File(sb.append(".png").toString());
	}

	private static RandomWalkPixelSorter findPoints() {
		long start = System.nanoTime();

		List<Point> pointList = new ArrayList<>(20000000);
		for (int y = 0; y < imageHeight; y++) {
			for (int x = 0; x < imageWidth; x++) {
				if (!SKIP_TRANSPARENT_PIXELS || (image.getRGB(x, y) & 0xFF000000) != 0) {
					pointList.add(new Point(x, y));
				}
			}
		}

		RandomWalkPixelSorter points = new RandomWalkPixelSorter(imageWidth, imageHeight, pointList.size(), 5000);

		for (Point point : pointList) {
			points.add(point);
		}

		System.out.println(pointList.size() + "\t" + (System.nanoTime() - start) / 1.0e9);
		return points;
	}

	private static void placePoints() throws InterruptedException {
		@Nullable Point point = null;

		//noinspection InfiniteLoopStatement
		while (true) {
			point = findPoint(point);
			if (point == null) {
				TimeUnit.MILLISECONDS.sleep(20);
				continue;
			}

			boolean needsNewRandomPoint = placePoint(point.x, point.y);
			if (needsNewRandomPoint) {
				point = null;
			}
		}
	}

	private static @Nullable Point findPoint(@Nullable Point point) {
		while (true) {
			@Nullable PixelChange nextPixelChange = client.getNextPixelChange();
			if (nextPixelChange == null) {
				break;
			}

			int x = nextPixelChange.x() - OFFSET_X;
			int y = nextPixelChange.y() - OFFSET_Y;
			if (x >= 0 && y >= 0 && x < imageWidth && y < imageHeight) {
				pixelCounter = 0;
				client.sendChunkRequest(nextPixelChange.x(), nextPixelChange.y());
				return new Point(x, y);
			}
		}

		do {
			if (points.isEmpty()) {
				return null;
			}

			if (point == null) {
				int x = ThreadLocalRandom.current().nextInt(imageWidth);
				int y = ThreadLocalRandom.current().nextInt(imageHeight);
				System.out.println("New random point (" + (x + OFFSET_X) + ", " + (y + OFFSET_Y));
				point = new Point(x, y);
				pixelCounter = 0;
			}

			point = points.removeClosest(point);
		} while (point == null && !points.isEmpty());

		return point;
	}

	private static boolean placePoint(int x, int y) throws InterruptedException {
		int imageColor = image.getRGB(x, y);
		if (SKIP_TRANSPARENT_PIXELS && (imageColor & 0xFF000000) == 0) {
			return false;
		}

		imageColor &= 0xFFFFFF;

		int     canvasX = x + OFFSET_X;
		int     canvasY = y + OFFSET_Y;
		TileKey key     = new TileKey(canvasX, canvasY, 0, CHUNK_SIZE);

		@Nullable Chunk chunk;
		while (true) {
			chunk = cache.get(key);
			if (chunk != null) {
				break;
			}

			TimeUnit.MILLISECONDS.sleep(2);
		}

		int chunkColor = chunk.getPixel(canvasX, canvasY);

		if (chunkColor != imageColor) {
			while (!client.canSetPixel()) {
				TimeUnit.MILLISECONDS.sleep(2);
			}

			System.out.println(points.size() + "\t (" + canvasX + ", " + canvasY + ')');
			client.sendSetPixel(canvasX, canvasY, imageColor);
		} else if (points.size() % 5000 == 0) {
			System.out.println(points.size());
		}

		pixelCounter++;
		if (pixelCounter >= 10000) {
			return true;
		}

		return false;
	}
}
