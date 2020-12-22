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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.StandardCharsets;

/**
 * A sub-file with .lbl extension.
 */
class LblSubFile extends ImgSubFile {

	private static final String NOCHAR = "???";

	private long dataOffset;

	private long dataLength;

	private long countryDefinitionOffset;

	private long countryDefinitionLength;

	private int countryDefinitionSize;

	private long regionDefinitionOffset;

	private long regionDefinitionLength;

	private int regionDefinitionSize;

	private long cityDefinitionOffset;

	private long cityDefinitionLength;

	private int cityDefinitionSize;

	private int labelCoding;

	private CharsetDecoder decoder;

	private final ByteArrayOutputStream out = new ByteArrayOutputStream();

	private long poiOffset;

	private long poiLength;

	private int poiMultiplier;

	private int poiGlobalMask;

	private int dataOffsetMultiplier;

	private long zipOffset;

	private long zipLength;

	private int zipSize;

	private static final int HAS_STREET_NUM = 0x01;

	private static final int HAS_STREET = 0x02;

	private static final int HAS_CITY = 0x04;

	private static final int HAS_ZIP = 0x08;

	private static final int HAS_PHONE = 0x10;

	// private static final int HAS_EXIT=0x20;
	//
	// private static final int HAS_TIDE_PREDICTION=0x40;
	//
	// private static final int HAS_UNKNOWN=0x80;

	public LblSubFile(String filename, String filetype, int fileSize,
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

		dataOffset = readUInt32(context);
		dataLength = readUInt32(context);
		dataOffsetMultiplier = 1 << readByte(context);
		labelCoding = readByte(context);

		countryDefinitionOffset = readUInt32(context);
		countryDefinitionLength = readUInt32(context);
		countryDefinitionSize = readUInt16(context);
		readUInt32(context);

		regionDefinitionOffset = readUInt32(context);
		regionDefinitionLength = readUInt32(context);
		regionDefinitionSize = readUInt16(context);
		readUInt32(context);

		cityDefinitionOffset = readUInt32(context);
		cityDefinitionLength = readUInt32(context);
		cityDefinitionSize = readUInt16(context);
		readUInt32(context);

		seek(initialOffset + 0x57, context);
		poiOffset = readUInt32(context);
		poiLength = readUInt32(context);
		poiMultiplier = 1 << readByte(context);
		poiGlobalMask = readByte(context);

		seek(initialOffset + 0x72, context);
		zipOffset = readUInt32(context);
		zipLength = readUInt32(context);
		zipSize = readUInt16(context);

		seek(initialOffset + 0xaa, context);
		int codepage = readUInt16(context);
		// int id1 = readUInt16(context);
		// int id2 = readUInt16(context);
		// long descOff = readUInt32(context);
		// long descLen = readUInt32(context);
		//
		// seek(initialOffset + descOff, context);

		// String description;
		// try {
		// description = new String(readBytes((int) descLen, context), "ascii");
		// } catch (Exception e) {
		// description = "Unknown";
		// }
		Charset charset = null;
		if (codepage == 0 || codepage == 850)
			charset = Charset.forName("cp1252");
		else if (codepage == 65001)
			charset = StandardCharsets.UTF_8;
		else if (codepage == 932)
			charset = Charset.forName("ms932");
		else if (codepage == 950)
			charset = Charset.forName("BIG5");
		else {
			String cp = "cp" + codepage;
			if (Charset.isSupported(cp))
				charset = Charset.forName(cp);
		}
		if (charset != null)
			decoder = charset.newDecoder();
	}

	public String getPOIName(long offset) throws IOException {
		if (poiMultiplier * offset > poiLength)
			throw new IOException("Invalid POI offset");
		FileContext context = new FileContext();
		seek(poiOffset + poiMultiplier * offset, context);
		int actualOffset = readUInt24(context) & 0x3FFFFF;
		return getLabelInternal(actualOffset, context);
	}

