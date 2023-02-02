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

import java.awt.geom.Point2D;
import java.util.Iterator;
import java.util.List;

/**
 * @author zom-b
 */
// Created 2023-01-22
public class Polygon {
	private final GSHHG         header;
	private final List<Point2D> points;

	public Polygon(GSHHG header, List<Point2D> points) {
		this.header = header;
		this.points = points;
	}

	public GSHHG getHeader() {
		return header;
	}

	public List<Point2D> getPoints() {
		return points;
	}
}
