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

import org.free.garminimg.utils.ImgConstants;

import android.util.Log;

import java.io.EOFException;
import java.io.IOException;
import java.util.BitSet;

/**
 * A sub-file with .rgn extension. Contains the map points and lines.
 */
class RgnSubFile extends ImgSubFile {

	private static final String TAG = "-RgnSubFile->";

	private long dataOffset;

	private long dataLength;

	private long extendedPolygonsOffset, extendedPolylinesOffset,
			extendedPoisOffset;

	private long extendedPolygonsLength, extendedPolylinesLength,
			extendedPoisLength;

	private static final int SEG_POS_POINT = 0;

	private static final int SEG_POS_IPOINT = 1;

	private static final int SEG_POS_POLYLINE = 2;

	private static final int SEG_POS_POLYGON = 3;

	private static final int SEG_POS_EXTPOINT = 6;

	private static final int SEG_POS_EXTPOLYLINE = 5;

	private static final int SEG_POS_EXTPOLYGON = 4;

	private final double coefUnitsDist;

	public RgnSubFile(String filename, String filetype, int fileSize,
			int blocSize, int initialOffset, ImgFileBag fileBag) {
		super(filename, filetype, fileSize, blocSize, initialOffset, fileBag);
		coefUnitsDist = fileBag.getImgContext().coefUnistsDist;
	}

	@Override
	public void init() throws IOException {

	}

	@Override
	public void fullInit() throws IOException {
		FileContext context = new FileContext();
		superInit(context);
		seek(initialOffset + 0x15, context);
		dataOffset = readUInt32(context);
		dataLength = readUInt32(context);
		if (headerLength >= 0x55) {
			seek(initialOffset + 0x1d, context);
			extendedPolygonsOffset = readUInt32(context);
			extendedPolygonsLength = readUInt32(context);
			seek(initialOffset + 0x39, context);
			extendedPolylinesOffset = readUInt32(context);
			extendedPolylinesLength = readUInt32(context);
			seek(initialOffset + 0x55, context);
			extendedPoisOffset = readUInt32(context);
			extendedPoisLength = readUInt32(context);
		}

	}

	public boolean hasExtendedPolygons() {

		return extendedPolygonsLength > 0;
	}

	public boolean hasExtendedPolilines() {

		return extendedPolylinesLength > 0;
	}

	public boolean hasExtendedPois() {

		return extendedPoisLength > 0;
	}

	public long getDataEnd() {

		return dataLength;
	}

	public long getExtendedPolygonsEnd() {
		return extendedPolygonsLength;
	}

	public long getExtendedPolylinesEnd() {
		return extendedPolylinesLength;
	}

	public long getExtendedPoisEnd() {
		return extendedPoisLength;
	}

	public void parseSubDivision(SubDivision subDivision, LblSubFile lbl,
			NetSubFile net, MapListener listener, int targetLevel,
			int objectKindFilter, BitSet objectTypeFilter, RgnContext rgnContext)
			throws IOException {
		Segment[] segments = getSegments(subDivision, rgnContext.context);
		if (segments == null)
			return;

		if ((objectKindFilter & ObjectKind.POLYGON) != 0) {
			final Segment segment = segments[SEG_POS_POLYGON];
			if (segment != null) {
				parsePoly(subDivision, net, segment, listener, false,
						targetLevel, objectTypeFilter, rgnContext);
			}
		}
		if ((objectKindFilter & ObjectKind.EXTENDED_POLYGON) != 0) {
			final Segment segment = segments[SEG_POS_EXTPOLYGON];
			if (segment != null) {
				parseExtPoly(subDivision, net, segment, listener, false,
						targetLevel, objectTypeFilter, rgnContext);
			}
		}

		if ((objectKindFilter & ObjectKind.POLYLINE) != 0) {
			final Segment segment = segments[SEG_POS_POLYLINE];
			if (segment != null) {
				parsePoly(subDivision, net, segment, listener, true,
						targetLevel, objectTypeFilter, rgnContext);
			}
		}
		if ((objectKindFilter & ObjectKind.EXTENDED_POLYLINE) != 0) {
			final Segment segment = segments[SEG_POS_EXTPOLYLINE];
			if (segment != null) {
				parseExtPoly(subDivision, net, segment, listener, true,
						targetLevel, objectTypeFilter, rgnContext);
			}
		}

		if ((objectKindFilter & ObjectKind.POINT) != 0) {
			final Segment segment = segments[SEG_POS_POINT];
			if (segment != null) {
				parsePoints(subDivision, segment, listener, false, targetLevel,
						objectTypeFilter, rgnContext.context);
			}
		}
		if ((objectKindFilter & ObjectKind.INDEXED_POINT) != 0) {
			final Segment segment = segments[SEG_POS_IPOINT];
			if (segment != null) {
				parsePoints(subDivision, segment, listener, true, targetLevel,
						objectTypeFilter, rgnContext.context);
			}
		}
		if ((objectKindFilter & ObjectKind.EXTENDED_POINT) != 0) {
			final Segment segment = segments[SEG_POS_EXTPOINT];
			if (segment != null) {
				parseExtPoints(subDivision, segment, listener, false,
						targetLevel, objectTypeFilter, rgnContext.context);
			}
		}
	}