	public void getPOI(long offset, POILabel poi) throws IOException {
		if (poiMultiplier * offset > poiLength)
			throw new IOException("Invalid POI offset");
		FileContext context = new FileContext();
		seek(poiOffset + poiMultiplier * offset, context);
		int local = readUInt24(context);
		int actualOffset = local & 0x3FFFFF;
		poi.setName(getLabelInternal(actualOffset, context));

		int propertyMask = poiGlobalMask;
		if ((local & 0x800000) != 0) {
			propertyMask = maskPointAttributes(propertyMask, readByte(context));
		}
		if ((propertyMask & HAS_STREET_NUM) != 0) {
			poi.setStreetNumber(getNumber(context));
		}
		if ((propertyMask & HAS_STREET) != 0) {
			poi.setStreet(getStreet(context));
		}
		if ((propertyMask & HAS_CITY) != 0) {
			poi.setCity(getCity(
					((cityDefinitionLength / cityDefinitionSize > 255) ? readUInt16(context)
							: readByte(context)), poi, context));
		}
		if ((propertyMask & HAS_ZIP) != 0) {
			poi.setZip(getZip(zipLength / zipSize > 255 ? readUInt16(context)
					: readByte(context), context));
		}
		if ((propertyMask & HAS_PHONE) != 0) {
			poi.setPhone(getNumber(context));
		}
	}

	private String getStreet(FileContext context) throws IOException {
		int offset = readUInt24(context);
		if ((offset & 0x800000) == 0) {
			return getLabelInternal(offset & 0x3FFFFF, context);
		} else {
			// TODO: implement that...
			System.out
					.println("Don't know how to deal with this kind of street label");
			return null;
		}
	}

	private int maskPointAttributes(int globalMask, int localMask) {
		int globalBit = 1;
		int localBit = 1;
		int result = globalMask;
		for (int cpt = 0; cpt < 8; ++cpt) {
			if ((globalMask & globalBit) != 0) {
				if ((localMask & localBit) == 0)
					result ^= globalBit;
				localBit <<= 1;
			}
			globalBit <<= 1;
		}
		return result;
	}

	private String getZip(int index, FileContext context) throws IOException {
		int offset = (index - 1) * zipSize;
		if (offset >= zipLength)
			throw new IOException("ZIP index out of range: " + index);
		long save = getNextReadPos(context);
		seek(this.zipOffset + offset, context);
		int labelOffset = readUInt24(context);
		seek(save, context);
		return getLabelInternal(labelOffset & 0x3FFFFF, context);
	}

	private String getCity(int index, POILabel poi, FileContext context)
			throws IOException {
		int offset = (index - 1) * cityDefinitionSize;
		if (cityDefinitionSize != 5)
			throw new IOException(
					"Don't know how to parse city records of size!=5");
		if (offset >= cityDefinitionLength)
			throw new IOException("invalid city index");

		long save = getNextReadPos(context);
		seek(cityDefinitionOffset + offset, context);
		int data = readUInt24(context);
		int info = readUInt16(context);
		seek(save, context);
		if ((info & 0x8000) == 0) {
			return getLabelInternal(data & 0x3FFFFF, context);
		} else {
			SubDivision subDivision = poi.getTre().getSubDivision(data >> 8);
			Label label = poi.getRgn().getIndexPointLabel(subDivision,
					data & 0xFF);
			seek(save, context);
			if (label != null)
				return label.getName();
			else
				return null;
		}
	}

	public String getNumber(FileContext context) throws IOException {
		int curByte = readByte(context);

		if ((curByte & 0x80) == 0) {
			// pointer to label
			int offset = (curByte << 16) | readUInt16(context);
			return getLabelInternal(offset, context);
		} else {
			// 11 bit encoded
			StringBuilder result = new StringBuilder();
			decode2Base11(curByte & 0x7F, true, result);
			do {
				curByte = readByte(context);
				decode2Base11(curByte & 0x7F, (curByte & 0x80) != 0, result);
			} while ((curByte & 0x80) == 0);
			return result.toString();
		}
	}

	private void decode2Base11(int curByte, boolean last, StringBuilder result) {
		int a = curByte / 11;
		decode1Base11(a, last, result);

		int b = curByte % 11;
		decode1Base11(b, last, result);
	}

	private void decode1Base11(int a, boolean last, StringBuilder result) {
		if (a == 10) {
			if (!last)
				result.append("-");
		} else {
			result.append(a);
		}
	}

	public String getLabel(long offset) throws IOException {
		FileContext context = new FileContext();
		return getLabelInternal(offset, context);
	}

