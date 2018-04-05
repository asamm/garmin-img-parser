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

class RgnContext {
	
	ImgSubFile.FileContext context = new ImgSubFile.FileContext();

	int longs[] = new int[50];

	int lats[] = new int[50];

	public final void checkCoordsSize(int nbPoints) {
		if (nbPoints >= longs.length) {
			longs = increaseSize(longs);
			lats = increaseSize(lats);
		}
	}

	private static int[] increaseSize(int old[]) {
		int result[] = new int[old.length * 2];
		System.arraycopy(old, 0, result, 0, old.length);
		return result;
	}
}
