/*
 * This file is part of PAO.
 *
 * Copyleft 2023 Mark Jeronimus. All Rights Reversed.
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

package org.digitalmodular.gis;

import org.digitalmodular.utilities.HexUtilities;

/**
 * @author zom-b
 */
// Created 2023-01-22
public class GSHHG {
	private final int    id;
	private final int    n;
	private final int    flag;
	private final double west;
	private final double east;
	private final double south;
	private final double north;
	private final int    area;
	private final int    areaFull;
	private final int    container;
	private final int    ancestor;

	public GSHHG(int id,
	             int n,
	             int flag,
	             double west,
	             double east,
	             double south,
	             double north,
	             int area,
	             int areaFull,
	             int container,
	             int ancestor) {
		this.id = id;
		this.n = n;
		this.flag = flag;
		this.west = west;
		this.east = east;
		this.south = south;
		this.north = north;
		this.area = area;
		this.areaFull = areaFull;
		this.container = container;
		this.ancestor = ancestor;
	}

	/** Unique polygon id number, starting at 0 */
	public int getId() {
		return id;
	}

	/** Number of points in this polygon */
	public int getN() {
		return n;
	}

	/**
	 * = level + version << 8 + greenwich << 16 + source << 24 + river << 25
	 * <p>
	 * flag contains 5 items, as follows:
	 * low byte:    level = flag & 255: Values: 1 land, 2 lake, 3 island_in_lake, 4 pond_in_island_in_lake
	 * 2nd byte:    version = (flag >> 8) & 255: Values: Should be 12 for GSHHG release 12 (i.e., version 2.2)
	 * 3rd byte:    greenwich = (flag >> 16) & 1: Values: Greenwich is 1 if Greenwich is crossed
	 * 4th byte:    source = (flag >> 24) & 1: Values: 0 = CIA WDBII, 1 = WVS
	 * 4th byte:    river = (flag >> 25) & 1: Values: 0 = not set, 1 = river-lake and level = 2
	 */
	public int getFlag() {
		return flag;
	}

	/**
	 * level = flag & 255: Values:
	 * <ul><li>1 land</li>
	 * <li>2 lake</li>
	 * <li>3 island_in_lake</li>
	 * <li>4 pond_in_island_in_lake</li>
	 * <li>5 Antarctic ice-front</li>
	 * <li>6 Antarctic grounding-line</li></ul>
	 */
	public int getLevel() {
		return flag & 255;
	}

	public boolean isRiver() {
		return ((flag >> 25) & 1) != 0;
	}

	/** min/max extent in micro-degrees */
	public double getWest() {
		return west;
	}

	/** min/max extent in micro-degrees */
	public double getEast() {
		return east;
	}

	/** min/max extent in micro-degrees */
	public double getSouth() {
		return south;
	}

	/** min/max extent in micro-degrees */
	public double getNorth() {
		return north;
	}

	/** Area of polygon in 1/10 km^2 */
	public int getArea() {
		return area;
	}

	/** Area of original full-resolution polygon in 1/10 km^2 */
	public int getAreaFull() {
		return areaFull;
	}

	/** Id of container polygon that encloses this polygon (-1 if none) */
	public int getContainer() {
		return container;
	}

	/** Id of ancestor polygon in the full resolution set that was the source of this polygon (-1 if none) */
	public int getAncestor() {
		return ancestor;
	}

	@Override
	public String toString() {
		return "GSHHG{" +
		       "id=" + id +
		       ",\tn=" + n +
		       ",\tlevel=" + getLevel() +
		       ",\tisRiver=" + isRiver() +
//		       ",\twest=" + west +
//		       ",\teast=" + east +
//		       ",\tsouth=" + south +
//		       ",\tnorth=" + north +
		       ",\tarea=" + area +
		       ",\tareaFull=" + areaFull +
		       ",\tcontainer=" + container +
		       ",\tancestor=" + ancestor +
		       '}';
	}
}