	private String getLabelInternal(long offset, FileContext context)
			throws IOException {
		if (offset < 0 || offset * dataOffsetMultiplier >= dataLength)
			throw new IOException("Invalid label offset: 0x"
					+ Long.toHexString(offset));
		long actualOffset = dataOffset + offset * dataOffsetMultiplier;

		return getAbsoluteLabel(actualOffset, context);
	}

	private String getAbsoluteLabel(long actualOffset, FileContext context)
			throws IOException {
		final String result;
		long save = getNextReadPos(context);
		switch (labelCoding) {
		case 6:
			result = getLabel6b(actualOffset, context);
			break;
		case 9:
			result = getLabel8b(actualOffset, context);
			break;
		case 10:
			result = getLabel10b(actualOffset, context);
			break;
		default:
			System.out.println("Don't know how to decode label with coding "
					+ labelCoding);
			return "???";
		}
		seek(save, context);
		return result;
	}

	private enum CharSet {
		NORMAL, SYMBOL, SPECIAL,
	}

    private static final char[] NORMAL_CHARS = {' ', 'A', 'B', 'C', 'D', 'E',
            'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R',
            'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', '~', '~', '~', '~', '~',
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '~', '~', '~',
            '~', '~', '~'};

    private static final char[] SYMBOL_CHARS = {'@', '!', '"', '#', '$', '%',
            '&', '\'', '(', ')', '*', '+', ',', '-', '.', '/', '~', '~', '~',
            '~', '~', '~', '~', '~', '~', '~', ':', ';', '<', '=', '>', '?',
            '~', '~', '~', '~', '~', '~', '~', '~', '~', '~', '~', '[', '\\',
            ']', '^', '_'};

    private static final char[] SPECIAL_CHARS = {'`', 'a', 'b', 'c', 'd', 'e',
            'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r',
            's', 't', 'u', 'v', 'w', 'x', 'y', 'z', '~', '~', '~', '~', '~',
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '~', '~', '~',
            '~', '~', '~'};

	private String getLabel6b(long offset, FileContext context)
			throws IOException {
		seek(offset, context);
		StringBuffer result = new StringBuffer();
		CharSet curCharSet = CharSet.NORMAL;
		while (true) {
			int b1 = readByte(context);
			int b2 = readByte(context);
			int b3 = readByte(context);

            int[] c = {b1 >> 2, (b1 & 0x3) << 4 | b2 >> 4,
                    (b2 & 0xF) << 2 | b3 >> 6, b3 & 0x3F};

			for (int cpt = 0; cpt < 4; ++cpt) {
				if (c[cpt] > 0x2F)
					return result.toString();
				switch (curCharSet) {
				case NORMAL:
					if (c[cpt] == 0x1c) {
						curCharSet = CharSet.SYMBOL;
					} else if (c[cpt] == 0x1b) {
						curCharSet = CharSet.SPECIAL;
					} else if (c[cpt] == 0x1d) {
						result.append('|');
					} else if (c[cpt] == 0x1f) {
						result.append(' ');
					} else if (c[cpt] == 0x1e) {
						result.append(' ');
					} else {
						result.append(NORMAL_CHARS[c[cpt]]);
					}
					break;
				case SYMBOL:
					result.append(SYMBOL_CHARS[c[cpt]]);
					curCharSet = CharSet.NORMAL;
					break;
				case SPECIAL:
					result.append(SPECIAL_CHARS[c[cpt]]);
					curCharSet = CharSet.NORMAL;
					break;
				}

			}
		}
	}

	private String getLabel8b(long offset, FileContext context)
			throws IOException {

		return getLabel10b(offset, context);
		//
		// seek(offset, context);
		// StringBuffer result = new StringBuffer();
		// while (true) {
		// char cur = (char) readByte(context);
		// if (cur == 0)
		// break;
		// result.append(cur);
		// }
		// return result.toString();
	}

	private String getLabel10b(long offset, FileContext context)
			throws IOException {
		String text = NOCHAR;
		if (decoder != null) {
			out.reset();
			seek(offset, context);
			while (true) {
				byte cur = (byte) readByte(context);
				if (cur == 0)
					break;
				out.write(cur);
			}
			byte[] ba = out.toByteArray();
			try {
				text = decoder.decode(ByteBuffer.wrap(ba)).toString();
			} catch (Exception e) {
			}
		}
		return text;
	}

}
