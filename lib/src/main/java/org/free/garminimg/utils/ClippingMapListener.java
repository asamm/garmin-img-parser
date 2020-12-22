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

import org.free.garminimg.CoordUtils;
import org.free.garminimg.ImgFileBag;
import org.free.garminimg.Label;
import org.free.garminimg.SubDivision;

/**
 * Remove the objects that are not in the specified range and cut the ones that
 * are not fully within it.
 */
public class ClippingMapListener implements TransformedMapListener {

	private final int minX;

	private final int maxX;

	private final int minY;

	private final int maxY;

	private final TransformedMapListener next;

    private int[] clippedLongitudes = new int[50];

    private int[] clippedLatitudes = new int[50];

	private int[] clippedLongitudes2 = new int[50];

	private int[] clippedLatitudes2 = new int[50];

	public ClippingMapListener(int minX, int maxX, int minY, int maxY,
			TransformedMapListener next) {
		super();
		this.maxY = maxY;
		this.minY = minY;
		this.maxX = maxX;
		this.minX = minX;
		this.next = next;
	}

	public final void addPoint(int type, int subType, int x, int y,
			Label label, boolean indexed) {
		if (CoordUtils.includedInCoordinates(x, y, minX, maxX, minY, maxY)) {
			next.addPoint(type, subType, x, y, label, indexed);
		}
	}

	public final void addPoly(int type, int[] xPoints, int[] yPoints,
			int nbPoints, Label label, boolean line) {
		if (clippedLongitudes.length < nbPoints + 4) {
			clippedLongitudes = new int[nbPoints + 4];
			clippedLatitudes = new int[nbPoints + 4];
		}

		if (line) {
			clipPolyline(type, xPoints, yPoints, nbPoints, label);
		} else {
			if (clippedLongitudes2.length < nbPoints + 4) {
				clippedLongitudes2 = new int[nbPoints + 4];
				clippedLatitudes2 = new int[nbPoints + 4];
			}
			clipPolygon(type, xPoints, yPoints, nbPoints, label);
		}
	}

	public void startMap(ImgFileBag file) {
		next.startMap(file);
	}

	public void startSubDivision(SubDivision subDivision) {
		next.startSubDivision(subDivision);
	}

	public void finishPainting() {
		next.finishPainting();
	}

	private void clipPolyline(int type, int[] xPoints, int[] yPoints,
			int nbPoints, Label label) {
		int nbClippedPoints = 0;
		for (int cpt = 1; cpt < nbPoints; ++cpt) {
			int delta = computeInterceptionWithBox(xPoints[cpt - 1],
					yPoints[cpt - 1], xPoints[cpt], yPoints[cpt],
					nbClippedPoints, clippedLongitudes, clippedLatitudes, 0xFF);
			if ((delta & 0x20) == 0) { // no transition
				nbClippedPoints += delta & 0x0F;
			} else { // interrupted line (the second point was modified)
				nbClippedPoints += delta & 0x0F;
				next.addPoly(type, clippedLongitudes, clippedLatitudes,
						nbClippedPoints, label, true);
				nbClippedPoints = 0;
			}
		}

		if (nbClippedPoints > 0)
			next.addPoly(type, clippedLongitudes, clippedLatitudes,
					nbClippedPoints, label, true);
	}

	private void clipPolygon(int type, int[] xPoints, int[] yPoints,
			int nbPoints, Label label) {
		int nbClippedPoints = nbPoints;

		nbClippedPoints = clipPolygonOneLimit(xPoints, yPoints,
				nbClippedPoints, TOP, clippedLongitudes, clippedLatitudes);
		nbClippedPoints = clipPolygonOneLimit(clippedLongitudes,
				clippedLatitudes, nbClippedPoints, RIGHT, clippedLongitudes2,
				clippedLatitudes2);
		nbClippedPoints = clipPolygonOneLimit(clippedLongitudes2,
				clippedLatitudes2, nbClippedPoints, BOTTOM, clippedLongitudes,
				clippedLatitudes);
		nbClippedPoints = clipPolygonOneLimit(clippedLongitudes,
				clippedLatitudes, nbClippedPoints, LEFT, clippedLongitudes2,
				clippedLatitudes2);

		if (nbClippedPoints > 0) {
			next.addPoly(type, clippedLongitudes2, clippedLatitudes2,
					nbClippedPoints, label, false);
		}
	}

