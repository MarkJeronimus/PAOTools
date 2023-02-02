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

package org.digitalmodular.owop.data;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;

import org.jetbrains.annotations.Nullable;

import static org.digitalmodular.utilities.ValidatorUtilities.requireNonNull;

/**
 * @author Mark Jeronimus
 */
// Created 2022-12-23
public class Region implements Tile {
	public static final Color DEFAULT_BACKGROUND = Color.WHITE;

	protected final int           x;
	protected final int           y;
	protected final int           zoom;
	protected final int           size;
	protected final BufferedImage image;
	protected final int[]         pixels;

	private @Nullable TileKey key = null;

	public Region(TileKey key) {
		this(key, create(key.getSize()));
	}

	public Region(TileKey key, @Nullable BufferedImage image) {
		x = key.getX();
		y = key.getY();
		zoom = key.getZoom();
		size = key.getSize();
		this.image = checkImage(requireNonNull(image, "image"));
		pixels = ((DataBufferInt)this.image.getRaster().getDataBuffer()).getData();
	}

	@Override
	public int getX() {
		return x;
	}

	@Override
	public int getY() {
		return y;
	}

	@Override
	public int getZoom() {
		return zoom;
	}

	@Override
	public int getSize() {
		return size;
	}

	@Override
	public TileKey makeKey() {
		if (key == null) {
			key = new TileKey(x, y, zoom, size);
		}

		return key;
	}

	public synchronized BufferedImage getImage() {
		return image;
	}

	/**
	 * The pixel coordinate is relative to this tile.
	 */
	public int getPixel(int x, int y) {
		return image.getRGB(x, y);
	}

	/**
	 * The pixel coordinate is relative to this tile.
	 */
	public synchronized void setPixel(int x, int y, int color) {
		if (!isSubRegionPresent(x, y)) {
			return;
		}

		int dx = x - this.x;
		int dy = y - this.y;
		if (dx < 0 || dy < 0 || dx >= size || dy >= size) {
			throw new IllegalArgumentException("Coordinate out of bounds for region @ (" + this.x + ", " + this.y +
			                                   "): (" + x + ", " + y + ')');
		}

		pixels[dy * size + dx] = 0xFF000000 | color;
	}

	public synchronized void setSubRegion(Region region) {
		int dx      = region.getX() - x;
		int dy      = region.getY() - y;
		int srcSize = region.getSize();

		int sizeDifference = size - srcSize;
		if (dx < 0 || dy < 0 || dx > sizeDifference || dy > sizeDifference) {
			throw new IllegalArgumentException("Chunk out of bounds for region: this = " + this +
			                                   ", region = " + region);
		}

		int srcP = 0;
		int dstP = dy * size + dx;

		for (int y = 0; y < srcSize; y++) {
			System.arraycopy(region.pixels, srcP, pixels, dstP, srcSize);
			srcP += srcSize;
			dstP += size;
		}
	}

	/**
	 * Checks if the region at the specified coordinate has opaque pixels.
	 * <p>
	 * The pixel coordinate is relative to this tile.
	 * <p>
	 * There's no 'size' parameter because it actually only checks the specified pixel,
	 * assuming it's the upper-left pixel of the intended region.
	 */
	public synchronized boolean isSubRegionPresent(int x, int y) {
		if (x < 0 || y < 0 || x >= size || y >= size) {
			throw new IllegalArgumentException("Coordinate out of bounds for region: (" + x + ", " + y + ')');
		}

		return (pixels[y * size + x] & 0xFF000000) != 0;
	}

	private BufferedImage checkImage(@Nullable BufferedImage img) {
		if (img == null || img.getWidth() != size || img.getHeight() != size) {
			return create(size); // Ignore incorrect size
		}

		if (img.getType() != BufferedImage.TYPE_INT_ARGB) {
			return convertImage(img); // This is almost surely the case on most VMs
		}

		return img;
	}

	private BufferedImage convertImage(BufferedImage img) {
		BufferedImage converted = create(size);

		Graphics2D g = converted.createGraphics();
		try {
			if (!DEFAULT_BACKGROUND.equals(Color.BLACK)) {
				g.setPaint(DEFAULT_BACKGROUND);
				g.fillRect(0, 0, size, size);
			}

			g.drawImage(img, 0, 0, null);
		} finally {
			g.dispose();
		}

		return converted;
	}

	private static BufferedImage create(int size) {
		return new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
	}

	@Override
	public boolean equals(@Nullable Object o) {
		if (this == o) {
			return true;
		}

		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		Region other = (Region)o;
		return getX() == other.getX() &&
		       getY() == other.getY() &&
		       getZoom() == other.getZoom() &&
		       getSize() == other.getSize() &&
		       getImage().equals(other.getImage());
	}

	@Override
	public int hashCode() {
		int hashCode = 0x811C9DC5;
		hashCode = 0x01000193 * (hashCode ^ Integer.hashCode(getX()));
		hashCode = 0x01000193 * (hashCode ^ Integer.hashCode(getY()));
		hashCode = 0x01000193 * (hashCode ^ Integer.hashCode(getZoom()));
		hashCode = 0x01000193 * (hashCode ^ Integer.hashCode(getSize()));
		hashCode = 0x01000193 * (hashCode ^ getImage().hashCode());
		return hashCode;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "[(" + x + ", " + y + "), zoom=" + zoom + ", size=" + size + ']';
	}

	public String toStringShort() {
		return "(" + x + ", " + y + "), zoom=" + zoom + ", size=" + size;
	}
}
