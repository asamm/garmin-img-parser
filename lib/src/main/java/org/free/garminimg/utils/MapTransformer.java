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

/**
 * Generic class used to do the conversion between 4 different coordinate
 * systems:
 * <ul>
 * <li>WGS84 coordinates
 * <li>Garmin coordinates (derivative from WGS84)
 * <li>User defined coordinate system (X/Y), defined by a converter class
 * <li>Map coordinates (pixels), basically the user defined coordinate system
 * scaled to fit the desired zoom level and position to display.
 * </ul>
 * <p>
 * Since this class is heavily used, a lot of care has been taken to avoid
 * performance problems (mainly memory allocation).
 */
public class MapTransformer<T> implements Cloneable {
	
	private static double MinSize = 1.0 / 3600.0;

	private int margin;

	private double minX;

	private double maxX;

	private double minY;

	private double maxY;

	private int height;

	private int width;

	private Converter<T> converter;

	public MapTransformer(Converter<T> converter, int margin) {
		super();
		this.converter = converter;
		this.margin = margin;
		resetAutoScale();
	}

	/**
	 * Use it to change the dimensions of the display area (in screen pixel)
	 * without changing the details (more/less of the map will be shown.
	 */
	public boolean setDimensions(int width, int height) {
		if (this.width != width || this.height != height) {
			if (isSetupDone() && width != 0 && height != 0) {
				double centerX = (maxX + minX) / 2.0;
				double centerY = (maxY + minY) / 2.0;
				double lengthX = maxX - minX;
				double lengthY = maxY - minY;
				lengthX *= (double) width / this.width / 2.0;
				lengthY *= (double) height / this.height / 2.0;
				maxX = centerX + lengthX;
				minX = centerX - lengthX;
				maxY = centerY + lengthY;
				minY = centerY - lengthY;
			}
			this.width = width;
			this.height = height;
			return true;
		} else
			return false;
	}

	/**
	 * Use it to change the dimensions of the display area (in screen pixel).
	 * That will change the amount of details show, but the same area of the map
	 * will be shown.
	 * <p/>
	 * Beware not to change the aspect ratio!
	 */
	public void changeDimensions(int width, int height) {
		this.width = width;
		this.height = height;
	}

	/**
	 * Must be called before the first call to adjustAutoScaleFromWgs84.
	 */
	public void resetAutoScale() {
		minX = Double.MAX_VALUE;
		maxX = -Double.MAX_VALUE;
		minY = Double.MAX_VALUE;
		maxY = -Double.MAX_VALUE;
	}

	/**
	 * Takes a radian WGS84 coordinate and adjust the scale/position to include
	 * this coordinate. No called too often, so no need to optimize...
	 */
	public void adjustAutoScaleFromWgs84(double lon, double lat) {
		Point2D.Double xy = new Point2D.Double();
		T temp = createTempCoord();
		wgs84ToMapNoScale(lon, lat, temp, xy);
		if (xy.x > maxX)
			maxX = xy.x;
		if (xy.x < minX)
			minX = xy.x;

		if (xy.y > maxY)
			maxY = xy.y;
		if (xy.y < minY)
			minY = xy.y;
	}

	/**
	 * Once several calls to adjustAutoScale have been done, call this to make
	 * sure the aspect ratio is correct.
	 */
	public void fixAspectRatio() {
		fixAspectRatio(true);
	}

	/**
	 * Convert a geographic coordinate into a map position (pixels)
	 */
	public void geo2map(T coord, Point2D.Double result) {
		result.x = scaleX(converter.getX(coord));
		result.y = scaleY(converter.getY(coord));
	}

	/**
	 * Convert a WGS84 coordinate (radians) into a map position (pixels)
	 */
	public void wgs84ToMap(double lon, double lat, T temp, Point2D.Double result) {
		wgs84ToMapNoScale(lon, lat, temp, result);
		result.x = scaleX(result.x);
		result.y = scaleY(result.y);
	}

	/**
	 * convert the map position (pixels) into geographic coordinates.
	 */
	public T map2geo(int x, int y) {
		return map2geoNoScale(unscaleX(x), unscaleY(y));
	}

	/**
	 * convert the map position (pixels) into WGS84 (radians). Longitude in x
	 * and latitude in y.
	 */
	public void map2wgs84(int x, int y, Point2D.Double result) {
		converter.toWGS84(map2geo(x, y), result);
	}

	/**
	 * @return The WGS84 coordinate (radians) of the lower left corner of the
	 *         map.
	 */
	public Point2D.Double getSouthWestWGS84() {
		Point2D.Double result = new Point2D.Double();
		converter.toWGS84(map2geo(0, height), result);
		return result;
	}

	/**
	 * @return The WGS84 coordinate (radians) of the lower left corner of the
	 *         map.
	 */
	public Point2D.Double getSouthEastWGS84() {
		Point2D.Double result = new Point2D.Double();
		converter.toWGS84(map2geo(width, height), result);
		return result;
	}

