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

/**
 * Some utlity static methods for converting from and to garmin coordinates.
 */
public abstract class CoordUtils {
	
	public static boolean matchesCoordinates(int minLong1, int maxLong1, int minLat1, int maxLat1,
			int minLong2, int maxLong2, int minLat2, int maxLat2) {
		assert (minLong1 <= maxLong1);
		assert (minLat1 <= maxLat1);
		assert (minLong2 <= maxLong2);
		assert (minLat2 <= maxLat2);
		return !(minLong1 > maxLong2 || maxLong1 < minLong2
				|| minLat1 > maxLat2 || maxLat1 < minLat2);
	}

	/**
	 * @return true if 1 is included in 2
	 */
	public static boolean includedInCoordinates(int minLong1, int maxLong1, int minLat1, int maxLat1,
			int minLong2, int maxLong2, int minLat2, int maxLat2) {
		assert (minLong1 <= maxLong1);
		assert (minLat1 <= maxLat1);
		assert (minLong2 <= maxLong2);
		assert (minLat2 <= maxLat2);
		return minLong1 >= minLong2 && maxLong1 <= maxLong2
				&& minLat1 >= minLat2 && maxLat1 <= maxLat2;
	}

	private static final double COORD_FACTOR = 360.0 / (1 << 24);

	public static double toWGS84(int coord) {
		return coord * COORD_FACTOR;
	}

	private static final double RAD_COORD_FACTOR = 2 * Math.PI / (1 << 24);

	public static double toWGS84Rad(int coord) {
		return coord * RAD_COORD_FACTOR;
	}

	public static int fromWGS84(double coord) {
		return (int) Math.round(coord / COORD_FACTOR);
	}

	public static int fromWGS84Rad(double coord) {
		return (int) Math.round(coord / RAD_COORD_FACTOR);
	}

	/**
	 * Convert a garmin coordinate into a String with degreesDminutes'seconds
	 */
	public static String toDMS(int coord) {
		boolean negative = coord < 0;
		if (negative)
			coord = -coord;
		long tmp = coord;
		tmp = tmp * 360 * 3600 * 100 / (1 << 24);
		long deg = tmp / (3600 * 100);
		tmp = tmp % (3600 * 100);
		long min = tmp / (60 * 100);
		long sec = tmp % (60 * 100);
		return String.format("%s%dd%02d'%02.2f", (negative ? "-" : ""), deg,
				min, sec / 100.0);
	}

	public static boolean includedInCoordinates(int longitude, int latitude,
			int minLon, int maxLon, int minLat, int maxLat) {
		return longitude >= minLon && longitude <= maxLon && latitude >= minLat
				&& latitude <= maxLat;
	}

	public static boolean includedInCoordinates(int[] longitudes,
			int[] latitudes, int minLon, int maxLon, int minLat, int maxLat) {
		int minLon1 = Integer.MAX_VALUE;
		int maxLon1 = Integer.MIN_VALUE;
		int minLat1 = Integer.MAX_VALUE;
		int maxLat1 = Integer.MIN_VALUE;
		for (int cpt = 0; cpt < longitudes.length; ++cpt) {
			int lon = longitudes[cpt];
			int lat = latitudes[cpt];
			if (lon < minLon1)
				minLon1 = lon;
			if (lon > maxLon1)
				maxLon1 = lon;
			if (lat < minLat1)
				minLat1 = lat;
			if (lat > maxLat1)
				maxLat1 = lat;
		}
		return includedInCoordinates(minLon1, maxLon1, minLat1, maxLat1,
				minLon, maxLon, minLat, maxLat);
	}

	public static boolean matchesCoordinates(int[] longitudes, int[] latitudes,
			int nbPoints, int minLon, int maxLon, int minLat, int maxLat) {
		int minLon1 = Integer.MAX_VALUE;
		int maxLon1 = Integer.MIN_VALUE;
		int minLat1 = Integer.MAX_VALUE;
		int maxLat1 = Integer.MIN_VALUE;
		for (int cpt = 0; cpt < nbPoints; ++cpt) {
			int lon = longitudes[cpt];
			int lat = latitudes[cpt];
			if (lon < minLon1)
				minLon1 = lon;
			if (lon > maxLon1)
				maxLon1 = lon;
			if (lat < minLat1)
				minLat1 = lat;
			if (lat > maxLat1)
				maxLat1 = lat;
		}
		return matchesCoordinates(minLon1, maxLon1, minLat1, maxLat1, minLon,
				maxLon, minLat, maxLat);
	}
}
