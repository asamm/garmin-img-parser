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
 * A visitor for drawing maps. The coordinates are in Garmin units (use
 * {@link org.free.garminimg.CoordUtils#toWGS84(int)}).
 */
public interface MapListener {
	
	/**
	 * Will be called for every points.
	 * 
	 * @param type
	 *            Type of points (see
	 *            {@link org.free.garminimg.utils.ImgConstants#getPointDesc(int,int)}
	 *            ).
	 * @param subType
	 *            Sub type.
	 * @param longitude
	 *            The longitude in garmin coordinates.
	 * @param latitude
	 *            The latitude in garmin coordinates.
	 * @param label
	 *            The label attached to this point or null if there is none.
	 * @param indexed
	 *            True if the point is indexed.
	 */
	void addPoint(int type, int subType, int longitude, int latitude,
			Label label, boolean indexed);

	/**
	 * Will be called for every polygons/lines.
	 * 
	 * @param type
	 *            Type of polyline/gone (see
	 *            {@link org.free.garminimg.utils.ImgConstants#getPolyDesc(int,boolean)}
	 *            ).
	 * @param longitudes
	 *            The longitudes in garmin coordinates. Don't keep a pointer on
	 *            it for more than the duration of this call.
	 * @param latitudes
	 *            Same as latitudes, but for longitudes.
	 * @param nbPoints
	 *            The number of points available.
	 * @param label
	 *            The label attached to this poly or null if there is none.
	 * @param line
	 *            True if it's a polyline, false if it's a polygon.
	 */
	void addPoly(int type, int[] longitudes, int[] latitudes, int nbPoints,
			Label label, boolean line);

	/**
	 * Called each time we start looking into another IMG file
	 * 
	 * @param file
	 *            The file.
	 */
	void startMap(ImgFileBag file);

	/**
	 * Called each time we start parsing a displayed sub-division.
	 * 
	 * @param subDivision
	 */
	void startSubDivision(SubDivision subDivision);

	/**
	 * Called when everything is done.
	 */
	void finishPainting();
}
