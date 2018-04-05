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

import org.free.garminimg.*;

/**
 * Does the bridge between a map listener that works in Garmin coordinates and a
 * listener that works in map coordinated (pixels)
 */
public class CoordinateConverterListener<COORD> implements MapListener {
	
	private MapTransformer<COORD> transformer;

	private TransformedMapListener listener;

	private int xPoints[] = new int[50];

	private int yPoints[] = new int[50];

	private Point2D.Double tempXY = new Point2D.Double();

	private COORD tempCoord;

	public CoordinateConverterListener(MapTransformer<COORD> transformer,
			TransformedMapListener listener) {
		this.transformer = transformer;
		this.listener = listener;
		tempCoord = transformer.createTempCoord();
	}

	public void addPoint(int type, int subType, int longitude, int latitude,
			Label label, boolean indexed) {
		transformer.wgs84ToMap(CoordUtils.toWGS84Rad(longitude),
				CoordUtils.toWGS84Rad(latitude), tempCoord, tempXY);
		listener.addPoint(type, subType, (int) (tempXY.x + 0.5),
				(int) (tempXY.y + 0.5), label, indexed);
	}

	public void addPoly(int type, int[] longitudes, int[] latitudes,
			int nbPoints, Label label, boolean line) {
		if (xPoints.length < nbPoints) { // avoid allocating too many objects by
											// re-using this one.
											// increase it if needed
			xPoints = new int[nbPoints];
			yPoints = new int[nbPoints];
		}
		transformer.garminGeo2Map(longitudes, latitudes, xPoints, yPoints,
				nbPoints, tempXY, tempCoord);
		listener.addPoly(type, xPoints, yPoints, nbPoints, label, line);
	}

	public void startMap(ImgFileBag file) {
		listener.startMap(file);
	}

	public void startSubDivision(SubDivision subDivision) {
		listener.startSubDivision(subDivision);
	}

	public void finishPainting() {
		listener.finishPainting();
	}
}