	public Label getIndexPointLabel(SubDivision subDivision, int index)
			throws IOException {
		FileContext context = new FileContext();
		Segment[] segments = getSegments(subDivision, context);
		if (segments == null)
			return null;
		Segment segment = segments[SEG_POS_IPOINT];
		if (segment != null) {
			int cpt = 0;
			seek(segment.segmentStart, context);
			while (getNextReadPos(context) < segment.segmentEnd) {
				int type = readByte(context);
				int info = readUInt24(context);
				boolean hasSubType = ((info & 0x800000) != 0); // The doc is
																// wrong
				// in that area...
				boolean isPOI = ((info & 0x400000) != 0);
				int lblOffset = info & 0x3FFFFF;
				int longitudeDelta = readInt16(context);
				int latitudeDelta = readInt16(context);
				int subType = 0;
				if (hasSubType) {
					subType = readByte(context);
				}
				if (++cpt == index) {
					Label label = null;
					if (lblOffset != 0) {
						int longitude = subDivision.getLongitude(
								longitudeDelta, 0);
						int latitude = subDivision.getLongitude(latitudeDelta,
								0);
						if (isPOI) {
							label = new IndexedPOILabel(fileBag, lblOffset,
									type, subType, longitude, latitude);
						} else {
							label = new IndexedSimpleLabel(fileBag, lblOffset,
									type, subType, longitude, latitude);
						}
					}
					return label;
				}
			}
		}
		return null;
	}

