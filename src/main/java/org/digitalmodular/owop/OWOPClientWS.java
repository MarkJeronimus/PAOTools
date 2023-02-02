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

import java.awt.Color;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.io.ByteArrayOutputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import net.jcip.annotations.GuardedBy;
import net.jcip.annotations.ThreadSafe;
import org.jetbrains.annotations.Nullable;

import org.digitalmodular.utilities.AnsiConsole;
import org.digitalmodular.utilities.HexUtilities;
import org.digitalmodular.utilities.annotation.ThreadBounded;

import org.digitalmodular.owop.data.Chunk;
import org.digitalmodular.owop.data.PixelChange;
import org.digitalmodular.owop.data.TileKey;
import static org.digitalmodular.owop.OWOPClient.breakpoint;
import static org.digitalmodular.owop.data.Chunk.CHUNK_SIZE;

/**
 * @author Mark Jeronimus
 */
// Created 2022-12-13
@SuppressWarnings(
		{"UseOfSystemOutOrSystemErr", "FieldHasSetterButNoGetter", "ProhibitedExceptionThrown", "SynchronizeOnThis"})
@ThreadSafe
public class OWOPClientWS implements OWOPClient {
	public static final int WORLD_VERIFICATION_MAGIC      = 25565;
	public static final int MAX_CONCURRENT_CHUNK_REQUESTS = 1024;
	public static final int CHUNK_REQUEST_TIMEOUT_MILLIS  = 2000;

	private final AtomicBoolean                              doLogging               = new AtomicBoolean(false);
	private final AtomicReference<@Nullable Consumer<Chunk>> chunkDownloadedCallback = new AtomicReference<>();
	private final AtomicReference<@Nullable Runnable>        connectionLostCallback  = new AtomicReference<>();

	private final WebSocket             ws;
	private final StringBuilder         textBuffer   = new StringBuilder(256);
	private final ByteArrayOutputStream binaryBuffer = new ByteArrayOutputStream(1024);

	@GuardedBy("this")
	private int  id                    = -1;
	@GuardedBy("this")
	private int  delay                 = -1;
	@GuardedBy("this")
	private int  rank                  = -1;
	@GuardedBy("this")
	private int  x                     = 0;
	@GuardedBy("this")
	private int  y                     = 0;
	@GuardedBy("this")
	private long lastSetPixelTimestamp = System.currentTimeMillis();

	@GuardedBy("chunkRequests")
	private final Map<Point, Long>        chunkRequests = new HashMap<>(MAX_CONCURRENT_CHUNK_REQUESTS);
	@GuardedBy("pixelChanges")
	private final LinkedList<PixelChange> pixelChanges  = new LinkedList<>();

	public OWOPClientWS() {
		@SuppressWarnings("SpellCheckingInspection")
		CompletableFuture<WebSocket> webSocketCompletableFuture = HttpClient
				.newHttpClient()
				.newWebSocketBuilder()
				.buildAsync(URI.create("wss://ourworldofpixels.com/"), new OWOPSocketListener());

		ws = webSocketCompletableFuture.join();
	}

	@Override
	public void setDoLogging(boolean doLogging) {
		this.doLogging.set(doLogging);
	}

	@Override
	public void setChunkDownloadedCallback(@Nullable Consumer<Chunk> chunkDownloadedCallback) {
		this.chunkDownloadedCallback.set(chunkDownloadedCallback);
	}

	@Override
	public void setConnectionLostCallback(@Nullable Runnable connectionLostCallback) {
		this.connectionLostCallback.set(connectionLostCallback);
	}

	private class OWOPSocketListener implements WebSocket.Listener {
		@Override
		public void onOpen(WebSocket webSocket) {
			System.out.println("Open");
			webSocket.request(1);
		}

		@Override
		public @Nullable CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
			textBuffer.append(data);

			if (last && !textBuffer.isEmpty()) {
				String text = textBuffer.toString();
				textBuffer.setLength(0);

				ForkJoinPool.commonPool().execute(() -> handleSocketText(text));
			}

			webSocket.request(1);
			return null;
		}

