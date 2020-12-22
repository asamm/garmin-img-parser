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

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.util.Log;

import org.free.garminimg.ImgFileBag;
import org.free.garminimg.Label;
import org.free.garminimg.SimpleLabel;
import org.free.garminimg.SubDivision;
import org.free.garminimg.utils.ImgConstants.LinePolyDrawSpec;
import org.free.garminimg.utils.ImgConstants.PainterStep;

import java.io.IOException;
import java.util.PriorityQueue;

/**
 * Main class for drawing a map
 */
public class MapDrawer implements TransformedMapListener {

	private final static String TAG = MapDrawer.class.getSimpleName();

	private static final boolean DEBUG = false;

	// lock for a draw
	private static final Object lock = new Object();

	private final float dips;

	// QUEUE FOR SORTING

	private class QueueData implements Comparable<QueueData> {

		private final int priority;

		public QueueData(int priority) {
			this.priority = priority;
		}

		@Override
		public int compareTo(QueueData another) {
			return another.priority - priority;
		}
	}

	private class PointQueueData extends QueueData {

		public Bitmap bitmap;
		public float ancho, alto;

		public PointQueueData(int priority, Bitmap bitmap, float ancho,
				float alto) {

			super(priority);
			this.bitmap = bitmap;
			this.alto = alto;
			this.ancho = ancho;
		}
	}

	private class LinePolyQueueData extends QueueData {

		public LinePolyDrawSpec spec;
		public int[] xPoints, yPoints;
		public int nbPoints;
		public String label;

		public LinePolyQueueData(int priority, LinePolyDrawSpec spec,
				int[] xPoints, int[] yPoints, int nbPoints) {

			super(priority);
			this.spec = spec;
			this.xPoints = xPoints;
			this.yPoints = yPoints;
			this.nbPoints = nbPoints;
			this.label = "";
		}

		public void setLabel(String label) {
			if (label == null || label.length() == 0) {
				return;
			}
			this.label = label;
		}
	}

	private final PriorityQueue<PointQueueData> queuePoints =
			new PriorityQueue<PointQueueData>();
	private final PriorityQueue<LinePolyQueueData> queuePolygons =
			new PriorityQueue<LinePolyQueueData>();
	private final PriorityQueue<LinePolyQueueData> queueLines =
			new PriorityQueue<LinePolyQueueData>();

	// PRIVATE PART

	private final Paint pointPaint = new Paint(Paint.FILTER_BITMAP_FLAG);
	// current canvas object
	private final Canvas canvas;
	// current map zoom level
	private final int zoomLevel;
	// method used for global labels
	private final LabelDeClutteringFilter labelFilter;
	// current map configuration
	private final MapConfig mapConfig;

	public MapDrawer(MapConfig config, Canvas g2, int zoomLevel,
			float fontSize, int frontColor, int backColor, float dips) {
		// store parameters
		this.mapConfig = config;
		this.canvas = g2;
		this.zoomLevel = zoomLevel;
		this.dips = dips;

		// clear canvas
		clearCanvas();

		// prepare label filter
		this.labelFilter = new LabelDeClutteringFilter(g2, fontSize,
				frontColor, // Labels
				Utils.getColorTransparent(Color.RED, 120), // Points
                Utils.getColorTransparent(Color.CYAN, 120), // Lines
                Utils.getColorTransparent(Color.MAGENTA, 120)); // Polygons
	}

	private void clearCanvas() {
		if (mapConfig.isOverlay()) {
			canvas.drawColor(Color.TRANSPARENT, Mode.CLEAR);
		} else {
			canvas.drawColor(Color.WHITE);
		}
	}

	public void addPoint(int type, int subType, int x, int y, Label label,
			boolean indexed) {
		ImgConstants.PointDrawSpec setup = ImgConstants.getPointType(type,
				subType);
		Bitmap icon = null;
		try {
			icon = setup.getIcon();
		} catch (IOException e) {
			Log.e(TAG, "error loading icon");
		}

		// define dimensions and add point to draw if has icon
		int width = 0;
		int height = 0;
		if (icon != null) {
			width = icon.getWidth();
			height = icon.getHeight();
			queuePoints.add(new PointQueueData(setup.getPriority(), icon, x
					- width / 2, y - height / 2));
		} else {
			if (setup.hasPoint()) {
				width = 5;
				height = 5;
			}
		}

		// add label to draw
		if (!setup.drawLabelGlobal(zoomLevel)) {
			return;
		}
		int testPriority = mapConfig.getPoiThreshold()
				+ ImgConstants.POINT_BASE_PRIORITY;
		if (mapConfig.isShowPointLabel() && label != null
				&& mapConfig.getQuality() == MapConfig.Quality.FINAL
				&& testPriority >= setup.getPriority())
			labelFilter.addPointLabel(x, y, width, height, label,
					setup.getPriority());
	}

