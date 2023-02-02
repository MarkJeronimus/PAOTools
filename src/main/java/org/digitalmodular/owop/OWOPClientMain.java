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

package org.digitalmodular.owop;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;
import javax.imageio.IIOException;
import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import org.digitalmodular.utilities.annotation.ThreadBounded;
import org.digitalmodular.utilities.graphics.GraphicsUtilities;
import static org.digitalmodular.utilities.NumberUtilities.quantize;

import org.digitalmodular.owop.data.Chunk;
import org.digitalmodular.owop.data.OWOPRegionDataModel;
import org.digitalmodular.owop.data.Region;
import org.digitalmodular.owop.data.TileKey;
import static org.digitalmodular.owop.data.Chunk.CHUNK_SIZE;

/**
 * @author Mark Jeronimus
 */
// Created 2022-12-23
@SuppressWarnings({"CallToPrintStackTrace", "SynchronizationOnStaticField"})
public final class OWOPClientMain {
	private static final String  CANVAS                 = "main";
	public static final  int     REGION_SIZE            = 1024;
	private static final int     FILENAME_NUMBER_LENGTH = 8;
	private static final boolean SIMULATION             = true;

	private static final OWOPRegionDataModel dataModel = new OWOPRegionDataModel();

	private @Nullable OWOPClient client = null;

	private static final ExecutorService executor =
			Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

	public static void main(String... args) {
		Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {
			throwable.printStackTrace();
		});

		OWOPClientMain main = new OWOPClientMain();
		SwingUtilities.invokeLater(() -> {
			dataModel.addElementRemovedListener(OWOPClientMain::persistRegionFromEDT);

			GraphicsUtilities.setNiceLookAndFeel();

			JFrame f = new JFrame();
			f.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

			OWOPPanel gui = new OWOPPanel(main);
			f.setContentPane(gui);

			f.pack();
			f.setLocationRelativeTo(null);
			f.setVisible(true);
		});
	}

	public OWOPClientMain() {
		ForkJoinPool.commonPool().execute(this::requestChunksLoop);
	}

	public OWOPRegionDataModel getDataModel() {
		return dataModel;
	}

	@SuppressWarnings("ProhibitedExceptionThrown")
	public synchronized void connect() {
		if (client != null) {
			client.close();
		}

		OWOPClient client;
		if (SIMULATION) {
			client = new OWOPClientDummy();
		} else {
			client = new OWOPClientWS();
		}

		this.client = client;

		client.setChunkDownloadedCallback(chunk -> {
			try {
				chunkDownloadedCallback(chunk);
			} catch (InterruptedException ex) {
				throw new RuntimeException(ex);
			}
		});

//		client.setDoLogging(true);
		client.sendJoinWorld(CANVAS);

		try {
			//noinspection ObjectEquality
			while (this.client == client && !client.isReady()) {
				TimeUnit.MILLISECONDS.sleep(1);
			}
		} catch (InterruptedException ex) {
			throw new RuntimeException(ex);
		}
	}

	public void disconnect() {
		OWOPClient client = this.client;

		if (client != null) {
			client.close();
			this.client = null;
		}
	}

	private void requestChunksLoop() {
		try {
			//noinspection InfiniteLoopStatement
			while (true) {
				TimeUnit.MILLISECONDS.sleep(200);

				requestChunks();
			}
		} catch (InterruptedException ignored) {
		}
	}

	private synchronized void requestChunks() throws InterruptedException {
		while (true) {
			OWOPClient client = this.client;

			if (client == null || !client.canRequestChunk()) {
				break;
			}

			@Nullable TileKey key = findNextChunkToRequest();
			if (key == null) {
				continue;
			}

			client.sendChunkRequest(key.getX(), key.getY());
		}
	}

	public @Nullable TileKey findNextChunkToRequest() {
		int regionX = 0; // Temporary
		int regionY = 0;

		for (int y = 0; y < REGION_SIZE; y += CHUNK_SIZE) {
			for (int x = 0; x < REGION_SIZE; x += CHUNK_SIZE) {
				OWOPClient client = this.client;
				if (client == null) {
					return null;
				}

				int chunkX = regionX + x;
				int chunkY = regionY + y;
				if (!dataModel.chunkInCache(regionX, regionY, x, y) &&
				    !client.isChunkRequested(chunkX, chunkY)) {
					return new TileKey(chunkX, chunkY, 0, REGION_SIZE);
				}
			}
		}

		return null;
	}

	@SuppressWarnings("TypeMayBeWeakened")
	private static void chunkDownloadedCallback(Chunk chunk) throws InterruptedException {
		System.out.println("chunkDownloaded(" + chunk.getX() + ", " + chunk.getY() + ')');

		int     regionX = quantize(chunk.getX(), REGION_SIZE);
		int     regionY = quantize(chunk.getY(), REGION_SIZE);
		TileKey key     = new TileKey(regionX, regionY, 0, REGION_SIZE);

		Region region = getOrLoadOrCreateRegion(key);

		region.setSubRegion(chunk);
		dataModel.addRegion(key, region);
	}

	@NotNull
	private static Region getOrLoadOrCreateRegion(TileKey key) {
		@Nullable Region region;

		region = dataModel.getRegion(key);

		if (region == null) {
			region = loadOrCreateRegion(key);
		}

		return region;
	}

	private static Region loadOrCreateRegion(TileKey key) {
		@Nullable Region region = loadRegion(key);

		if (region != null) {
			return region;
		}

		return createRegion(key);
	}

	@ThreadBounded("AWT-EventQueue-0")
	private static @Nullable Region loadRegion(TileKey key) {
		Path path = makeFileName(key);

		try {
			BufferedImage image = ImageIO.read(path.toFile());
			return new Region(key, image);
		} catch (IIOException ex) {
			if (ex.getMessage().equals("Can't read input file!")) {
				return null;
			}

			ex.printStackTrace();
		} catch (IOException ex) {
			ex.printStackTrace();
		}

		return null;
	}

	@ThreadBounded("AWT-EventQueue-0")
	private static Region createRegion(TileKey key) {
		return new Region(key);
	}

	public static void saveAll() {
		synchronized (dataModel) {
			for (Region region : dataModel.regions()) {
				persistRegionFromEDT(null, region);
			}
		}
	}

	@ThreadBounded("AWT-EventQueue-0")
	private static void persistRegionFromEDT(@Nullable TileKey key, Region region) {
		executor.execute(() -> persistRegion(region));
	}

	private static void persistRegion(Region region) {
		Path path = makeFileName(region.makeKey());

		try {
			Files.createDirectories(path.getParent());
			ImageIO.write(region.getImage(), "PNG", path.toFile());
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	private static Path makeFileName(TileKey key) {
		String xString    = Integer.toString(Math.abs(key.getX()));
		String yString    = Integer.toString(Math.abs(key.getY()));
		String zoomString = Integer.toString(key.getZoom());

		StringBuilder sb = new StringBuilder(zoomString.length() + CANVAS.length() + 32);

		sb.append(zoomString);
		sb.append('/');

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

		return Paths.get(sb.append(".png").toString());
	}
}
