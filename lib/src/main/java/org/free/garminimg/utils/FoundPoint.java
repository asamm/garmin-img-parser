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

public class FoundPoint extends FoundObject {
	
	private int subType;

	private int longitude;

	private int latitude;

	private boolean indexed;

	public FoundPoint(int type, int subType, int longitude, int latitude,
			Label label, boolean indexed) {
		super(type, label);
		this.indexed = indexed;
		this.latitude = latitude;
		this.longitude = longitude;
		this.subType = subType;
	}

	public boolean isIndexed() {
		return indexed;
	}

	public int getLatitude() {
		return latitude;
	}

	public int getLongitude() {
		return longitude;
	}

	public int getSubType() {
		return subType;
	}

}
