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
 * A basic label.
 */
public class SimpleLabel extends Label {
	
	public SimpleLabel(ImgFileBag file, int labelOffset) {
		super(file, labelOffset);
	}

	public SimpleLabel(String name) {
		super(null, -1);
		this.name = name;
		this.initDone = true;
	}

	protected void init() throws IOException {
		name = file.getLblFile().getLabel(labelOffset);
	}

}
