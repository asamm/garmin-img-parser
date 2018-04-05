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
import java.util.*;

/**
 * A sub-file with .tre extension. Contains the information about the
 * sub-divisions and the boundary of the map.
 */
public class TreSubFile extends ImgSubFile {
	// private static final String TAG = "oruxmaps -TreSubFile->";

	private static final int SMALL_DIVISION_RECORD = 14;

	private static final int FULL_DIVISION_RECORD = 16;

	private int northBoundary;

	private int eastBoundary;

	private int southBoundary;

	private int westBoundary;

	private List<SubDivision> rootSubDivisions;

	private int lastBigIndex = 0;

	private int maxLevel;

	private int maxLevelWithData = 0;

	private int minLevel;

	private static int MAX_RESOLUTION = 24;

	private int bitsPerCoords[] = { MAX_RESOLUTION, MAX_RESOLUTION,
			MAX_RESOLUTION, MAX_RESOLUTION, MAX_RESOLUTION, MAX_RESOLUTION,
			MAX_RESOLUTION, MAX_RESOLUTION, MAX_RESOLUTION, MAX_RESOLUTION,
			MAX_RESOLUTION, MAX_RESOLUTION, MAX_RESOLUTION, MAX_RESOLUTION,
			MAX_RESOLUTION, MAX_RESOLUTION };

	private boolean inheriteds[] = new boolean[16];

	private boolean levelPresent[] = new boolean[16];

	private long fullSurface;

	private boolean extendedTypes;

	private long extendedTypesOffset, extendedTypesLength;

	private int extendedTypesSize, extendedTypesNumber;

	private int subdivisionss = 1;

	private int decalajeExtendedTypes;

	public TreSubFile(String filename, String filetype, int fileSize,
			int blocSize, int initialOffset, ImgFileBag fileBag) {
		super(filename, filetype, fileSize, blocSize, initialOffset, fileBag);
	}

	public boolean hasExtendedTypes() {

		return extendedTypes;
	}

	public long getExtendedTypesOffset() {

		return extendedTypesOffset;
	}

	public int getExtendedTypesSize() {

		return extendedTypesSize;
	}

	public int getExtendedTypesDecalaje() {

		return decalajeExtendedTypes;
	}

	@Override
	public void init() throws IOException {
		FileContext context = new FileContext();
		superInit(context);
		seek(initialOffset + 0x15, context);
		northBoundary = readInt24(context);
		eastBoundary = readInt24(context);
		southBoundary = readInt24(context);
		westBoundary = readInt24(context);
		fullSurface = ((long) northBoundary - southBoundary)
				* ((long) eastBoundary - westBoundary);
		if (headerLength >= 0x7C + 4 + 4 + 2) {
			parseTRE7(initialOffset + 0x7C, context);
		}
	}

	@Override
	public void fullInit() throws IOException {

	}

	private void parseTRE7(int infoOffset, FileContext context)
			throws IOException {
		seek(infoOffset, context);
		extendedTypesOffset = readUInt32(context);
		extendedTypesLength = readUInt32(context);
		extendedTypesSize = readUInt16(context);
		if (extendedTypesSize > 0)
			extendedTypesNumber = (int) (extendedTypesLength / extendedTypesSize);
		if (extendedTypesLength > 0)
			extendedTypes = true;
	}

	/**
	 * Check if the subDivisions needs to be read and read them if yes.
	 */
	private synchronized void initIfNeeded() throws IOException {
		if (rootSubDivisions == null) {
			FileContext context = new FileContext();
			parseLevels(context);
			parseSubDivisions(context);
		}
	}

	public long getLevelsPos(FileContext context) throws IOException {
		seek(initialOffset + 0x21, context);
		return readUInt32(context);
	}

	public long getLevelsLength(FileContext context) throws IOException {
		seek(initialOffset + 0x25, context);
		return readUInt32(context);
	}

	private void parseLevels(FileContext context) throws IOException {
		seek(initialOffset + 0x21, context);
		long levelsOffset = readUInt32(context);
		long levelsLength = readUInt32(context);

		seek(levelsOffset, context);
		maxLevel = 0;
		minLevel = 16;
		while (getNextReadPos(context) < levelsOffset + levelsLength) {
			int zoom = readByte(context);
			int bitsPerCoord = readByte(context);
			subdivisionss += readUInt16(context); // don't care
			int level = zoom & 0xF;
			boolean inherited = ((zoom & 0x80) != 0);
			if (maxLevel < level)
				maxLevel = level;
			if (minLevel > level)
				minLevel = level;
			levelPresent[level] = true;
			bitsPerCoords[level] = bitsPerCoord;
			inheriteds[level] = inherited;
		}
		decalajeExtendedTypes = subdivisionss - extendedTypesNumber;
	}

