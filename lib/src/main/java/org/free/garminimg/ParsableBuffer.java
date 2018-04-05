/*
 * JGarminImgParser - A java library to parse .IMG Garmin map files.
 *
 * Copyright (C) 2007 Patrick Valsecchi
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

public class ParsableBuffer {
	
	private int position = 0;

	private byte[] buffer = null;

	public ParsableBuffer(byte[] buffer) {
		this.buffer = buffer;
	}

	public byte getByte() {
		return buffer[position++];
	}

	public short getShort() {
		int temp = (((int) buffer[position]) & 0xFF)
				| ((((int) buffer[position + 1]) & 0xFF) << 8);
		position += 2;
		return (short) temp;
	}

	public int getInt() {
		int temp = ((int) buffer[position]) & 0xFF
				| (((int) buffer[position + 1]) & 0xFF) << 8
				| (((int) buffer[position + 2]) & 0xFF) << 16
				| (((int) buffer[position + 3]) & 0xFF) << 24;
		position += 4;
		return temp;
	}

	public String getString() {
		StringBuffer sb = new StringBuffer();
		char character;
		do {
			character = (char) buffer[position++];
			if (character != 0) {
				sb.append(character);
			}
		} while (character != 0 && position < buffer.length);
		return sb.toString();
	}

	public boolean hasMoreData() {
		return position < buffer.length;
	}

	public String toString() {
		StringBuffer sb = new StringBuffer();
		for (int cpt = position; cpt < buffer.length; ++cpt) {
			String a = Integer.toHexString(buffer[cpt] & 0xFF);
			sb.append(ParsableBuffer.zeroPad(a, 2));
			sb.append(" ");
		}
		return sb.toString();
	}

	private static String zeroPad(String a, int number) {
		String temp = a;
		while (temp.length() < number) {
			temp = "0" + temp;
		}
		return temp;
	}

}
