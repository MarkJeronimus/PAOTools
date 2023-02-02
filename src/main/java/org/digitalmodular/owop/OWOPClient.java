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

import java.util.function.Consumer;

import org.jetbrains.annotations.Nullable;

import org.digitalmodular.owop.data.Chunk;
import org.digitalmodular.owop.data.PixelChange;

/**
 * @author Mark Jeronimus
 */
// Created 2022-12-15
public interface OWOPClient {
	static void breakpoint() {
		//noinspection CallToThreadYield
		Thread.yield();
	}

	void setDoLogging(boolean doLogging);

	void setChunkDownloadedCallback(@Nullable Consumer<Chunk> chunkDownloadedCallback);

	void setConnectionLostCallback(@Nullable Runnable connectionLostCallback);

	/**
	 * Sends the command to join the specified world.
	 */
	void sendJoinWorld(String worldName);

	/**
	 * Checks if the player has logged in to the world and is ready to play.
	 */
	boolean isReady();

	/**
	 * Sends a command to change the player position, color, and tool.
	 */
	void sendUpdatePlayer(int x, int y, int color, byte tool);

	/**
	 * Returns true if the pixel cool-down is not active.
	 */
	boolean canSetPixel();

	/**
	 * Sends a command to draw a pixel.
	 * <p>
	 * This also calls {@link #sendUpdatePlayer(int, int, int, byte)} if necessary.
	 */
	void sendSetPixel(int x, int y, int color);

	/**
	 * Sends the command to retrieve the chunk containing the coordinate.
	 */
	void sendChunkRequest(int chunkX, int chunkY);

	boolean isChunkRequested(int chunkX, int chunkY);

	/**
	 * Returns true if the chunk cool-down is not active.
	 */
	boolean canRequestChunk();

	void retryStaleChunkRequests();

	int getNumPendingChunkRequests();

	void clearChunkRequests();

	/**
	 * Returns the next coordinate from the queue containing pixels which were changed by other players.
	 */
	@Nullable PixelChange getNextPixelChange();

	void close();
}
