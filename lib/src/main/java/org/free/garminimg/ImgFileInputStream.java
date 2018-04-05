/*
 * JGarminImgParser - A java library to parse .IMG Garmin map files.
 *
 * Copyright (C) 2006 Patrick Valsecchi
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */
package org.free.garminimg;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * Handle the "decryption" of a .img file.
 */
class ImgFileInputStream {
	private RandomAccessFile file;

	private int xor;

	public ImgFileInputStream(File input, boolean xored) throws IOException {
		file = new RandomAccessFile(input, "r");

		// get the xor byte
		if (xored) {
			xor = file.read();
			seek(0);
		}
	}

	public void seek(long pos) throws IOException {
		file.seek(pos);
	}

	public int readByte() throws IOException {
		return (file.read() ^ xor) & 0xFF;
	}

	public String readString(int len) throws IOException {
		byte[] result = new byte[len];
		readBloc(result);
		return new String(result);
	}

	public int readInt32() throws IOException {
		return readByte() | readByte() << 8 | readByte() << 16
				| readByte() << 24;
	}

	public int readUInt16() throws IOException {
		return readByte() | readByte() << 8;
	}

	public void readBloc(byte[] content) throws IOException {
		file.read(content);
		if (xor == 0)
			return;
		for (int cpt = 0; cpt < content.length; cpt++) {
			content[cpt] = (byte) (content[cpt] ^ xor);
		}
	}

	public void close() throws IOException {
		file.close();
	}

	public int getXor() {
		return xor;
	}
}