	/**
	 * Parse all the poly[gons|lines] of the RGN and send the results to the
	 * listener.
	 */
	private void parsePoly(SubDivision subDivision, NetSubFile net,
			Segment segment, MapListener listener, boolean line,
			int targetLevel, BitSet objectTypeFilter, RgnContext rgnContext)
			throws IOException {
		final FileContext context = rgnContext.context;
		seek(segment.segmentStart, context);
		final BitStreamReader reader = new BitStreamReader();

		while (getNextReadPos(context) < segment.segmentEnd) {
			int info = readByte(context);
			int type;
			final boolean direction;
			if (line) {
				type = info & 0x3F;
				direction = (info & 0x40) != 0;
			} else {
				type = info & 0x7F;
				direction = false;
			}
			boolean twoBytesLen = (info & 0x80) != 0;

			info = readUInt24(context);
			final int labelOffset = info & 0x3FFFFF;
			final boolean extraBit = (info & 0x400000) != 0;
			final boolean dataInNet = (info & 0x800000) != 0;
			final int longitudeDelta = readInt16(context);
			final int latitudeDelta = readInt16(context);
			final int bitStreamLen;
			if (twoBytesLen)
				bitStreamLen = readUInt16(context);
			else
				bitStreamLen = readByte(context);
			final int bitStreamInfo = readByte(context);

			reader.reset(bitStreamLen);
			int nbPoints = 0;

			int longSign = 0;
			if (reader.readNextBits(1, context) != 0) { // constant signed
				longSign = (reader.readNextBits(1, context) == 0 ? +1 : -1);
			}

			int latSign = 0;
			if (reader.readNextBits(1, context) != 0) { // constant signed
				latSign = (reader.readNextBits(1, context) == 0 ? +1 : -1);
			}

			int longExtraBit = 0;
			int latExtraBit = 0;
			if (extraBit) {
				// I don't know... for some reason, only the longitude gets
				// the extra bit... weird...
				longExtraBit = 1;
			}

			final int longBits = convertCoordinateLength(bitStreamInfo & 0xF,
					longSign, longExtraBit);
			final int latBits = convertCoordinateLength(bitStreamInfo >> 4,
					latSign, latExtraBit);

			int curLongPos = longitudeDelta;
			int curLatPos = latitudeDelta;

			// I guess, they would be stupid not to use the first point...
			rgnContext.longs[nbPoints] = subDivision
					.getLongitude(curLongPos, 0);
			rgnContext.lats[nbPoints] = subDivision.getLatitude(curLatPos, 0);
			nbPoints++;

			// increase the precision if needed
			curLongPos <<= longExtraBit;
			curLatPos <<= latExtraBit;

			while (reader.hasNext(longBits + latBits)) {
				int longOffset = reader.readCoordOffset(longBits, longSign,
						longExtraBit, context);
				int latOffset = reader.readCoordOffset(latBits, latSign,
						latExtraBit, context);
				curLongPos += longOffset;
				curLatPos += latOffset;

				rgnContext.checkCoordsSize(nbPoints);
				rgnContext.longs[nbPoints] = subDivision.getLongitude(
						curLongPos, longExtraBit);
				rgnContext.lats[nbPoints] = subDivision.getLatitude(curLatPos,
						latExtraBit);
				nbPoints++;
			}
			reader.finish(context);

			Label label = null;
			if (labelOffset != 0) {
				if (line && dataInNet) {
					if (net != null) {
						label = new LineInNetLabel(fileBag, labelOffset);
					} else {
						// I don't know what to do, here...
						label = new SimpleLabel(fileBag, labelOffset);
					}
				} else {
					label = new SimpleLabel(fileBag, labelOffset);
				}
				label = translateLabel(type, label, line);
			}
			if (objectTypeFilter == null || objectTypeFilter.get(type))
				listener.addPoly(type, rgnContext.longs, rgnContext.lats,
						nbPoints, label, line);

		}
		if (getNextReadPos(context) > segment.segmentEnd) {
			Log.e(TAG, "Bad poly* subDivision end: expected="
					+ segment.segmentEnd + " actual=" + getNextReadPos(context)
					+ " index=" + subDivision.getIndex());
		}
	}

	/**
	 * Parse all the points of the RGN and send the results to the listener.
	 */
	private void parseExtPoints(SubDivision subDivision, Segment segment,
			MapListener listener, boolean indexed, int targetLevel,
			BitSet objectTypeFilter, ImgSubFile.FileContext context)
			throws IOException {

		seek(segment.segmentStart, context);
		while (getNextReadPos(context) < segment.segmentEnd) {
			int tipo = readByte(context);
			int subtipo = readByte(context);
			int fullTipo = ((tipo + 0x100) << 8) + (subtipo % 32);

			boolean hasLbl = (subtipo & 0x20) > 0;

			final int longitudeDelta = readInt16(context);
			final int latitudeDelta = readInt16(context);

			Label label = null;
			if (hasLbl) {
				label = new SimpleLabel(fileBag, readUInt24(context));
			}
			listener.addPoint(fullTipo >> 8, fullTipo & 0xff,
					subDivision.getLongitude(longitudeDelta, 0),
					subDivision.getLatitude(latitudeDelta, 0), label, indexed);
		}
		if (getNextReadPos(context) > segment.segmentEnd) {
			Log.e(TAG, "Bad segment end: expected=" + segment.segmentEnd
					+ " actual=" + getNextReadPos(context));
		}
	}

