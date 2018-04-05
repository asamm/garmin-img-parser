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
package org.free.garminimg.utils;

import org.free.garminimg.ImgFileBag;
import org.free.garminimg.Label;
import org.free.garminimg.SubDivision;

/**
 * A map visitor with coordinates already projected (expressed in pixels).
 * 
 * @see org.free.garminimg.MapListener
 */
public interface TransformedMapListener {
	
	/**
	 * Will be called for every points.
	 * 
	 * @param type
	 *            Type of points (see
	 *            {@link org.free.garminimg.utils.ImgConstants#getPointDesc(int,int)}
	 *            ).
	 * @param subType
	 *            Sub type.
	 * @param x
	 *            The X coordinate in pixels.
	 * @param y
	 *            The Y coordinate in pixels.
	 * @param label
	 *            The label attached to this point or null if there is none.
	 * @param indexed
	 *            True if the point is indexed.
	 */
	void addPoint(int type, int subType, int x, int y, Label label,
			boolean indexed);

	/**
	 * Will be called for every polygons/lines.
	 * 
	 * @param type
	 *            Type of polyline/gone (see
	 *            {@link org.free.garminimg.utils.ImgConstants#getPolyDesc(int,boolean)}
	 *            ).
	 * @param xPoints
	 *            The X coordinates in pixels. Don't keep a pointer on it for
	 *            more than the duration of this call.
	 * @param yPoints
	 *            Same as xPoints, but for y coordinates.
	 * @param nbPoints
	 *            The number of points available.
	 * @param label
	 *            The label attached to this poly or null if there is none.
	 * @param line
	 *            True if it's a polyline, false if it's a polygon.
	 */
	void addPoly(int type, int[] xPoints, int[] yPoints, int nbPoints,
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