		@Override
		public @Nullable CompletionStage<?> onBinary(WebSocket webSocket, ByteBuffer data, boolean last) {
			try {
				byte[] array  = data.array();
				int    offset = data.arrayOffset();
				int    len    = data.remaining();
				binaryBuffer.write(array, offset, len);

				if (last && binaryBuffer.size() > 0) {
					ByteBuffer buf = ByteBuffer.wrap(binaryBuffer.toByteArray());
					binaryBuffer.reset();

					ForkJoinPool.commonPool().execute(() -> handleSocketBinary(buf));
				}
			} catch (Throwable th) {
				th.printStackTrace();
				breakpoint();
			}

			webSocket.request(1);
			return null;
		}

		@Override
		public @Nullable CompletionStage<?> onPing(WebSocket webSocket, ByteBuffer message) {
			System.out.println("Ping");

			ForkJoinPool.commonPool().execute(OWOPClientWS.this::handleSocketPing);

			webSocket.request(1);
			return null;
		}

		@Override
		public @Nullable CompletionStage<?> onPong(WebSocket webSocket, ByteBuffer message) {
			System.out.println("Pong");
			webSocket.request(1);
			return null;
		}

		@Override
		public @Nullable CompletionStage<?> onClose(WebSocket webSocket, int statusCode, String reason) {
			System.out.println("Close");

			@Nullable Runnable connectionLostRunnable = connectionLostCallback.get();
			if (connectionLostRunnable != null) {
				connectionLostRunnable.run();
			}

			breakpoint();
			System.exit(0);
			return null;
		}