	/**
	 * Parse all the poly[gons|lines] of the RGN and send the results to the
	 * listener.
	 */
	private void parseExtPoly(SubDivision subDivision, NetSubFile net,
			Segment segment, MapListener listener, boolean line,
			int targetLevel, BitSet objectTypeFilter, RgnContext rgnContext)
			throws IOException {
		final FileContext context = rgnContext.context;

		seek(segment.segmentStart, context);
		final BitStreamReader reader = new BitStreamReader();

		while (getNextReadPos(context) < segment.segmentEnd) {

			int tipo = readByte(context);
			int subtipo = readByte(context);

			int fullTipo = ((tipo + 0x100) << 8) + (subtipo % 32);

			boolean hasLbl = (subtipo & 0x20) > 0;

			final int longitudeDelta = readInt16(context);
			final int latitudeDelta = readInt16(context);

			int bitStreamLen = readByte(context);

			if ((bitStreamLen % 2) == 0) {
				bitStreamLen = (bitStreamLen + readByte(context) * 256) / 4 - 1;
			} else
				bitStreamLen = bitStreamLen / 2 - 1;

			final int bitStreamInfo = readByte(context);
			reader.reset(bitStreamLen);
			int nbPoints = 0;

			int longSign = 0;
			if (reader.readNextBits(1, context) != 0) { // constant signed
				longSign = (reader.readNextBits(1, context) == 0 ? +1 : -1);
			}

			int latSign = 0;
			if (reader.readNextBits(1, context) != 0) { // constant signed
				latSign = (reader.readNextBits(1, context) == 0 ? +1 : -1);
			}

			int longExtraBit = reader.readNextBits(1, context);

			final int longBits = convertCoordinateLength(bitStreamInfo & 0xF,
					longSign, longExtraBit);
			final int latBits = convertCoordinateLength(bitStreamInfo >> 4,
					latSign, 0);

			int curLongPos = longitudeDelta;
			int curLatPos = latitudeDelta;

			// I guess, they would be stupid not to use the first point...
			rgnContext.longs[nbPoints] = subDivision
					.getLongitude(curLongPos, 0);
			rgnContext.lats[nbPoints] = subDivision.getLatitude(curLatPos, 0);
			nbPoints++;

			// increase the precision if needed
			curLongPos <<= longExtraBit;
			try {

				while (reader.hasNext(longBits + latBits)) {
					int longOffset = reader.readCoordOffset(longBits, longSign,
							longExtraBit, context);
					int latOffset = reader.readCoordOffset(latBits, latSign, 0,
							context);
					curLongPos += longOffset;
					curLatPos += latOffset;
					rgnContext.checkCoordsSize(nbPoints);
					rgnContext.longs[nbPoints] = subDivision.getLongitude(
							curLongPos, longExtraBit);
					rgnContext.lats[nbPoints] = subDivision.getLatitude(
							curLatPos, 0);
					nbPoints++;
				}
				reader.finish(context);
			} catch (Exception e) {
				System.out.println("SubDivision " + segment.segmentEnd
						+ " actual=" + getNextReadPos(context) + " index="
						+ subDivision.getIndex());
				return;
			}
			Label label = null;
			if (hasLbl) {
				label = new SimpleLabel(fileBag, readUInt24(context));
			}
			listener.addPoly(fullTipo, rgnContext.longs, rgnContext.lats,
					nbPoints, label, line);
		}
		if (getNextReadPos(context) > segment.segmentEnd) {
			Log.e(TAG, "Bad poly* subDivision end: expected="
					+ segment.segmentEnd + " actual=" + getNextReadPos(context)
					+ " index=" + subDivision.getIndex());
		}
	}

	private Label translateLabel(int type, Label label, boolean line)
			throws IOException {
		if (label != null && line) {
			if (type >= ImgConstants.MINOR_LAND_CONTOUR
					&& type <= ImgConstants.MAJOR_DEPTH_CONTOUR) {
				return translateLabel(label, 10);
			}
		}
		return label;
	}

	private Label translateLabel(Label label, int round) {

		double feet = 0.;
		try {
			feet = Double.parseDouble(label.getName());
			label = new SimpleLabel(Integer.toString((int) Math.round(feet
					* coefUnitsDist / round)
					* round));
		} catch (Exception e) {
			feet = 0.;
		}
		return label;
	}

	private int convertCoordinateLength(int i, int sign, int extraBit) {
		int additionalLength = 0;
		if (sign == 0)
			additionalLength++;
		additionalLength += extraBit;
		if (i <= 9)
			return i + 2 + additionalLength;
		else
			return 2 * i - 9 + 2 + additionalLength;
	}