	/**
	 * @return The WGS84 coordinate (radians) of the upper right corner of the
	 *         map.
	 */
	public Point2D.Double getNorthEastWGS84() {
		Point2D.Double result = new Point2D.Double();
		converter.toWGS84(map2geo(width, 0), result);
		return result;
	}

	/**
	 * @return The WGS84 coordinate (radians) of the upper right corner of the
	 *         map.
	 */
	public Point2D.Double getNorthWestWGS84() {
		Point2D.Double result = new Point2D.Double();
		converter.toWGS84(map2geo(0, 0), result);
		return result;
	}

	/**
	 * @return The bounding box in garmin coordinates
	 */
	public Rectangle getGarminBoundingBox() {
		Point2D.Double sw = getSouthWestWGS84();
		Point2D.Double ne = getNorthEastWGS84();
		Point2D.Double se = getSouthEastWGS84();
		Point2D.Double nw = getNorthWestWGS84();
		int minLon = CoordUtils.fromWGS84Rad(Math.min(sw.x, nw.x));
		int maxLon = CoordUtils.fromWGS84Rad(Math.max(ne.x, se.x));
		int minLat = CoordUtils.fromWGS84Rad(Math.min(sw.y, se.y));
		int maxLat = CoordUtils.fromWGS84Rad(Math.max(ne.y, nw.y));
		return new Rectangle(minLon, minLat, maxLon - minLon, maxLat - minLat);
	}

	/**
	 * Convert a set of garmin coordinate into map positions (pixels).
	 * 
	 * @param longitudes
	 *            the input longitudes
	 * @param latitudes
	 *            the output latitudes
	 * @param xPoints
	 *            the output X positions
	 * @param yPoints
	 *            the output Y positions
	 * @param nbPoints
	 *            the number of points to concider
	 * @param tempXY
	 *            some temporary variable (avoid to do too many allocations for
	 *            perf purpose)
	 * @param tempCoord
	 *            some other temporary variable (avoid to do too many
	 *            allocations for perf purpose)
	 */
	public void garminGeo2Map(int[] longitudes, int[] latitudes, int[] xPoints,
			int[] yPoints, int nbPoints, Point2D.Double tempXY, T tempCoord) {
		for (int cpt = 0; cpt < nbPoints; ++cpt) {
			wgs84ToMap(CoordUtils.toWGS84Rad(longitudes[cpt]),
					CoordUtils.toWGS84Rad(latitudes[cpt]), tempCoord, tempXY);
			xPoints[cpt] = (int) (tempXY.x + 0.5);
			yPoints[cpt] = (int) (tempXY.y + 0.5);
		}
	}

	public boolean isSetupDone() {
		return minX != Double.MAX_VALUE && minX != maxX && minY != maxY
				&& width != 0 && height != 0;
	}

	/**
	 * Move the visible area by some delta given in map coordinates (pixels)
	 */
	public void moveMapPosition(double deltaX, double deltaY) {
		double scaledX = deltaX * (maxX - minX) / (double) (width - 2 * margin);
		double scaledY = -deltaY * (maxY - minY)
				/ (double) (height - 2 * margin);
		minX += scaledX;
		maxX += scaledX;
		minY += scaledY;
		maxY += scaledY;
	}

	/**
	 * If factor>1 zoom in, otherwise zoom out. X and Y specify the new center
	 * of the map.
	 */
	public void zoom(double factor, int x, int y) {
		if (factor <= 0.0)
			return;
		double newWidth = (maxX - minX) * factor;
		double newHeight = (maxY - minY) * factor;
		T coord = map2geo(x, y);
		double centerX = converter.getX(coord);
		double centerY = converter.getY(coord);
		minX = centerX - newWidth / 2;
		maxX = centerX + newWidth / 2;
		minY = centerY - newHeight / 2;
		maxY = centerY + newHeight / 2;
	}

	/**
	 * If factor>1 zoom in, otherwise zoom out. The center of the map doesn't
	 * move.
	 */
	public void zoom(double factor) {
		if (factor <= 0.0)
			return;
		double deltaX = (maxX - minX) * (factor - 1.0) / 2.0;
		double deltaY = (maxY - minY) * (factor - 1.0) / 2.0;
		maxX += deltaX;
		minX -= deltaX;
		maxY += deltaY;
		minY -= deltaY;
	}

	public T map2geoNoScale(double x, double y) {
		return converter.createFromXY(x, y);
	}

	public void scale(Point2D.Double xy, Point2D.Double result) {
		result.x = scaleX(xy.x);
		result.y = scaleY(xy.y);
	}

	public void unscale(Point2D xy, Point2D.Double result) {
		result.x = unscaleX(xy.getX());
		result.y = unscaleY(xy.getY());
	}

	public void wgs84ToMapNoScale(double lon, double lat, T temp,
			Point2D.Double result) {
		converter.fromWGS84(lon, lat, temp);
		result.x = converter.getX(temp);
		result.y = converter.getY(temp);
	}

