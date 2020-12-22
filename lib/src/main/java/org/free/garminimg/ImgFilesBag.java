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
import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * The main entry point of the library. Manages the list of .img mapFiles.
 * <p>
 * Instances of this class are supposed to be fully thread safe.
 */
public class ImgFilesBag {

	private static final int MAX_OPEN_FILES = 9;
    private static final double UNIT_METER_TO_FEET = 3.2808D;
	private static BitSet mapOthersFilter;

	private static BitSet mapBackgroundFilter;

	private static BitSet mapZonesFilter;

	/**
	 * We cannot have too many files open at the same time (OS limitation) this
	 * list keeps the list of files open.
	 * <p>
	 * Every usages must be synchronised manually.
	 */
	private final List<ImgFileBag> openFiles = new ArrayList<>();

	private final SortedSet<ImgFileBag> mapFiles = Collections
			.synchronizedSortedSet(new TreeSet<>(new FileComparator()));

	private int maxLat = Integer.MIN_VALUE, maxLon = Integer.MIN_VALUE,
			minLat = Integer.MAX_VALUE, minLon = Integer.MAX_VALUE;

	private final ImgContext imgContext;

	public ImgContext getImgContext() {

		return imgContext;
	}

	public ImgFilesBag() {
		//this(new ImgContext(10.0));
        this(new ImgContext(1 / UNIT_METER_TO_FEET));
	}

	public ImgFilesBag(ImgContext imgContext) {

		this.imgContext = imgContext;
	}

	/**
	 * Add a single .img file to the repository.
	 */
	public synchronized void addFile(File file, boolean fullInit,
			SubFileReader subFileReader) throws IOException {
		ImgFileBagExtractor extractor = new ImgFileBagExtractor(file, this,
				subFileReader);
		List<ImgFileBag> toAdd = extractor.createImgFileBags(fullInit);
		if (toAdd.size() > 0) {
			mapFiles.addAll(toAdd);
		}
	}

	/**
	 * Remove every maps.
	 */
	public synchronized void clear() throws IOException {
		mapFiles.clear();
		for (ImgFileBag openFile : openFiles) {
			openFile.close();
		}
		openFiles.clear();
	}

	/**
	 * Read the map for the given coordinates.<br>
	 * This method will try to have the objects sorted in an order that is
	 * suitable for drawing.
	 * 
	 * @param resolution
	 *            The resolution in "Garmin coordinate" units of the details we
	 *            want. The smaller, the higher the details will be. If negative
	 *            gets all the details.
	 * @param listener
	 *            A visitor that will be called for every map objects.
	 * @param objectKindFilter
	 *            Bitset of what kind of object to concider (see ObjectKind for
	 *            possible values).
	 * @see #readMap(int,int,int,int,int,int,BitSet,MapListener)
	 */
	public synchronized void readMapForDrawing(int minLong, int maxLong,
			int minLat, int maxLat, int resolution, int objectKindFilter,
			MapListener listener) throws IOException {
		if (!mapFiles.isEmpty()) {
			readMapForDrawing(minLong, maxLong, minLat, maxLat, resolution,
					listener, mapFiles, objectKindFilter);
		}
		listener.finishPainting();
	}

	/**
	 * Read the map for the given coordinates.
	 * 
	 * @param resolution
	 *            The resolution in "Garmin coordinate" units of the details we
	 *            want. The smaller, the higher the details will be. If negative
	 *            gets all the details.
	 * @param listener
	 *            A visitor that will be called for every map objects.
	 * @param objectKindFilter
	 *            Bitset of what kind of object to concider (see ObjectKind for
	 *            possible values).
	 * @param objectTypeFilter
	 *            If not null, allows to filter by type. Must be used with a
	 *            objectKindFilter allowing only one object kind.
	 * @see org.free.garminimg.MapListener
	 * @see org.free.garminimg.ObjectKind
	 */
	public synchronized void readMap(int minLong, int maxLong, int minLat,
			int maxLat, int resolution, int objectKindFilter,
			BitSet objectTypeFilter, MapListener listener) throws IOException {
		readMap(minLong, maxLong, minLat, maxLat, resolution, objectKindFilter,
				objectTypeFilter, listener, mapFiles);
	}