	/**
	 * Parse all the points of the RGN and send the results to the listener.
	 */
	private void parsePoints(SubDivision subDivision, Segment segment,
			MapListener listener, boolean indexed, int targetLevel,
			BitSet objectTypeFilter, ImgSubFile.FileContext context)
			throws IOException {
		seek(segment.segmentStart, context);
		while (getNextReadPos(context) < segment.segmentEnd) {
			int type = readByte(context);
			int info = readUInt24(context);
			boolean hasSubType = ((info & 0x800000) != 0); // The doc is wrong
			// in that area...
			boolean isPOI = ((info & 0x400000) != 0);
			int lblOffset = info & 0x3FFFFF;
			int longitudeDelta = readInt16(context);
			int latitudeDelta = readInt16(context);
			int subType = 0;
			if (hasSubType) {
				subType = readByte(context);
			}

			Label label = null;
			if (lblOffset != 0) {
				if (isPOI) {
					label = new POILabel(fileBag, lblOffset);
				} else {
					label = new SimpleLabel(fileBag, lblOffset);
				}

			}
			int fulltipo = (type << 8) + subType;
			if (fulltipo == 0x6616 || fulltipo == 0x6300) {
				label = translateLabel(label, 1);
			}
			listener.addPoint(type, subType,
					subDivision.getLongitude(longitudeDelta, 0),
					subDivision.getLatitude(latitudeDelta, 0), label, indexed);
		}
		if (getNextReadPos(context) > segment.segmentEnd) {
			Log.e(TAG, "Bad segment end: expected=" + segment.segmentEnd
					+ " actual=" + getNextReadPos(context));
		}
	}

	private Segment[] getSegments(SubDivision subDivision,
			ImgSubFile.FileContext context) throws IOException {

		long offset = subDivision.getDataOffset() + dataOffset;
		long end = subDivision.getDataEnd() + dataOffset;
		int objectTypes = subDivision.getObjectTypes();
		if (objectTypes == 0)
			return null;
		Segment[] result = new Segment[7];

		// poi/line/polygon ordinarios
		if (subDivision.hasData()) {
			int nbTypes = subDivision.getNbObjectTypes();
			if (nbTypes > 0) {
				if ((objectTypes & ObjectKind.POINT) != 0) {
					result[SEG_POS_POINT] = new Segment(ObjectKind.POINT);
				}
				if ((objectTypes & ObjectKind.INDEXED_POINT) != 0) {
					result[SEG_POS_IPOINT] = new Segment(
							ObjectKind.INDEXED_POINT);
				}
				if ((objectTypes & ObjectKind.POLYLINE) != 0) {
					result[SEG_POS_POLYLINE] = new Segment(ObjectKind.POLYLINE);
				}
				if ((objectTypes & ObjectKind.POLYGON) != 0) {
					result[SEG_POS_POLYGON] = new Segment(ObjectKind.POLYGON);
				}
				seek(offset, context);
				int nbPointers = nbTypes - 1;
				long segmentStart = offset + nbPointers * 2;
				int curType = 0;
				for (int cpt = 0; cpt < nbPointers; ++cpt) {
					while (result[curType] == null && curType < 4)
						curType++;
					long segmentEnd = readUInt16(context) + offset;
					if (segmentEnd > end) {
						System.out
								.println("WARNING: invalid segment end (too big)");
						result[curType] = null;
						// return null;
					}
					if (segmentEnd <= segmentStart) {
						System.out
								.println("WARNING: invalid segment end (too small)");
						result[curType] = null;
						// return null;
					}
					if (curType < result.length && result[curType] != null)
						result[curType].setPosition(segmentStart, segmentEnd);
					segmentStart = segmentEnd;
					curType++;
				}
				while (result[curType] == null && curType < 4)
					curType++;
				if (curType < result.length && result[curType] != null)
					result[curType].setPosition(segmentStart, end);
			}
		}

		if ((objectTypes & ObjectKind.EXTENDED_POLYGON) != 0) {
			long ini = subDivision.getDataExtPolygonOffset();
			long fin = subDivision.getDataExtPolygonEnd();
			if (fin > ini) {
				result[SEG_POS_EXTPOLYGON] = new Segment(
						ObjectKind.EXTENDED_POLYGON);
				result[SEG_POS_EXTPOLYGON].setPosition(extendedPolygonsOffset
						+ ini, extendedPolygonsOffset + fin);
			}
		}
		if ((objectTypes & ObjectKind.EXTENDED_POLYLINE) != 0) {
			long ini = subDivision.getDataExtPolylineOffset();
			long fin = subDivision.getDataExtPolylineEnd();
			if (fin > ini) {
				result[SEG_POS_EXTPOLYLINE] = new Segment(
						ObjectKind.EXTENDED_POLYLINE);
				result[SEG_POS_EXTPOLYLINE].setPosition(extendedPolylinesOffset
						+ ini, extendedPolylinesOffset + fin);
			}
		}
		if ((objectTypes & ObjectKind.EXTENDED_POINT) != 0) {
			long ini = subDivision.getDataExtPoiOffset();
			long fin = subDivision.getDataExtPoiEnd();
			if (fin > ini) {
				result[SEG_POS_EXTPOINT] = new Segment(
						ObjectKind.EXTENDED_POINT);
				result[SEG_POS_EXTPOINT].setPosition(extendedPoisOffset + ini,
						extendedPoisOffset + fin);
			}
		}
		return result;
	}