	private int clipPolygonOneLimit(int[] longitudes, int[] latitudes,
			int nbPoints, int locFilter, int[] targetLongitudes,
			int[] targetLatitudes) {
		if (nbPoints == 0)
			return 0;
		int nbClippedPoints = 0;
		int delta;
		for (int cpt = 1; cpt < nbPoints; ++cpt) {
			delta = computeInterceptionWithBox(longitudes[cpt - 1],
					latitudes[cpt - 1], longitudes[cpt], latitudes[cpt],
					nbClippedPoints, targetLongitudes, targetLatitudes,
					locFilter);
			nbClippedPoints += delta & 0x0F;
		}

		// close the polygon
		delta = computeInterceptionWithBox(longitudes[nbPoints - 1],
				latitudes[nbPoints - 1], longitudes[0], latitudes[0],
				nbClippedPoints, targetLongitudes, targetLatitudes, locFilter);
		nbClippedPoints += delta & 0x0F;

		// remove point added to close the polygon if it was not
		// moved by the clipping
		if (nbClippedPoints > 1
				&& targetLongitudes[0] == targetLongitudes[nbClippedPoints - 1]
				&& targetLatitudes[0] == targetLatitudes[nbClippedPoints - 1]) {
			nbClippedPoints--;
		}

		return nbClippedPoints;
	}

	private int insertPointIfDifferent(int x, int y, int pos, int[] xPoints,
			int[] yPoints) {
		if (pos == 0 || xPoints[pos - 1] != x || yPoints[pos - 1] != y) {
			xPoints[pos] = x;
			yPoints[pos] = y;
			return 1;
		} else {
			return 0;
		}
	}

	/**
	 * Cohen-Sutherland Algorithm for clipping a segment.
	 * 
	 * @return The number of points added in the 4 LSB, if the first point was
	 *         modified in bit 4 and the second point in bit 5.
	 */
	private int computeInterceptionWithBox(int x0, int y0, int x1, int y1,
			int curPos, int[] xPoints, int[] yPoints, int locFilter) {
		int loc0 = getLocation(x0, y0) & locFilter;
		int loc1 = getLocation(x1, y1) & locFilter;
		int result = 0;
		while (true) {
			if (loc0 == 0 && loc1 == 0) { // everybody is inside
				int delta = insertPointIfDifferent(x0, y0, curPos, xPoints,
						yPoints);
				delta += insertPointIfDifferent(x1, y1, curPos + delta,
						xPoints, yPoints);
				return delta | result;
			} else if ((loc0 & loc1) != 0) {
				// no intersection
				return result;
			} else {
				int x;
				int y;
				int loc;
				boolean compute0 = (loc0 != 0);
				if (compute0) {
					x = x0;
					y = y0;
					loc = loc0;
					result |= 0x10;
				} else {
					x = x1;
					y = y1;
					loc = loc1;
					result |= 0x20;
				}

				if ((loc & TOP) != 0) {
					x = (int) (x0 + ((long) x1 - x0) * (maxY - y0) / (y1 - y0));
					y = maxY;
				} else if ((loc & BOTTOM) != 0) {
					x = (int) (x0 + ((long) x1 - x0) * (minY - y0) / (y1 - y0));
					y = minY;
				} else if ((loc & RIGHT) != 0) {
					y = (int) (y0 + ((long) y1 - y0) * (maxX - x0) / (x1 - x0));
					x = maxX;
				} else if ((loc & LEFT) != 0) {
					y = (int) (y0 + ((long) y1 - y0) * (minX - x0) / (x1 - x0));
					x = minX;
				}

				if (compute0) {
					x0 = x;
					y0 = y;
					loc0 = getLocation(x0, y0) & locFilter;
				} else {
					x1 = x;
					y1 = y;
					loc1 = getLocation(x1, y1) & locFilter;
				}
			}
		}
	}

	private static final int TOP = 0x1;

	private static final int BOTTOM = 0x2;

	private static final int LEFT = 0x4;

	private static final int RIGHT = 0x8;

	private int getLocation(int x, int y) {
		int code = 0;
		if (y > maxY)
			code = TOP;
		else if (y < minY)
			code = BOTTOM;

		if (x > maxX)
			code |= RIGHT;
		else if (x < minX)
			code |= LEFT;
		return code;
	}
}