	public String toString() {
		StringBuilder result = new StringBuilder("Coords=");
		result.append(minX).append(',');
		result.append(minY).append(',');
		result.append(maxX).append(',');
		result.append(maxY);
		result.append(" size=").append(width).append(',').append(height);
		return result.toString();
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

	private int scaleY(double d) {
		return (int) ((maxY - d) * (height - 2 * margin) / (maxY - minY))
				+ margin;
	}

	private double unscaleY(double y) {
		return maxY - (y - margin) * (maxY - minY)
				/ (double) (height - 2 * margin);
	}

	private int scaleX(double d) {
		return (int) ((d - minX) * (width - 2 * margin) / (maxX - minX))
				+ margin;
	}

	private double unscaleX(double x) {
		return (x - margin) * (maxX - minX) / (double) (width - 2 * margin)
				+ minX;
	}

	private void fixAspectRatio(boolean noMin) {
		if (isSetupDone()) {
			double x = maxX - minX;
			double y = maxY - minY;
			double realWidth = width - 2 * margin;
			double realHeight = height - 2 * margin;
			if (y <= 0 || x / y > realWidth / realHeight) {
				double correction = (x * realHeight / realWidth - y) / 2;
				minY -= correction;
				maxY += correction;
			} else {
				double correction = (realWidth * y / realHeight - x) / 2;
				minX -= correction;
				maxX += correction;
			}

			if (!noMin && x < 2.0 * MinSize && y < 2.0 * MinSize) {
				maxX = (maxX + minX) / 2 + MinSize;
				minX = (maxX + minX) / 2 - MinSize;
				maxY = (maxY + minY) / 2 + MinSize;
				minY = (maxY + minY) / 2 - MinSize;
				fixAspectRatio(true);
			}
		}
	}

	public T createTempCoord() {
		return converter.createFromXY(0, 0);
	}

	/**
	 * @return A rough estimate of how many pixels is one meter.
	 */
	public double getPixelsPerMeter() {
		double altAng = 2.0d * Math.PI / 40041d; // what angle in radian is one
													// kilometer at the surface
													// of the earth

		Point2D.Double mapAlt = new Point2D.Double();
		Point2D.Double map = new Point2D.Double();
		T temp = createTempCoord();

		Point2D.Double wgs84 = getNorthEastWGS84();
		wgs84ToMap(wgs84.getX(), wgs84.getY(), temp, map);
		wgs84ToMap(wgs84.getX() + altAng, wgs84.getY(), temp, mapAlt);

		return (mapAlt.getX() - map.getX()) / 1000;
	}

	/**
	 * Interface for the class converting from/to the user defined coordinate
	 * system and datum to/from WGS84.
	 * <p>
	 * Must be state less and thread safe.
	 */
	public interface Converter<T2> {
		/**
		 * Convert the WGS84 (radians) into the coordinate system.
		 */
		void fromWGS84(double lon, double lat, T2 result);

		/**
		 * Convert the coordinate system into WGS84 (radians).
		 * 
		 * @param result
		 *            Where the result is put. Longitude in x and latitude in y.
		 */
		void toWGS84(T2 coordinate, Point2D.Double result);

		/**
		 * Create a coordinate from a X/Y position.
		 */
		T2 createFromXY(double x, double y);

		double getX(T2 xy);

		double getY(T2 xy);
	}

	public MapTransformer<T> clone() {
		MapTransformer<T> result = new MapTransformer<T>(converter, margin);
		result.setFrom(this);
		return result;
	}

	public void setFrom(MapTransformer<T> other) {
		minX = other.minX;
		maxX = other.maxX;
		minY = other.minY;
		maxY = other.maxY;
		height = other.height;
		width = other.width;
	}

	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;

		MapTransformer<?> that = (MapTransformer<?>) o;

		return height == that.height && width == that.width
				&& margin == that.margin
				&& Double.compare(that.maxX, maxX) == 0
				&& Double.compare(that.maxY, maxY) == 0
				&& Double.compare(that.minX, minX) == 0
				&& Double.compare(that.minY, minY) == 0
				&& converter.equals(that.converter);

	}

	public int hashCode() {
		int result;
		long temp;
		result = margin;
		temp = minX != +0.0d ? Double.doubleToLongBits(minX) : 0L;
		result = 31 * result + (int) (temp ^ (temp >>> 32));
		temp = maxX != +0.0d ? Double.doubleToLongBits(maxX) : 0L;
		result = 31 * result + (int) (temp ^ (temp >>> 32));
		temp = minY != +0.0d ? Double.doubleToLongBits(minY) : 0L;
		result = 31 * result + (int) (temp ^ (temp >>> 32));
		temp = maxY != +0.0d ? Double.doubleToLongBits(maxY) : 0L;
		result = 31 * result + (int) (temp ^ (temp >>> 32));
		result = 31 * result + height;
		result = 31 * result + width;
		result = 31 * result + converter.hashCode();
		return result;
	}
}