	private static class Segment {
		private long segmentStart;

		private long segmentEnd;

		private final int type;

		public Segment(int type) {
			this.type = type;
		}

		public void setPosition(long segmentStart, long segmentEnd)
				throws IOException {
			if (segmentStart > segmentEnd)
				throw new IOException(
						"A segment's end cannot be before it's start!");
			this.segmentStart = segmentStart;
			this.segmentEnd = segmentEnd;
		}
	}

	/**
	 * Reads a series of bits from the RGN file regardless of the Bytes limits.
	 */
	private class BitStreamReader {
		int length;

		int remainingBits;

		int curByte;

		public final void reset(int length) {
			this.length = length;
			remainingBits = 0;
		}

		public final void finish(FileContext context) throws IOException {
			while (length > 0) {
				readByte(context);
				--length;
			}

		}

		public final boolean hasNext(int nbBits) {
			return length * 8 + remainingBits >= nbBits;
		}

		public final int readCoordOffset(int nbBits, int sign, int extraBit,
				FileContext context) throws IOException {
			// For when the extrabit is set, I did a lot of experimenting to get
			// to this solution.
			// I don't know if it's 100% correct, but it seems so.

			if (sign == 0) {
				// variable sign value
				int value = readNextBits(nbBits, context);
				int signMask = 1 << (nbBits - 1);
				if ((value & signMask) != 0) {
					// negative
					int comp = value ^ signMask;
					if (extraBit == 0) {
						if (comp != 0)
							return comp - signMask;
						else {
							// need to get an extra nbBits to know the value
							int other = readCoordOffset(nbBits, sign, extraBit,
									context);
							if (other < 0)
								return 1 - value + other; // negatif
							else
								return value - 1 + other; // positif
						}
					} else {
						if ((comp & 0xFFFFFE) != 0) // the LSB doesn't count
													// when extraBit is set
						{
							// simple negatif
							return (comp & 0xFFFFFE) - signMask/* +1-(comp&0x1) */;
						} else {
							int other = readCoordOffset(nbBits - 1, sign, 0,
									context);
							if (other < 0)
								return 1 - signMask + 1/*-comp*/
										+ (other << 1); // negatif
							else
								return signMask - 1/* +comp */- 1
										+ (other << 1); // positif
						}
					}
				} else {
					if (extraBit > 0)
						return (value & 0xFFFFFE)/*-1+(value&0x1)*/;
					else
						return value;
				}
			} else {
				// constant sign value
				int val = readNextBits(nbBits, context);
				if (extraBit > 0)
					return (((val >>> 1) * sign) << 1)/* +(val&0x1) */;
				else
					return val * sign;
			}
		}

		public final int readNextBits(int toGet, FileContext context)
				throws IOException {
			int curPos = 0;
			int result = 0;
			do {
				getSomethingIfNeeded(context);
				final int remainingToGet = toGet - curPos;
				if (remainingToGet >= remainingBits) {
					result |= curByte << curPos;
					curPos += remainingBits;
					remainingBits = 0;
				} else {
					int mask = (1 << remainingToGet) - 1;
					result |= (curByte & mask) << curPos;
					curByte >>>= remainingToGet;
					remainingBits -= remainingToGet;
					return result;
				}
			} while (curPos < toGet);
			return result;
		}

		private void getSomethingIfNeeded(FileContext context)
				throws IOException {
			if (remainingBits == 0) {
				if (length == 0)
					throw new EOFException();
				remainingBits = 8;
				length--;
				curByte = readByte(context);
			}
		}
	}
}