	private void parseSubDivisions(FileContext context) throws IOException {

		seek(initialOffset + 0x29, context);
		long subDivisionOffset = readUInt32(context);
		long subDivisionLength = readUInt32(context);
		rootSubDivisions = new ArrayList<SubDivision>();
		ArrayList<SubDivision> subDivisionsByIndex = new ArrayList<SubDivision>();
		parseSubDivision(subDivisionOffset, subDivisionLength,
				subDivisionsByIndex, rootSubDivisions, maxLevel, 1, context);// subDivisions
		parseLastSubDivision(subDivisionOffset, subDivisionLength,
				subDivisionsByIndex, rootSubDivisions, context);// subDivisions
		adjustSubDivisions(subDivisionsByIndex);
	}

	public int convertMapUnits(int level, int value, int additionalAccuracy) {
		int shift = 24 - getResolution(level) - additionalAccuracy;
		if (shift >= 0)
			return value << shift;
		else
			return value >> -shift;
	}

	private void adjustSubDivisions(ArrayList<SubDivision> subDivisionsByIndex)
			throws IOException {
		for (int cpt = 1; cpt < subDivisionsByIndex.size() - 1; ++cpt) {
			SubDivision curRegion = subDivisionsByIndex.get(cpt);
			SubDivision nextRegion = subDivisionsByIndex.get(cpt + 1);
			if (nextRegion == null) {
				throw new IOException("Empty region #" + (cpt + 1));
			}

			curRegion.setDataEnd(nextRegion.getDataOffset());
			if (extendedTypes) {
				curRegion.setDataExtendedPoiEnd(nextRegion
						.getDataExtPoiOffset());
				curRegion.setDataExtendedPolygonEnd(nextRegion
						.getDataExtPolygonOffset());
				curRegion.setDataExtendedPolylineEnd(nextRegion
						.getDataExtPolylineOffset());
			}
		}
		SubDivision curRegion = subDivisionsByIndex.get(subDivisionsByIndex
				.size() - 1);
		RgnSubFile rgn = getImgFileBag().getRgnFile();
		curRegion.setDataEnd(rgn.getDataEnd());
		if (extendedTypes) {
			curRegion.setDataExtendedPoiEnd(rgn.getExtendedPoisEnd());
			curRegion.setDataExtendedPolygonEnd(rgn.getExtendedPolygonsEnd());
			curRegion.setDataExtendedPolylineEnd(rgn.getExtendedPolylinesEnd());
		}
	}

	private void parseLastSubDivision(long subDivisionOffset,
			long subDivisionLength, ArrayList<SubDivision> subDivisionsByIndex,
			List<SubDivision> subDivisions, FileContext context)
			throws IOException {

		long pos;

		if (subDivisions.size() == 0)
			return;

		for (SubDivision s : subDivisions) {

			int nextIndex = s.getIndexNextLevel();

			if (nextIndex <= lastBigIndex) {
				parseLastSubDivision(subDivisionOffset, subDivisionLength,
						subDivisionsByIndex, s.getSubDivisions(), context);
			} else {
				int nextlevel = s.getLevel() - 1;

				while (nextlevel > 0 && !levelPresent[nextlevel])
					nextlevel--;

				if (nextlevel == minLevel) {

					pos = subDivisionOffset + (lastBigIndex)
							* FULL_DIVISION_RECORD
							+ (nextIndex - lastBigIndex - 1)
							* SMALL_DIVISION_RECORD;

					List<SubDivision> subs = s.getSubDivisions();

					seek(pos, context);
					while (getNextReadPos(context) < subDivisionOffset
							+ subDivisionLength) {
						SubDivision cur = new SubDivision(nextIndex, nextlevel,
								this);
						boolean last = cur.parse(this, context,
								SMALL_DIVISION_RECORD);
						registerRegionByIndex(subDivisionsByIndex, nextIndex,
								cur);
						subs.add(cur);
						if (cur.getDataOffset() != 0)
							maxLevelWithData = Math.max(maxLevelWithData,
									nextlevel);
						if (last) {
							break;
						}
						nextIndex++;
					}
				} else {// puede ser?????
						// TODO
				}
			}
		}
	}

	private void parseSubDivision(long subDivisionOffset,
			long subDivisionLength, ArrayList<SubDivision> subDivisionsByIndex,
			List<SubDivision> subs, int level, int index, FileContext context)
			throws IOException {

		if (index == 0 || level <= minLevel) {
			return;
		}

		long pos = subDivisionOffset + (index - 1) * FULL_DIVISION_RECORD;
		seek(pos, context);
		while (getNextReadPos(context) < subDivisionOffset + subDivisionLength) {
			SubDivision cur = new SubDivision(index, level, this);
			boolean last = cur.parse(this, context, FULL_DIVISION_RECORD);
			registerRegionByIndex(subDivisionsByIndex, index, cur);
			subs.add(cur);
			if (cur.getDataOffset() != 0)
				maxLevelWithData = Math.max(maxLevelWithData, level);
			if (last) {
				break;
			}
			index++;
		}

		if (lastBigIndex < index) {
			lastBigIndex = index;
		}

		int nextlevel = level - 1;

		while (nextlevel > 0 && !levelPresent[nextlevel])
			nextlevel--;

		if (nextlevel > 0) {
			for (SubDivision s : subs) {
				parseSubDivision(subDivisionOffset, subDivisionLength,
						subDivisionsByIndex, s.getSubDivisions(), nextlevel,
						s.getIndexNextLevel(), context);
			}
		}
	}

