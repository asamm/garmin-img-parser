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

import java.io.IOException;

public class UnknownSubFile extends ImgSubFile {
	
	public UnknownSubFile(String filename, String filetype, int fileSize,
			int blocSize, int initialOffset, ImgFileBag fileBag) {
		super(filename, filetype, fileSize, blocSize, initialOffset, fileBag);
	}

	@Override
	public void init() throws IOException {
		// FileContext context=new FileContext();
		// superInit(context);
	}

	@Override
	public void fullInit() throws IOException {

	}
}
