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

import java.io.IOException;

/**
 * A label in a .net sub-file
 */
public class LineInNetLabel extends Label {
	
	public LineInNetLabel(ImgFileBag file, int labelOffset) {
		super(file, labelOffset);
	}

	protected void init() throws IOException {
		name = getNet().getRoadName(labelOffset, file.getLblFile());
	}

	public boolean equals(Object o) {
		return o == this || (o instanceof LineInNetLabel && super.equals(o));
	}

	public NetSubFile getNet() throws IOException {
		return file.getNetFile();
	}
}
