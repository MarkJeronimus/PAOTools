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

import java.io.OutputStream;
import java.nio.ByteBuffer;

/**
 * @author Mark Jeronimus
 */
// Created 2022-12-20
class ByteBufferOutputStream extends OutputStream {
	private final ByteBuffer buf;

	public ByteBufferOutputStream(ByteBuffer buf) {
		this.buf = buf;
	}

	@Override
	public void write(int b) {
		buf.put((byte)b);
	}

	@Override
	public void write(byte[] bytes, int off, int len) {
		buf.put(bytes, off, len);
	}
}
