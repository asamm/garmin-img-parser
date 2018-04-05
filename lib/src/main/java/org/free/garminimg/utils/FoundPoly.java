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
package org.free.garminimg.utils;

import org.free.garminimg.Label;

public class FoundPoly extends FoundObject {
	
	private final int[] longitudes;

	private final int[] latitudes;

	private final boolean line;

	public FoundPoly(int type, int[] longitudes, int[] latitudes, int nbPoints,
			Label label, boolean line) {
		super(type, label);
		this.longitudes = new int[nbPoints];
		this.latitudes = new int[nbPoints];
		System.arraycopy(longitudes, 0, this.longitudes, 0, nbPoints);
		System.arraycopy(latitudes, 0, this.latitudes, 0, nbPoints);
		this.line = line;
	}

	public int getLongitude() {
		return longitudes[longitudes.length / 2];
	}

	public int getLatitude() {
		return latitudes[latitudes.length / 2];
	}

}
