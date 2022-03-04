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
package org.free.garminimg.utils;

import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Region;

import org.free.garminimg.CoordUtils;
import org.free.garminimg.ImgFileBag;
import org.free.garminimg.Label;
import org.free.garminimg.MapListener;
import org.free.garminimg.SubDivision;

import java.util.ArrayList;
import java.util.List;

/**
 * Special map listener used to find an object according to its coordinates.
 */
public class FindObjectByPositionListener implements MapListener {
	
	private final int precision;

	private final int targetlong;

	private final int targetLat;

	private final List<FoundObject> founds = new ArrayList<FoundObject>();

	public FindObjectByPositionListener(int targetlong, int targetLat,
			int precision) {
		this.targetlong = targetlong;
		this.targetLat = targetLat;
		this.precision = precision;
	}

	public List<FoundObject> getFounds() {
		return founds;
	}

	public void addPoint(int type, int subType, int longitude, int latitude,
			Label label, boolean indexed) {
		if (longitude >= targetlong - precision
				&& longitude <= targetlong + precision
				&& latitude >= targetLat - precision
				&& latitude <= targetLat + precision) {
			founds.add(new FoundPoint(type, subType, longitude, latitude,
					label, indexed));
		}
	}

	public void addPoly(int type, int[] longitudes, int[] latitudes,
			int nbPoints, Label label, boolean line) {
		if (isNearPoints(longitudes, latitudes, nbPoints)) {
			if (line) {
				if (isNearLine(longitudes, latitudes, nbPoints))
					founds.add(new FoundPoly(type, longitudes, latitudes,
							nbPoints, label, true));
			} else {
				if (isInPolygon(longitudes, latitudes, nbPoints)) {
					founds.add(new FoundPoly(type, longitudes, latitudes,
							nbPoints, label, false));
				}
			}
		}
	}

	public void startMap(ImgFileBag file) {
	}

	public void startSubDivision(SubDivision subDivision) {
	}

	public void finishPainting() {
	}

	private boolean isInPolygon(int[] longitudes, int[] latitudes, int nbPoints) {
		Path path = new Path();
		path.moveTo(longitudes[0], latitudes[0]);
		for (int cpt = 1; cpt < nbPoints; ++cpt) {
			path.lineTo(longitudes[cpt], latitudes[cpt]);
		}
		path.close();
		RectF rectF = new RectF();
		path.computeBounds(rectF, true);
		Region r = new Region();
		r.setPath(path, new Region((int) rectF.left, (int) rectF.top,
				(int) rectF.right, (int) rectF.bottom));

		return r.contains(targetlong, targetLat);
	}

	private boolean isNearLine(int[] longitudes, int[] latitudes, int nbPoints) {
		for (int cpt = 0; cpt < nbPoints - 1; ++cpt) {
			// double dist=Line2D.ptSegDist(longitudes[cpt], latitudes[cpt],
			// longitudes[cpt+1], latitudes[cpt+1],
			// targetlong, targetLat);
			double p0 = (longitudes[cpt] + longitudes[cpt + 1]) / 2.
					- targetlong;
			double p1 = (latitudes[cpt] + latitudes[cpt + 1]) / 2. - targetLat;
			double dist = Math.sqrt(p0 * p0 + p1 * p1);
			if (dist <= precision)
				return true;
		}
		return false;
	}

	private boolean isNearPoints(int[] longitudes, int[] latitudes, int nbPoints) {
		int minLon = Integer.MAX_VALUE;
		int maxLon = Integer.MIN_VALUE;
		int minLat = Integer.MAX_VALUE;
		int maxLat = Integer.MIN_VALUE;
		for (int cpt = 0; cpt < nbPoints; ++cpt) {
			int lon = longitudes[cpt];
			int lat = latitudes[cpt];
			if (lon < minLon)
				minLon = lon;
			if (lon > maxLon)
				maxLon = lon;
			if (lat < minLat)
				minLat = lat;
			if (lat > maxLat)
				maxLat = lat;
		}

		return CoordUtils.includedInCoordinates(targetlong, targetLat, minLon,
				maxLon, minLat, maxLat);
	}

}
