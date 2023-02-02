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
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import net.jcip.annotations.GuardedBy;
import org.jetbrains.annotations.Nullable;

import org.digitalmodular.owop.data.PixelChange;

/**
 * @author Mark Jeronimus
 */
// Created 2022-12-14
@SuppressWarnings("SynchronizationOnStaticField")
public class OWOPClearBotMain {
	private static final int CHUNK_CACHE_SIZE = 1024;

	public static final  String  CANVAS     = "beta";
	private static final boolean SIMULATION = false;

	private static @Nullable OWOPClient client = null;

	private static final     Set<Point> clearQueue      = new HashSet<>(65536);
	@GuardedBy("clearQueue")
	private static           boolean    clearToolArmed  = false;
	@GuardedBy("clearQueue")
	private static @Nullable Point      topLeft         = null;
	@GuardedBy("clearQueue")
	private static @Nullable Point      bottomRight     = null;
	@GuardedBy("clearQueue")
	private static           boolean    clearToolActive = false;

	public static void main(String... args) throws InterruptedException {
		doStuff();
	}

	private static void doStuff() throws InterruptedException {
		if (client == null) {
			if (SIMULATION) {
				client = new OWOPClientDummy();
			} else {
				client = new OWOPClientWS();
			}

//			client.setDoLogging(true);
			client.sendJoinWorld(CANVAS);

			while (!client.isReady()) {
				TimeUnit.MILLISECONDS.sleep(1);
			}
		}

		while (true) {
			client.canRequestChunk();

			checkPixelChanges();

			tickTools();

			TimeUnit.MILLISECONDS.sleep(2);
		}
	}

	private static void checkPixelChanges() {
		PixelChange change = client.getNextPixelChange();
		if (change == null) {
			return;
		}

		if (change.id() != 1) {
			useClearTool(change);
		}
	}

	private static void useClearTool(PixelChange change) {
		int x = change.x();
		int y = change.y();

		synchronized (clearQueue) {
			if (!clearQueue.isEmpty()) {
				return;
			}

			if (topLeft == null) {
				if (!clearToolArmed && change.color() == 0x000000) {
					clearToolArmed = true;
				} else if (clearToolArmed && change.color() == 0xFFFFFF) {
					setTopLeft(x, y);
					clearToolArmed = false;
				}
			} else if (bottomRight == null) {
				if (!clearToolArmed && change.color() == 0x000000) {
					clearToolArmed = true;
				} else if (clearToolArmed && change.color() == 0xFFFFFF) {
					setBottomRight(x, y);
					clearToolArmed = false;
				}
			} else {
				if (x >= topLeft.x && y >= topLeft.y && x <= bottomRight.x && y <= bottomRight.y) {
					clearToolActive = true;
				} else {
					client.clearChunkRequests();
				}
			}
		}
	}

	@GuardedBy("clearQueue")
	private static void setTopLeft(int x, int y) {
		topLeft = new Point(x, y);
		setPixel(x, y, 0xFF0000);
	}

	@GuardedBy("clearQueue")
	private static void setBottomRight(int x, int y) {
		int dx = x - topLeft.x;
		int dy = y - topLeft.y;
		if (dx < 0 || dy < 0 || dx * dy > CHUNK_CACHE_SIZE) {
			setPixel(x, y, 0xFFFFFF);
			return;
		}

		bottomRight = new Point(x, y);
		setPixel(x, topLeft.y, 0x00FF00);
		setPixel(topLeft.x, y, 0x00FF00);
		setPixel(x, y, 0x0000FF);
	}

	private static void tickTools() {
		Point point;

		synchronized (clearQueue) {
			if (!clearToolActive || client.getNumPendingChunkRequests() > 0 || !client.canSetPixel()) {
				return;
			}

			point = clearQueue.iterator().next();
		}

		System.out.println(point);
//		setPixel(point.x, point.y, 0xFFFFFF);
	}

	private static void setPixel(int x, int y, int color) {
		assert client != null;
		client.sendSetPixel(x, y, color);
	}

}