		@Override
		public void onError(WebSocket webSocket, Throwable error) {
			System.out.println("Error: " + error.getMessage());

			@Nullable Runnable connectionLostRunnable = connectionLostCallback.get();
			if (connectionLostRunnable != null) {
				connectionLostRunnable.run();
			}

			breakpoint();
			System.exit(0);
		}
	}

	@ThreadBounded // socket
	private static void handleSocketText(String text) {
		int i = text.indexOf(": ");
		if (i != 0) {
			if (text.startsWith("[D] ")) {
				AnsiConsole.setColor(new Color(0x3AB2FF));
			} else {
				AnsiConsole.setColor(new Color(0x6CFFE7));
			}

			AnsiConsole.setBold(true);
			System.out.print(System.currentTimeMillis() + " Chat: " + text.substring(0, i + 2));

			AnsiConsole.setColor(Color.WHITE);
			System.out.println(text.substring(i + 2));

			AnsiConsole.resetAll();
		} else {
			System.out.println(System.currentTimeMillis() + " Text: " + text);
		}
	}

	@SuppressWarnings("ProhibitedExceptionThrown")
	@ThreadBounded // socket
	private void handleSocketBinary(ByteBuffer buf) {
		buf.order(ByteOrder.LITTLE_ENDIAN);

		dumpByteBuffer("< ", buf);

		try {
			byte opCode = buf.get();
			switch (opCode) {
				case 0:
					handleSetID(buf);
					break;
				case 1:
					handleWorldUpdate(buf);
					break;
				case 2:
					handleChunkLoad(buf);
					break;
				case 3:
					handleTeleport(buf);
					break;
				case 4:
					handleSetRank(buf);
					break;
				case 5:
					handleCapcha(buf);
					break;
				case 6:
					handleSetPQuota(buf);
					break;
				default:
					throw new RuntimeException("Unknown opCode: " + opCode);
			}
		} catch (RuntimeException ex) {
			buf.position(0);
			dumpByteBuffer("<? ", buf);
			//noinspection CallToPrintStackTrace
			ex.printStackTrace();
			breakpoint();
		}
	}

	@ThreadBounded // socket
	private void handleSocketPing() {
		int x;
		int y;
		synchronized (this) {
			x = this.x + 1;
			y = this.y;
		}

		sendUpdatePlayer(x + 1, y, 0x000000, (byte)0);
	}

	@SuppressWarnings("MethodMayBeStatic")
	@ThreadBounded // socket
	private void handleCapcha(ByteBuffer buf) {
		byte captchaState = buf.get();
		if (captchaState != 3) {
			throw new RuntimeException("Unknown captchaState: " + captchaState);
		}
	}

	@ThreadBounded // socket
	private void handleSetID(ByteBuffer buf) {
		if (buf.remaining() != 4) {
			throw new RuntimeException("Unknown 'setId' message");
		}

		synchronized (this) {
			if (id != -1) {
				throw new RuntimeException("'id' is already set");
			}

			id = buf.getInt();
			System.out.println("id = " + id);
		}
	}

	@ThreadBounded // socket
	private void handleSetPQuota(ByteBuffer buf) {
		int rate = buf.getShort();
		int per  = buf.getShort();

		synchronized (this) {
			delay = per * CHUNK_REQUEST_TIMEOUT_MILLIS / rate;
			System.out.println("rate = " + rate + " pixels per " + per + " seconds (delay = " + delay + ')');
		}
	}

	@ThreadBounded // socket
	private void handleSetRank(ByteBuffer buf) {
		byte rank = buf.get();

		synchronized (this) {
			this.rank = rank;
			System.out.println("rank = " + rank);
		}

		sendRankVerification(rank);
	}

	@ThreadBounded // socket
	private void handleWorldUpdate(ByteBuffer buf) {
		updatePlayerList(buf);
		updatePixels(buf);
	}

	@ThreadBounded // socket
	private synchronized void handleTeleport(ByteBuffer buf) {
		x = buf.getInt();
		y = buf.getInt();
		System.out.println("Teleport: (" + x + ", " + y + ')');
	}

	@SuppressWarnings({"unused", "CommentedOutCode"})
	@ThreadBounded // socket
	private void updatePlayerList(ByteBuffer buf) {
		int count = buf.get() & 0xFF;
		for (int i = 0; i < count; i++) {
			int id   = buf.getInt(); // player id
			int x    = buf.getInt(); // x
			int y    = buf.getInt(); // y
			int r    = buf.get() & 0xFF; // r
			int g    = buf.get() & 0xFF; // g
			int b    = buf.get() & 0xFF; // b
			int tool = buf.get() & 0xFF; // tool
//
//			System.out.println("Player " + id + ": (" + x + ", " + y + ") 0x" +
//			                   HexUtilities.toUnsignedWordString((byte)r) +
//			                   HexUtilities.toUnsignedWordString((byte)g) +
//			                   HexUtilities.toUnsignedWordString((byte)b) + " tool = " + tool);

			synchronized (this) {
				if (id == this.id) {
					this.x = x >> 4; // Subpixel to pixel
					this.y = y >> 4;
				}
			}
		}
	}

	@SuppressWarnings("CommentedOutCode")
	private void updatePixels(ByteBuffer buf) {
		int count = buf.getShort() & 0xFFFF;
		for (int i = 0; i < count; i++) {
			int id = buf.getInt(); // player which set pixel
			int x  = buf.getInt(); // x
			int y  = buf.getInt(); // y
			int r  = buf.get() & 0xFF; // r
			int g  = buf.get() & 0xFF; // g
			int b  = buf.get() & 0xFF; // b

//			System.out.println("Pixel (" + x + ", " + y + ") 0x" +
//			                   HexUtilities.toUnsignedWordString(r) +
//			                   HexUtilities.toUnsignedWordString(g) +
//			                   HexUtilities.toUnsignedWordString(b) + " by = " + id);

			int color = (r << 16) | (g << 8) | b;

			PixelChange pixelChange = new PixelChange(x, y, color, id, System.currentTimeMillis());
			synchronized (pixelChanges) {
				pixelChanges.remove(pixelChange);
				pixelChanges.add(pixelChange);
			}
		}
	}

	private void handleChunkLoad(ByteBuffer buf) {
		int chunkX = buf.getInt() * CHUNK_SIZE;
		int chunkY = buf.getInt() * CHUNK_SIZE;

		synchronized (chunkRequests) {
			Point point = new Point(chunkX, chunkY);
			chunkRequests.remove(point);
//		    System.out.println("removed (" + point.x + ", " + point.y + "). size=" + requestedChunks.size());
		}

		@Nullable Consumer<Chunk> chunkUpdatedRunnable = chunkDownloadedCallback.get();
		if (chunkUpdatedRunnable == null) {
			return;
		}

		Chunk chunk = decompressChunk(buf, chunkX, chunkY);

		chunkUpdatedRunnable.accept(chunk);
	}

	private static Chunk decompressChunk(ByteBuffer buf, int chunkX, int chunkY) {
		boolean locked = buf.get() != 0;
		buf.getShort(); // originalLength
		int numRepeats = buf.getShort() & 0xFFFF;

		int[]         repeatCodePositions = new int[numRepeats];
		BufferedImage image               = new BufferedImage(CHUNK_SIZE, CHUNK_SIZE, BufferedImage.TYPE_INT_ARGB);
		int[]         pixels              = ((DataBufferInt)image.getRaster().getDataBuffer()).getData();
		IntBuffer     pixelBuffer         = IntBuffer.wrap(pixels);

		int offset = numRepeats * 2 + buf.position();

		for (int i = 0; i < numRepeats; i++) {
			repeatCodePositions[i] = (buf.getShort() & 0xFFFF) + offset;
		}

		for (int i = 0; i < numRepeats; i++) {
			while (buf.position() < repeatCodePositions[i]) {
				int color = 0xFF000000 |
				            ((buf.get() & 0xFF) << 16) |
				            ((buf.get() & 0xFF) << 8) |
				            ((buf.get() & 0xFF));
				pixelBuffer.put(color);
			}

			int repeatedNum = buf.getShort() & 0xFFFF;
			int color = 0xFF000000 |
			            ((buf.get() & 0xFF) << 16) |
			            ((buf.get() & 0xFF) << 8) |
			            ((buf.get() & 0xFF));

			do {
				pixelBuffer.put(color);
				repeatedNum--;
			} while (repeatedNum > 0);
		}

		while (pixelBuffer.hasRemaining()) {
			int color = 0xFF000000 |
			            ((buf.get() & 0xFF) << 16) |
			            ((buf.get() & 0xFF) << 8) |
			            ((buf.get() & 0xFF));
			pixelBuffer.put(color);
		}

		TileKey key = new TileKey(chunkX, chunkY, 0, CHUNK_SIZE);
		return new Chunk(key, image, locked);
	}

	private void sendRankVerification(int rank) {
		ByteBuffer reply = ByteBuffer.allocate(1);
		reply.order(ByteOrder.LITTLE_ENDIAN);
		reply.put((byte)rank);
		reply.flip();
		reply.order(ByteOrder.BIG_ENDIAN);
		dumpByteBuffer("> ", reply);
		ws.sendBinary(reply, true);
	}

	@Override
	public void sendJoinWorld(String worldName) {
		ByteBuffer reply = ByteBuffer.allocate(6);
		reply.order(ByteOrder.LITTLE_ENDIAN);
		reply.put(worldName.getBytes(StandardCharsets.ISO_8859_1));
		reply.putShort((short)WORLD_VERIFICATION_MAGIC);
		reply.flip();
		dumpByteBuffer("> ", reply);
		ws.sendBinary(reply, true);
	}

	@Override
	public synchronized boolean isReady() {
		return id > 0 && delay > 0 && rank > 0;
	}

	@Override
	public void sendUpdatePlayer(int x, int y, int color, byte tool) {
		ByteBuffer reply = ByteBuffer.allocate(12);
		reply.order(ByteOrder.LITTLE_ENDIAN);
		reply.putInt(x * 16 + ThreadLocalRandom.current().nextInt(16));
		reply.putInt(y * 16 + ThreadLocalRandom.current().nextInt(16));
		reply.put((byte)((color >> 16) & 0xFF));
		reply.put((byte)((color >> 8) & 0xFF));
		reply.put((byte)(color & 0xFF));
		reply.put(tool);
		reply.flip();
		reply.order(ByteOrder.BIG_ENDIAN);
		dumpByteBuffer("> ", reply);
		ws.sendBinary(reply, true);
	}

	@Override
	public boolean canSetPixel() {
		long now = System.currentTimeMillis();

		synchronized (this) {
			long elapsed = now - lastSetPixelTimestamp;
			return elapsed > delay;
		}
	}

	@Override
	public void sendSetPixel(int x, int y, int color) {
		boolean needPositionUpdate;
		synchronized (this) {
			needPositionUpdate = this.x != x || this.y != y;
		}

		if (needPositionUpdate) {
			sendUpdatePlayer(x, y, color, (byte)0);
		}

		ByteBuffer reply = ByteBuffer.allocate(11);
		reply.order(ByteOrder.LITTLE_ENDIAN);
		reply.putInt(x);
		reply.putInt(y);
		reply.put((byte)((color >>> 16) & 0xFF));
		reply.put((byte)((color >>> 8) & 0xFF));
		reply.put((byte)(color & 0xFF));
		reply.flip();
		reply.order(ByteOrder.BIG_ENDIAN);
		dumpByteBuffer("> ", reply);
		ws.sendBinary(reply, true);

		synchronized (this) {
			lastSetPixelTimestamp = System.currentTimeMillis();
		}
	}

	@Override
	public void sendChunkRequest(int chunkX, int chunkY) {
		chunkX &= -CHUNK_SIZE;
		chunkY &= -CHUNK_SIZE;
		Point point = new Point(chunkX, chunkY);

		synchronized (chunkRequests) {
			if (chunkRequests.containsKey(point)) {
				return;
			}

			chunkRequests.put(point, System.currentTimeMillis());
//		    System.out.println("Added (" + point.x + ", " + point.y + "). size=" + requestedChunks.size());
		}

		ByteBuffer reply = ByteBuffer.allocate(8);
		reply.order(ByteOrder.LITTLE_ENDIAN);
		reply.putInt(chunkX / CHUNK_SIZE);
		reply.putInt(chunkY / CHUNK_SIZE);
		reply.flip();
		reply.order(ByteOrder.BIG_ENDIAN);
		dumpByteBuffer("> ", reply);
		ws.sendBinary(reply, true);
	}

	@Override
	public boolean isChunkRequested(int chunkX, int chunkY) {
		return false;
	}

	@Override
	public boolean canRequestChunk() {
		retryStaleChunkRequests();

		synchronized (chunkRequests) {
			return chunkRequests.size() < MAX_CONCURRENT_CHUNK_REQUESTS;
		}
	}

	@Override
	public void retryStaleChunkRequests() {
		@Nullable Collection<Point> expiredRequests = null;

		synchronized (chunkRequests) {
			for (Iterator<Map.Entry<Point, Long>> iterator = chunkRequests.entrySet().iterator();
			     iterator.hasNext(); ) {
				Map.Entry<Point, Long> entry     = iterator.next();
				Long                   timestamp = entry.getValue();

				long elapsed = System.currentTimeMillis() - timestamp;
				if (elapsed > CHUNK_REQUEST_TIMEOUT_MILLIS) {
					iterator.remove();

					if (expiredRequests == null) {
						expiredRequests = new ArrayList<>(8);
					}

					expiredRequests.add(entry.getKey());
				}
			}
		}

		if (expiredRequests != null) {
			for (Point p : expiredRequests) {
				sendChunkRequest(p.x, p.y);
			}
		}
	}

	@Override
	public int getNumPendingChunkRequests() {
		synchronized (chunkRequests) {
			return chunkRequests.size();
		}
	}

	@Override
	public void clearChunkRequests() {
		synchronized (chunkRequests) {
			chunkRequests.clear();
		}
	}

	@Override
	public @Nullable PixelChange getNextPixelChange() {
		synchronized (pixelChanges) {
			if (pixelChanges.isEmpty()) {
				return null;
			}

			try {
				return pixelChanges.removeFirst();
			} catch (NoSuchElementException ignored) {
				return null;
			}
		}
	}

	@Override
	public void close() {
		ws.sendClose(WebSocket.NORMAL_CLOSURE, "");
	}

	private void dumpByteBuffer(String prefix, ByteBuffer data) {
		if (doLogging.get()) {
			int pos = data.position();

			byte[] ary = new byte[data.remaining()];
			data.get(ary, 0, ary.length);
			System.out.println(prefix + HexUtilities.byteArrayToHexString(ary));

			data.position(pos);
		}
	}
}
