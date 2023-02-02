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

package org.digitalmodular.owop.data;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import org.jetbrains.annotations.Nullable;

/**
 * @author Mark Jeronimus
 */
// Created 2022-12-14
public class RandomWalkPixelSorter {
	public static final double OVERESTIMATE_CONSTANT = 1.5;

	private final int cellSize;

	private final List<List<List<Point>>> grid;
	private       int                     size = 0;

	public RandomWalkPixelSorter(int width, int height, int numPoints, int maxPointsPerCell) {
		int    area        = width * height;
		double density     = (double)numPoints / area;
		double maxCellArea = maxPointsPerCell / density;
		int    maxCellSize = (int)Math.sqrt(maxCellArea / OVERESTIMATE_CONSTANT);
		cellSize = Integer.highestOneBit(maxCellSize);

		int cellsX = (width + cellSize - 1) / cellSize;
		int cellsY = (height + cellSize - 1) / cellSize;

		grid = new ArrayList<>(cellsY);

		for (int y = 0; y < cellsY; y++) {
			List<List<Point>> row = new ArrayList<>(cellsX);

			for (int x = 0; x < cellsX; x++) {
				row.add(new ArrayList<>(maxPointsPerCell));
			}

			grid.add(row);
		}
	}

	public synchronized int size() {
		return size;
	}

	public synchronized boolean isEmpty() {
		return size == 0;
	}

	public synchronized void add(Point point) {
		int cellY = point.y / cellSize;
		if (cellY < 0 || cellY >= grid.size()) {
			throw new IndexOutOfBoundsException("'y' out of range [0," + grid.size() + "): " + point.y);
		}

		List<List<Point>> row = grid.get(cellY);

		int cellX = point.x / cellSize;
		if (cellX < 0 || cellX >= row.size()) {
			throw new IndexOutOfBoundsException("'x' out of range [0," + row.size() + "): " + point.x);
		}

		List<Point> cell = row.get(cellX);
		for (Point cellPoint : cell) {
			if (cellPoint.equals(point)) {
				return;
			}
		}

		cell.add(point);
		size++;
	}

	public synchronized @Nullable Point removeClosest(Point point) {
		if (size == 0) {
			return null;
		}

		int cellY = point.y / cellSize;
		if (cellY < 0 || cellY >= grid.size()) {
			throw new IndexOutOfBoundsException("'y' out of range [0," + grid.size() + "): " + point.y);
		}

		List<List<Point>> row = grid.get(cellY);

		int cellX = point.x / cellSize;
		if (cellX < 0 || cellX >= row.size()) {
			throw new IndexOutOfBoundsException("'x' out of range [0," + row.size() + "): " + point.x);
		}

		int fromX = Math.max(cellX - 1, 0);
		int toX   = Math.min(cellX + 1, row.size() - 1);
		int fromY = Math.max(cellY - 1, 0);
		int toY   = Math.min(cellY + 1, grid.size() - 1);

		@Nullable List<Point> foundCell  = null;
		@Nullable Point       foundPoint = null;
		int                   minDist    = Integer.MAX_VALUE;
		int                   numFound   = 0;
		for (int y = fromY; y <= toY; y++) {
			for (int x = fromX; x <= toX; x++) {
				List<Point> cell = grid.get(y).get(x);
				for (Point cellPoint : cell) {
					int dx   = cellPoint.x - point.x;
					int dy   = cellPoint.y - point.y;
					int dist = dx * dx + dy * dy;

					if (dist < minDist) {
						minDist = dist;
						foundCell = cell;
						foundPoint = cellPoint;
						numFound = 1;
					} else if (dist == minDist) {
						numFound++;
						if (ThreadLocalRandom.current().nextInt(numFound) == 0) {
							minDist = dist;
							foundCell = cell;
							foundPoint = cellPoint;
						}
					}
				}
			}
		}

		if (foundCell == null) {
			return null;
		}

		foundCell.remove(foundPoint);
		size--;
		return foundPoint;
	}
}