	public synchronized int[] getAproxGMapslevel() {

		int TILE_S = 512;
		int numTeselas = 0;
		double maxLat = Double.MAX_VALUE;
		double minLat = Double.MAX_VALUE;

		try {
			if (mapFiles.size() > 0) {
				for (ImgFileBag img : mapFiles) {
					int n = img.getNorthBoundary();
					int s = img.getSouthBoundary();
					int w = img.getWestBoundary();
					int e = img.getEastBoundary();
					int num0 = (getMaxLatitude() - getMinLatitude()) / (n - s);
					int num1 = (getMaxLongitude() - getMinLongitude())
							/ (e - w);
					int num = Math.max(num0, num1);
					if (num > numTeselas) {
						numTeselas = num;
						maxLat = CoordUtils.toWGS84(n);
						minLat = CoordUtils.toWGS84(s);
					}
				}
			} else
				return new int[] { 0, 0 };// TODO
			int nn = 0;
			double mejor = Double.MAX_VALUE;
			for (int i = 0; i < 21; i++) {

				int worldSize = 256 * (1 << i);
				int y0 = latToY(minLat, worldSize);
				int y1 = latToY(maxLat, worldSize);
				int d = Math.abs(y0 - y1);
				int comp = Math.abs(d - TILE_S);
				if (comp < mejor) {
					mejor = comp;
					nn = i;
				}
			}
			return new int[] { numTeselas, nn };
		} catch (IOException e1) {
			return new int[] { 0, 0 };
		}
	}

	public synchronized int getMinLongitude() throws IOException {

		if (minLon != Integer.MAX_VALUE)
			return minLon;
		if (mapFiles.size() > 0)
			for (ImgFileBag file : mapFiles) {
				minLon = Math.min(minLon, file.getWestBoundary());
			}
		return minLon;
	}

	public synchronized int getMaxLongitude() throws IOException {
		if (maxLon != Integer.MIN_VALUE)
			return maxLon;
		if (mapFiles.size() > 0)
			for (ImgFileBag file : mapFiles) {
				maxLon = Math.max(maxLon, file.getEastBoundary());
			}
		return maxLon;
	}

	public synchronized int getMinLatitude() throws IOException {
		if (minLat != Integer.MAX_VALUE)
			return minLat;
		if (mapFiles.size() > 0)
			for (ImgFileBag file : mapFiles) {
				minLat = Math.min(minLat, file.getSouthBoundary());
			}
		return minLat;
	}

	public synchronized int getMaxLatitude() throws IOException {
		if (maxLat != Integer.MIN_VALUE)
			return maxLat;
		if (mapFiles.size() > 0)
			for (ImgFileBag file : mapFiles) {
				maxLat = Math.max(maxLat, file.getNorthBoundary());
			}

		return maxLat;
	}

	synchronized void registerOpenFile(ImgFileBag bag) throws IOException {
		int cpt = 0;
		while (openFiles.size() >= MAX_OPEN_FILES && cpt < openFiles.size()) {
			ImgFileBag toClose = openFiles.remove(0);
			if (!toClose.close()) { // failed (was in use), retry with another
									// one
				openFiles.add(toClose);
			}
			++cpt;
		}
		openFiles.add(bag);
	}

	private int latToY(double lat, int worldSize) {
		double sinLat = Math.sin(Math.toRadians(lat));
		double log = Math.log((1.0 + sinLat) / (1.0 - sinLat));
		int y = (int) (worldSize * (0.5 - (log / (4.0 * Math.PI))));
		y = Math.min(y, worldSize - 1);
		return y;
	}

	private void readMap(int minLong, int maxLong, int minLat, int maxLat,
			int resolution, int objectKindFilter, BitSet objectTypeFilter,
			MapListener listener, SortedSet<ImgFileBag> files)
			throws IOException {
		for (ImgFileBag file : files) {
			file.readMap(minLong, maxLong, minLat, maxLat, resolution,
					objectKindFilter, objectTypeFilter, listener);
		}
	}