	public void addPoly(int type, int[] xPoints, int[] yPoints, int nbPoints,
			Label label, boolean line) {
//Logger.d(TAG, "addPoly(), type:" + type + ", label:" + label);
		// discard all backgrounds if layer is overlay
		if (mapConfig.isOverlay()) {
			if (type == 75) {
				return;
			}
		}

		// handle poly
		LinePolyDrawSpec setup = null;
		if (line) {
			setup = ImgConstants.getLineType(type, label);
		} else {
			setup = ImgConstants.getPolygonType(type);
		}

		// add object to draw
		if (nbPoints > 0) {
			int[] resx = new int[nbPoints];
			int[] resy = new int[nbPoints];
			System.arraycopy(xPoints, 0, resx, 0, nbPoints);
			System.arraycopy(yPoints, 0, resy, 0, nbPoints);

			LinePolyQueueData queueData = new LinePolyQueueData(
					setup.getPriority(), setup, resx, resy, nbPoints);
			queueData.setLabel(label != null ? label.getNameNoException() : "");
			if (line) {
				queueLines.add(queueData);
			} else {
				queuePolygons.add(queueData);
			}
		}

		// testing numbers
		if (DEBUG) {
			Label typeLabel = new SimpleLabel(String.valueOf(type));
			if (line) {
				// draw also labels
				labelFilter.addLineLabel(xPoints, yPoints, nbPoints, typeLabel,
						setup.getPriority());
			} else {
				// draw also labels
				labelFilter.addSurfaceLabel(xPoints, yPoints, nbPoints,
						typeLabel, setup.getPriority());
			}
			return;
		}

		// add labels
		if (!setup.drawLabelGlobal(zoomLevel)) {
			return;
		}

		if (line) {
			// draw also labels
			if (mapConfig.isShowLineLabel() && label != null
					&& mapConfig.getQuality() == MapConfig.Quality.FINAL) {
				labelFilter.addLineLabel(xPoints, yPoints, nbPoints, label,
						setup.getPriority());
			}
		} else {
			// draw also labels
			if (mapConfig.isShowPolygonLabel() && label != null
					&& mapConfig.getQuality() == MapConfig.Quality.FINAL) {
				labelFilter.addSurfaceLabel(xPoints, yPoints, nbPoints, label,
						setup.getPriority());
			}
		}
	}

	public void startMap(ImgFileBag file) {
	}

	public void startSubDivision(SubDivision subDivision) {
	}

	// protected LineDescription setLineStyle(int type) {
	// LineDescription setup = ImgConstants.getLineType(type, null);
	// return setup;
	// }

	public void finishPainting() {
		synchronized (lock) {
			boolean antialiasing = mapConfig.isAntialiasingEnabled();
			boolean overlay = mapConfig.isOverlay();

			// draw all polygons
			LinePolyQueueData[] polys = convertToArray(queuePolygons);
			for (int i = 0; i < polys.length; i++) {
				LinePolyQueueData data = polys[i];
				data.spec.getPainter().paint(canvas, overlay, zoomLevel,
						data.xPoints, data.yPoints, data.nbPoints,
						PainterStep.FIRST, antialiasing);
			}

			// draw all lines in two steps
			LinePolyQueueData[] lines = convertToArray(queueLines);
			for (int i = 0; i < lines.length; i++) {
				LinePolyQueueData data = lines[i];
				data.spec.getPainter().paint(canvas, overlay, zoomLevel,
						data.xPoints, data.yPoints, data.nbPoints,
						PainterStep.FIRST, antialiasing);
			}
			for (int i = 0; i < lines.length; i++) {
				LinePolyQueueData data = lines[i];
				data.spec.getPainter().paint(canvas, overlay, zoomLevel,
						data.xPoints, data.yPoints, data.nbPoints,
						PainterStep.SECOND, antialiasing);
			}

			// write all points
			while (!queuePoints.isEmpty()) {
				PointQueueData data = queuePoints.poll();
				if (dips > 1.f) {
					canvas.save();
					canvas.scale(dips, dips, data.ancho, data.alto);
					canvas.drawBitmap(data.bitmap, data.ancho, data.alto,
							pointPaint);
					canvas.restore();
				} else
					canvas.drawBitmap(data.bitmap, data.ancho, data.alto,
							pointPaint);
			}

			// draw own labels
			for (int i = 0; i < polys.length; i++) {
				LinePolyQueueData data = polys[i];
				if (data.spec.drawLabelHandle(zoomLevel)) {
					data.spec.getPainter().drawText(canvas, zoomLevel,
							data.xPoints, data.yPoints, data.nbPoints,
							data.label);
				}
			}
			for (int i = 0; i < lines.length; i++) {
				LinePolyQueueData data = lines[i];
				if (data.spec.drawLabelHandle(zoomLevel)) {
					data.spec.getPainter().drawText(canvas, zoomLevel,
							data.xPoints, data.yPoints, data.nbPoints,
							data.label);
				}
			}

			// draw global labels
			labelFilter.paint();
		}
		queuePolygons.clear();
		queueLines.clear();
		queuePoints.clear();
	}

	private LinePolyQueueData[] convertToArray(
			PriorityQueue<LinePolyQueueData> queue) {
		LinePolyQueueData[] lines = new LinePolyQueueData[queue.size()];
		int index = 0;
		while (!queue.isEmpty()) {
			lines[index] = queue.poll();
            index++;
		}
		return lines;
	}
}
