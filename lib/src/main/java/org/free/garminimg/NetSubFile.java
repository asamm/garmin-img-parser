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
 * A sub-file with .net extension.
 */
class NetSubFile extends ImgSubFile {
	
	private long roadDefinitionOffset;

	private long roadDefinitionLength;

	private int roadDefinitionOffsetMultiplier;

	// private long segmentedRoadDefinitionOffset;
	//
	// private long segmentedRoadDefinitionLength;
	//
	// private int segmentedRoadDefinitionOffsetMultiplier;
	//
	// private long sortedRoadDefinitionOffset;
	//
	// private long sortedRoadDefinitionLength;
	//
	// private int sortedRoadDefinitionSize;

	public NetSubFile(String filename, String filetype, int fileSize,
			int blocSize, int initialOffset, ImgFileBag fileBag) {
		super(filename, filetype, fileSize, blocSize, initialOffset, fileBag);
	}

	@Override
	public void init() throws IOException {

	}

	@Override
	public void fullInit() throws IOException {
		FileContext context = new FileContext();
		superInit(context);
		seek(initialOffset + 0x15, context);

		roadDefinitionOffset = readUInt32(context);
		roadDefinitionLength = readUInt32(context);
		roadDefinitionOffsetMultiplier = 1 << readByte(context);

		// segmentedRoadDefinitionOffset=readUInt32(context);
		// segmentedRoadDefinitionLength=readUInt32(context);
		// segmentedRoadDefinitionOffsetMultiplier=1<<readByte(context);
		//
		// sortedRoadDefinitionOffset=readUInt32(context);
		// sortedRoadDefinitionLength=readUInt32(context);
		// sortedRoadDefinitionSize=readUInt16(context);
	}

	public String getRoadName(long offset, LblSubFile lbl) throws IOException {
		long multOffset = roadDefinitionOffsetMultiplier * offset;

		if (multOffset > roadDefinitionLength)
			return "!!!error!!!";

		FileContext context = new FileContext();
		seek(roadDefinitionOffset + multOffset, context);
		long actualOffset = readUInt24(context) & 0x3FFFFF;

		return lbl.getLabel(actualOffset);
	}

}