	private void readMapForDrawing(int minLong, int maxLong, int minLat,
			int maxLat, int resolution, MapListener listener,
			SortedSet<ImgFileBag> files, int objectKindFilter)
			throws IOException {
		// it's very important to read polygons first, to avoid hiding other
		// objects.
		if ((objectKindFilter & ObjectKind.POLYGON) != 0) {
			// first, the map background
			readMap(minLong, maxLong, minLat, maxLat, resolution,
					ObjectKind.POLYGON, getMapBackgroundFilter(), listener,
					files);

			// then, the city limits
			readMap(minLong, maxLong, minLat, maxLat, resolution,
					ObjectKind.POLYGON, getMapCityFilter(), listener, files);

			// then, the definition of small zones
			readMap(minLong, maxLong, minLat, maxLat, resolution,
					ObjectKind.POLYGON, getMapZonesFilter(), listener, files);

			// then, the forests
			readMap(minLong, maxLong, minLat, maxLat, resolution,
					ObjectKind.POLYGON, getMapForestFilter(), listener, files);

			// finally, the rest
			readMap(minLong, maxLong, minLat, maxLat, resolution,
					ObjectKind.POLYGON, getMapOthersFilter(), listener, files);
		}

		// otros poligonos
		if ((objectKindFilter & ObjectKind.EXTENDED_POLYGON) != 0) {
			readMap(minLong, maxLong, minLat, maxLat, resolution,
					ObjectKind.EXTENDED_POLYGON, null, listener, files);
		}

		// lines and points can be read in any order.
		if ((objectKindFilter ^ (ObjectKind.POLYGON | ObjectKind.EXTENDED_POLYGON)) != 0)
			readMap(minLong,
					maxLong,
					minLat,
					maxLat,
					resolution,
					(ObjectKind.ALL ^ (ObjectKind.POLYGON | ObjectKind.EXTENDED_POLYGON))
							& objectKindFilter, null, listener, files);
	}

	private static BitSet getMapBackgroundFilter() {
		if (mapBackgroundFilter == null) {
			mapBackgroundFilter = new BitSet(0xB + 1);
			mapBackgroundFilter.set(ImgConstants.BACKGROUND);
			mapBackgroundFilter.set(ImgConstants.DEFINITION_AREA);
		}
		return mapBackgroundFilter;
	}

	private static BitSet mapForestFilter = null;

	private static BitSet getMapForestFilter() {
		if (mapForestFilter == null) {
			mapForestFilter = new BitSet(ImgConstants.FOREST + 1);
			mapForestFilter.set(0x0E);

			mapForestFilter.set(0x14);
			mapForestFilter.set(0x15);
			mapForestFilter.set(0x16);
			mapForestFilter.set(0x17);
			mapForestFilter.set(0x18);

			mapForestFilter.set(0x1E);
			mapForestFilter.set(0x1F);
			mapForestFilter.set(0x20);

			mapForestFilter.set(0x50);
			mapForestFilter.set(0x53);
			mapForestFilter.set(ImgConstants.FOREST);
		}
		return mapForestFilter;
	}

	private static BitSet mapCityFilter = null;

	private static BitSet getMapCityFilter() {
		if (mapCityFilter == null) {
			mapCityFilter = new BitSet(0x03 + 1);

			mapCityFilter.set(0x01);
			mapCityFilter.set(0x02);
			mapCityFilter.set(0x03);

		}
		return mapCityFilter;
	}

	private static BitSet getMapZonesFilter() {
		if (mapZonesFilter == null) {
			mapZonesFilter = new BitSet(ImgConstants.GRAVEL_AREA + 1);

			mapZonesFilter.set(0x07);
			mapZonesFilter.set(0x0C);
			mapZonesFilter.set(0x0D);
			mapZonesFilter.set(0x0E);
			mapZonesFilter.set(0x0F);
			mapZonesFilter.set(0x11);
			mapZonesFilter.set(0x19);
			mapZonesFilter.set(0x1A);
			mapZonesFilter.set(0x4E);
			mapZonesFilter.set(0x4F);

			mapZonesFilter.set(ImgConstants.STATION_AREA);
			mapZonesFilter.set(ImgConstants.GRAVEL_AREA);

		}
		return mapZonesFilter;
	}

	private static BitSet getMapOthersFilter() {
		if (mapOthersFilter == null) {
			mapOthersFilter = new BitSet(512);
			mapOthersFilter.or(mapBackgroundFilter);
			mapOthersFilter.or(mapForestFilter);
			mapOthersFilter.or(mapCityFilter);
			mapOthersFilter.flip(0, mapOthersFilter.length() - 1);

		}
		return mapOthersFilter;
	}

	private static class FileComparator implements Comparator<ImgFileBag> {
		public int compare(ImgFileBag o1, ImgFileBag o2) {
			// biggest surface (less precise) first
			int result = 0;
			try {
				result = Long.valueOf(o1.getFullSurface()).compareTo(
						o2.getFullSurface());
			} catch (IOException e) {
				// ignored
			}
			if (result != 0)
				return -result;

			// then, by filename
			return o1.getMapCode().compareTo(o2.getMapCode());
		}
	}
}
