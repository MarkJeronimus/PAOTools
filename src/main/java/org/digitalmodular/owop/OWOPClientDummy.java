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

import java.awt.Point;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.util.Deque;
import java.util.LinkedList;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Consumer;

import net.jcip.annotations.GuardedBy;
import org.jetbrains.annotations.Nullable;

import org.digitalmodular.utilities.HexUtilities;
import org.digitalmodular.utilities.graphics.color.Color3fConst;

import org.digitalmodular.owop.data.Chunk;
import org.digitalmodular.owop.data.PixelChange;
import org.digitalmodular.owop.data.TileKey;
import static org.digitalmodular.owop.data.Chunk.CHUNK_SIZE;

/**
 * @author Mark Jeronimus
 */
// Created 2022-12-15
@SuppressWarnings("UseOfSystemOutOrSystemErr")
public class OWOPClientDummy implements OWOPClient {
	public static final int MAX_CONCURRENT_CHUNK_REQUESTS = 10;

	@SuppressWarnings("FieldHasSetterButNoGetter")
	private @Nullable Consumer<Chunk> chunkDownloadedCallback = null;

	@GuardedBy("chunkRequests")
	private final Deque<Point> chunkRequests = new LinkedList<>();

	@Override
	public void setDoLogging(boolean doLogging) {
	}

	@Override
	public void setChunkDownloadedCallback(@Nullable Consumer<Chunk> chunkDownloadedCallback) {
		this.chunkDownloadedCallback = chunkDownloadedCallback;
	}

	@Override
	public void setConnectionLostCallback(@Nullable Runnable connectionLostCallback) {
	}

	@Override
	public void sendJoinWorld(String worldName) {
		System.out.println("sendJoinWorld(" + worldName + ')');
	}

	@Override
	public boolean isReady() {
		return true;
	}

	@Override
	public void sendUpdatePlayer(int x, int y, int color, byte tool) {
		System.out.println("sendUpdatePlayer(" + x + ", " + y + ", 0x" + HexUtilities.toColorString(color) + ", " +
		                   tool + ')');
	}

	@Override
	public boolean canSetPixel() {
		return true;
	}

	@Override
	public void sendSetPixel(int x, int y, int color) {
		System.out.println("sendSetPixel(" + x + ", " + y + ", 0x" + HexUtilities.toColorString(color) + ')');
	}

	@Override
	public void sendChunkRequest(int chunkX, int chunkY) {
		System.out.println("sendChunkRequest(" + chunkX + ", " + chunkY + ')');

		chunkX &= -CHUNK_SIZE;
		chunkY &= -CHUNK_SIZE;
		Point point = new Point(chunkX, chunkY);

		synchronized (chunkRequests) {
			if (chunkRequests.contains(point)) {
				return;
			}

			chunkRequests.add(point);
		}

		ForkJoinPool.commonPool().execute(this::mockIncomingChunk);
	}

	@Override
	public boolean isChunkRequested(int chunkX, int chunkY) {
		Point point = new Point(chunkX, chunkY);

		synchronized (chunkRequests) {
			return chunkRequests.contains(point);
		}
	}

	@Override
	public boolean canRequestChunk() {
		synchronized (chunkRequests) {
			return chunkRequests.size() < MAX_CONCURRENT_CHUNK_REQUESTS;
		}
	}

	@Override
	public void retryStaleChunkRequests() {
	}

	@Override
	public int getNumPendingChunkRequests() {
		return 0;
	}

	@Override
	public void clearChunkRequests() {
	}

	@Override
	public @Nullable PixelChange getNextPixelChange() {
		return null;
	}

	@Override
	public void close() {
	}

	private void mockIncomingChunk() {
		synchronized (chunkRequests) {
			@Nullable Point point = chunkRequests.pollFirst();
			if (point != null && chunkDownloadedCallback != null) {
				Chunk chunk = createMockChunk(point.x, point.y);
				chunkDownloadedCallback.accept(chunk);
			}
		}
	}

	private static Chunk createMockChunk(int chunkX, int chunkY) {
		BufferedImage image  = new BufferedImage(CHUNK_SIZE, CHUNK_SIZE, BufferedImage.TYPE_INT_ARGB);
		int[]         colors = ((DataBufferInt)image.getRaster().getDataBuffer()).getData();

		double randomness = ThreadLocalRandom.current().nextDouble();
		int range = ThreadLocalRandom.current().nextInt(12) * // 16..247, biased towards 16.
		            ThreadLocalRandom.current().nextInt(22) + 16;

		for (int i = 0; i < colors.length; i++) {
			if (ThreadLocalRandom.current().nextDouble() < randomness) {
				colors[i] = 0xFFFFFFFF;
			} else {
				int index = ThreadLocalRandom.current().nextInt(range);
				colors[i] = 0xFF000000 | Color3fConst.DOS_PALETTE[index];
			}
		}

		TileKey key = new TileKey(chunkX, chunkY, 0, CHUNK_SIZE);

		Chunk chunk = new Chunk(key, image, false, System.currentTimeMillis());
		return chunk;
	}
}
