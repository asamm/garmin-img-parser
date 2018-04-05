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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Class for parsing .TDB files
 */
class ProductFile {
	
	private int productNumber;

	private String productName;

	private String productType;

	private Map<Integer, Area> areaTable = new HashMap<Integer, Area>();

	private Map<Integer, MapDesc> maps = new HashMap<Integer, MapDesc>();

	public ProductFile(File file) throws IOException {
		FileInputStream tdbStream = new FileInputStream(file);
		BufferedInputStream bis = new BufferedInputStream(tdbStream);

		int readByte;
		byte[] lengthArray = new byte[2];
		readByte = bis.read();
		while ((readByte != 0) && (readByte != -1)) {
			bis.read(lengthArray);
			int length = ((0xFF & lengthArray[0]) | ((0xFF & lengthArray[1]) << 8));
			byte[] data = new byte[length];
			bis.read(data);
			ParsableBuffer pb = new ParsableBuffer(data);
			switch (readByte) {
			case 0x50:
				parseProduct(pb);
				break;
			case 0x44:
				parseCopyright(pb);
				break;
			case 0x42:
				parseArea(pb);
				break;
			case 0x4c:
				parseMap(pb);
				break;
			case 0x52:
				parseDCA(pb);
				break;
			case 0x53:
				// I guess it's the TYP section
				break;
			default:
				System.out.println("unknown data in tdb: 0x"
						+ Integer.toHexString(readByte));
				break;
			}
			readByte = bis.read();
		}
		bis.close();

	}

	private void parseDCA(ParsableBuffer pb) {
		while (pb.hasMoreData()) {
			pb.getByte();
			pb.getString();
		}
	}

	private void parseMap(ParsableBuffer pb) {
		int imgNumber = pb.getInt();
		int areaCode = pb.getInt();
		int coordN = pb.getInt() >> 8;
		int coordE = pb.getInt() >> 8;
		int coordS = pb.getInt() >> 8;
		int coordW = pb.getInt() >> 8;
		String name = pb.getString();
		pb.getShort();
		int fileParts = pb.getShort();
		int[] fileSizes = new int[fileParts];
		for (int i = 0; i < fileParts; i++) {
			fileSizes[i] = pb.getInt();
		}

		Area area = areaTable.get(areaCode);
		MapDesc aMap = new MapDesc(imgNumber, name, area, coordN, coordW,
				coordS, coordE);
		maps.put(imgNumber, aMap);

		byte areaCode2 = pb.getByte(); // A little strange
		// Seems to be DCA when having such otherwise 1
		if (!pb.hasMoreData()) {
			return;
		}
		short dca = pb.getShort();
		int mdrLength = pb.getInt(); // propably MDR length for map
		if (!pb.hasMoreData()) {
			return;
		}

		// Internal filenames
		for (int i = 0; i < fileParts; i++) {
			String intFileName = pb.getString();
			// Internal filename of all files
		}
		if (!pb.hasMoreData()) {
			return;
		}
		int numberExtraDCAs = pb.getShort();
		if (numberExtraDCAs != 0) {
			for (int i = 0; i < numberExtraDCAs; i++) {
				short extraDCA = pb.getShort();
			}
		}
		if (pb.hasMoreData()) {
			System.out.println("Unknown data in map " + name + " "
					+ pb.toString());
		}
	}

	private void parseArea(ParsableBuffer pb) {
		while (pb.hasMoreData()) {
			int areaNumber = pb.getInt();
			int unknown = pb.getInt();
			if (unknown != 0) {
				System.out.println("Something is not 0 in tdb. (Area section)="
						+ unknown);
			}
			int coordn = pb.getInt() >> 8;
			int coorde = pb.getInt() >> 8;
			int coords = pb.getInt() >> 8;
			int coordw = pb.getInt() >> 8;
			String productArea = pb.getString();
			Area area = new Area(areaNumber, productArea, coordn, coordw,
					coords, coorde);
			areaTable.put(areaNumber, area);
		}
	}

	private void parseCopyright(ParsableBuffer pb) {
		while (pb.hasMoreData()) {
			int type = pb.getInt();
			String name = pb.getString();
		}
	}

	private void parseProduct(ParsableBuffer pb) {
		productNumber = pb.getInt();
		pb.getShort(); // Mapsource version number
		productName = pb.getString();
		if (!pb.hasMoreData()) {
			// We have R&R USA
			productType = "US Road & Rec";
			return;
		}
		pb.getShort(); // Product version number
		productType = pb.getString();
		if (pb.hasMoreData()) {
			System.out.println("More unknown data exists in product "
					+ productName + " in tdb file");
		}
	}

	private static final Pattern filenameParser = Pattern.compile(
			"^(\\d+)\\.img$", Pattern.CASE_INSENSITIVE);

	public MapDesc getMapDescription(ImgFileBag imgFileBag) {
		Matcher matcher = filenameParser
				.matcher(imgFileBag.getFile().getName());
		if (matcher.matches()) {
			int imgNumber = Integer.parseInt(matcher.group(1));
			return maps.get(imgNumber);
		}
		return null;
	}

	public class Area {
		private int number;

		private String name;

		private int coordN;

		private int coordW;

		private int coordS;

		private int coordE;

		public Area(int number, String name, int coordN, int coordW,
				int coordS, int coordE) {
			this.number = number;
			this.name = name;
			this.coordN = coordN;
			this.coordW = coordW;
			this.coordS = coordS;
			this.coordE = coordE;
		}

		public int getNumber() {
			return number;
		}

		public String getName() {
			return name;
		}

		public int getCoordN() {
			return coordN;
		}

		public int getCoordW() {
			return coordW;
		}

		public int getCoordS() {
			return coordS;
		}

		public int getCoordE() {
			return coordE;
		}
	}

	public class MapDesc {
		private int imgNumber;

		private String name;

		private Area area;

		private int coordN;

		private int coordW;

		private int coordS;

		private int coordE;

		public MapDesc(int imgNumber, String name, Area area, int coordN,
				int coordW, int coordS, int coordE) {

			this.imgNumber = imgNumber;
			this.name = name;
			this.area = area;
			this.coordN = coordN;
			this.coordW = coordW;
			this.coordS = coordS;
			this.coordE = coordE;
		}

		public int getImgNumber() {
			return imgNumber;
		}

		public String getName() {
			return name;
		}

		public Area getArea() {
			return area;
		}

		public int getCoordN() {
			return coordN;
		}

		public int getCoordW() {
			return coordW;
		}

		public int getCoordS() {
			return coordS;
		}

		public int getCoordE() {
			return coordE;
		}
	}

	public int getProductNumber() {
		return productNumber;
	}

	public String getProductName() {
		return productName;
	}

	public String getProductType() {
		return productType;
	}
}
