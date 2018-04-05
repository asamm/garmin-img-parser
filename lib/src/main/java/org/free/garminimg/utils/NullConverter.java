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

/**
 *
 */
package org.free.garminimg.utils;

import org.free.garminimg.utils.MapTransformer.Converter;

/**
 * A dummy coordinate converter doing no transformation.
 */
public class NullConverter implements Converter<Point2D.Double> {
	
	public void fromWGS84(double lon, double lat, Point2D.Double result) {
		result.x = lon;
		result.y = lat;
	}

	public void toWGS84(Point2D.Double coordinate, Point2D.Double result) {
		result.x = coordinate.x;
		result.y = coordinate.y;
	}

	public Point2D.Double createFromXY(double x, double y) {
		return new Point2D.Double(x, y);
	}

	public double getX(Point2D.Double xy) {
		return xy.x;
	}

	public double getY(Point2D.Double xy) {
		return xy.y;
	}

}