	public int getResolution(int level) {
		return bitsPerCoords[level];
	}

	public void halveResolution(int level) throws IOException {
		bitsPerCoords[level] -= 1;
		if (bitsPerCoords[level] <= 0) {
			throw new IOException("Cannot guess resolution for level " + level
					+ ": " + getResolutionsDesc());
		}
	}

	private String getResolutionsDesc() {
		StringBuffer buffer = new StringBuffer();
		for (int cpt = maxLevel; cpt >= minLevel; --cpt) {
			if (cpt < maxLevel)
				buffer.append(", ");
			buffer.append(cpt).append(':').append(bitsPerCoords[cpt]);
		}
		return buffer.toString();
	}

	public int getMaxLevel() throws IOException {
		initIfNeeded();
		return maxLevel;
	}

	public int getMinLevel() throws IOException {
		initIfNeeded();
		return minLevel;
	}

	public void registerRegionByIndex(
			ArrayList<SubDivision> subDivisionsByIndex, int index,
			SubDivision division) {
		while (subDivisionsByIndex.size() <= index)
			subDivisionsByIndex.add(null);
		subDivisionsByIndex.set(index, division);
	}

	public SubDivision getSubDivision(int i) throws IOException {

		initIfNeeded();
		if (rootSubDivisions != null) {
			for (SubDivision s : rootSubDivisions) {
				SubDivision res = getSubDivisionRecursive(s, i);
				if (res != null)
					return res;
			}
		}
		return null;
	}

	private SubDivision getSubDivisionRecursive(SubDivision sub, int index) {

		if (sub.getIndex() == index) {
			return sub;
		} else {
			for (SubDivision s : sub.getSubDivisions()) {
				SubDivision res = getSubDivisionRecursive(s, index);
				if (res != null)
					return res;
			}
			return null;
		}
	}

	public boolean matchesCoordinates(int minLong, int maxLong, int minLat,
			int maxLat) {
		return CoordUtils.matchesCoordinates(westBoundary, eastBoundary,
				southBoundary, northBoundary, minLong, maxLong, minLat, maxLat);
	}

	public boolean matchesCoordinate(int longitude, int latitude) {
		return CoordUtils.includedInCoordinates(longitude, latitude,
				westBoundary, eastBoundary, southBoundary, northBoundary);
	}

	public void readMap(int minLong, int maxLong, int minLat, int maxLat,
			int resolution, int objectKindFilter, BitSet objectTypeFilter,
			RgnSubFile rgn, LblSubFile lbl, NetSubFile net, MapListener listener)
			throws IOException {

		initIfNeeded();
		if (rootSubDivisions == null) {
			return;
		}
		int targetMinLevel;
		int targetMaxLevel;
		if (resolution >= 0) {
			targetMinLevel = guessLevel(resolution);
			targetMaxLevel = findMaxToDisplay(targetMinLevel);
		} else {
			targetMinLevel = minLevel;
			targetMaxLevel = maxLevel;
		}
		RgnContext rgnContext = new RgnContext();
		for (int level = targetMaxLevel; level >= targetMinLevel; --level) {
			for (SubDivision s : rootSubDivisions)// subDivisions1
			{
				s.readMap(minLong, maxLong, minLat, maxLat, level,
						objectKindFilter, objectTypeFilter, rgn, lbl, net,
						listener, rgnContext);
			}
		}
	}

	private int findMaxToDisplay(int targetMinLevel) {
		for (int cpt = targetMinLevel; cpt <= maxLevelWithData; cpt++) {
			if (!inheriteds[cpt])
				return cpt;
		}
		return maxLevel;
	}

	private int guessLevel(long resolution) {
		int targetBits = 24 - (int) Math.round(Math.log(resolution)
				/ Math.log(2));

		int minDist = Integer.MAX_VALUE;
		int result = 0;
		for (int cpt = minLevel; cpt <= maxLevelWithData; ++cpt) {
			int dist = Math.abs(bitsPerCoords[cpt] - targetBits);
			if (dist < minDist && levelPresent[cpt]) {
				minDist = dist;
				result = cpt;
			}
		}
		return result;
	}

	public long getFullSurface() {
		return fullSurface;
	}

	public int getEastBoundary() {
		return eastBoundary;
	}

	public int getNorthBoundary() {
		return northBoundary;
	}

	public int getSouthBoundary() {
		return southBoundary;
	}

	public int getWestBoundary() {
		return westBoundary;
	}

	public boolean[] getInheriteds() throws IOException {
		initIfNeeded();
		return inheriteds;
	}

}
