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

import org.free.garminimg.Label;
import org.free.garminimg.UtilsGarminImg;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;

import java.io.IOException;
import java.util.ArrayList;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Sort the labels by importance for latter drawing.
 */
public class LabelDeClutteringFilter {

	private final Paint paint = new Paint();
	private final Paint paintRect = new Paint();

	private final Canvas g2;

	private final int frontColor;

	private final int labelBackgroundColor;
    private final int lineBackgroundColor;
    private final int polygonBackgroundColor;

	private final SortedSet<LabelInfo> everyLabels = new TreeSet<LabelInfo>();

	private final SortedSet<String> knownNames = new TreeSet<String>();

	private final ArrayList<LabelInfo> toPaintLabels = new ArrayList<LabelInfo>();

	private static final int MARGIN_X = 5;

	private static final int MARGIN_Y = 2;

	private static final int LINE_LENGTH = 2;

	private static final int LINE_MARGIN = 2;

	public LabelDeClutteringFilter(Canvas g2, float fontSize, int frontColor,
			int labelBackgroundColor, int lineBackgroundColor,
			int polygonBackgroundColor) {
		this.g2 = g2;
		this.frontColor = frontColor;
		this.labelBackgroundColor = labelBackgroundColor;
		this.lineBackgroundColor = lineBackgroundColor;
		this.polygonBackgroundColor = polygonBackgroundColor;
		paint.setAntiAlias(true);
		// paint.setTypeface(Typeface.DEFAULT_BOLD);
		paint.setTextSize(fontSize);
		paintRect.setStyle(Paint.Style.FILL_AND_STROKE);
		// paintRect.setColor(labelBackgroundColor);
	}

	/**
	 * Will try to put the label horizontally around the point
	 */
	public void addPointLabel(int x, int y, int width, int height, Label label,
			int priority) {
		if (width != 0 && height != 0)
			addLabel(x + width / 2 + 3, y, label, Placement.MIDDLE_LEFT,
					priority, labelBackgroundColor);
		else
			addLabel(x, y, label, Placement.MIDDLE_CENTER, priority,
					labelBackgroundColor);

	}

	/**
	 * Will try to put the label attached to one of the segments of the given
	 * line
	 */
	public void addLineLabel(int[] xPoints, int[] yPoints, int nbPoints,
			Label label, int priority) {

		int middle = nbPoints / 2;
		if (middle == nbPoints - 1)
			middle--;

		final Placement placement;
		int x;
		int y;
		int dx = 0;
		int dy = 0;
		if (middle >= 0) {
			int x1 = xPoints[middle];
			int y1 = yPoints[middle];
			int x2 = xPoints[middle + 1];
			int y2 = yPoints[middle + 1];
			x = (x1 + x2) / 2;
			y = (y1 + y2) / 2;

			if (Math.abs(x1 - x2) < Math.abs(y1 - y2)) {
				// mainly horizontal
				placement = Placement.MIDDLE_LEFT;
				dx = 3;
			} else {
				// mainly vertical
				placement = Placement.TOP_CENTER;
				dy = 3;
			}

		} else {
			x = xPoints[0];
			y = yPoints[0];
			placement = Placement.MIDDLE_LEFT;
			dx = 3;
		}

		addLabel(x + dx, y + dy, label, placement, priority,
				lineBackgroundColor);
	}

	/**
	 * will try to put the label horizontaly within the given surface
	 */
	public void addSurfaceLabel(int[] xPoints, int[] yPoints, int nbPoints,
			Label label, int priority) {
		Point2D center = UtilsGarminImg.computeCenter(xPoints, yPoints, nbPoints);
		int minX = Integer.MAX_VALUE;
		int minY = Integer.MAX_VALUE;
		int maxX = Integer.MIN_VALUE;
		int maxY = Integer.MIN_VALUE;
		for (int cpt = 0; cpt < nbPoints; ++cpt) {
			minX = Math.min(minX, xPoints[cpt]);
			minY = Math.min(minY, yPoints[cpt]);
			maxX = Math.max(maxX, xPoints[cpt]);
			maxY = Math.max(maxY, yPoints[cpt]);
		}

		// finally add label
		addLabel((int) center.getX(), (int) center.getY(), label,
				Placement.MIDDLE_CENTER, priority, polygonBackgroundColor);
	}

	private enum Placement {
		MIDDLE_LEFT, TOP_CENTER, MIDDLE_CENTER
	}

	private class LabelInfo implements Comparable<LabelInfo> {
		private final String name;

		private final RectF bounds;

		private int x;

		private int y;

		private final Placement placement;

		private final boolean firstInstance;

		private final int priority;

		private final int backColor;

		public LabelInfo(String name2, Rect bounds2, int x2, int y2,
				int priority2, boolean firstInstance2, Placement placement2,
				int backColor) {
			this.firstInstance = firstInstance2;
			this.priority = priority2;
			this.name = name2;
			this.backColor = backColor;
			bounds = new RectF();
			this.x = x2;
			this.y = y2;
			this.bounds.set(bounds2.left, bounds2.top, bounds2.right,
					bounds2.bottom);// =boundings;
			if (bounds.left < 0) {
				x -= bounds.left;
				bounds.offset(-bounds.left, 0);
			} else if (bounds.right > 511) {
				x += 512 - bounds.right;
				bounds.offset(512 - bounds.right, 0);
			}
			if (bounds.top < 0) {
				y -= bounds.top;
				bounds.offset(0, -bounds.top);
			} else if (bounds.bottom > 511) {
				y += 512 - bounds.bottom;
				bounds.offset(0, 512 - bounds.bottom);
			}
			this.placement = placement2;
		}

		public void paint(Canvas g2)// , int frontColor, int backColor)
		{
			paintRect.setColor(backColor);
			g2.drawRoundRect(bounds, 3f, 3f, paintRect);

			// paint.setColor(frontColor);
			g2.drawText(name, x, y, paint);
			// switch(placement)
			// {
			// case MIDDLE_CENTER:
			// break;
			// case MIDDLE_LEFT:
			// int cy=(int)(bounds.centerY()+0.5f);
			// g2.drawLine(x-LINE_MARGIN, cy, x-LINE_LENGTH-LINE_MARGIN, cy,
			// paint);
			// break;
			// case TOP_CENTER:
			// int cx=(int)(bounds.centerX()+0.5f);
			// int ty=y-(int)(bounds.height()+0.5f)+2*MARGIN_Y;
			// g2.drawLine(cx, ty-LINE_MARGIN, cx,
			// ty-LINE_LENGTH-LINE_MARGIN,paint);
			// break;
			// }
		}

		public int compareTo(LabelInfo o) {
			if (firstInstance != o.firstInstance)
				return firstInstance ? -1 : 1;
			if (priority != o.priority)
				return priority - o.priority;
			int nameCompare = name.compareTo(o.name);
			if (nameCompare != 0) {
				return nameCompare;
			}
			if (x != o.x)
				return x - o.x;
			return y - o.y;
		}
	}

	public void paint() {
		// compute what label has to be painted
		for (LabelInfo labelInfo : everyLabels) {
			testAddLabelToPaint(labelInfo);
		}

		// paint.setColor(frontColor);
		for (int i = 0; i < toPaintLabels.size(); i++) {
			LabelInfo info = toPaintLabels.get(i);
			info.paint(g2);// , frontColor, backgroundColor);
		}
	}

	/**
	 * Check if a label can be painted or not (overlapping with another one).
	 * Add it to {@link #toPaintLabels} if it can be painted.
	 */
	private void testAddLabelToPaint(LabelInfo labelInfo) {
		for (int i = 0; i < toPaintLabels.size(); i++) {
			LabelInfo info = toPaintLabels.get(i);
			if (info.bounds.intersects(labelInfo.bounds.left,
					labelInfo.bounds.top, labelInfo.bounds.right,
					labelInfo.bounds.bottom))
				return;
		}
		toPaintLabels.add(labelInfo);
	}

	public static final int ROAD_LABEL_BACKGROUND = Color.argb(220, 160, 255,
			255);

	private final Rect textBounds = new Rect();
	
	private void addLabel(int x, int y, Label label, Placement placement,
			int priority, int backColor) {
		String name;
		try {
			name = label.getName();
			if (name != null && name.length() > 0) {
				int ini = name.charAt(0);
				if (ini < 10) {
					name = name.substring(1);
					placement = Placement.MIDDLE_CENTER;
					backColor = ROAD_LABEL_BACKGROUND;
				}
				// layout.getBounds();
				paint.getTextBounds(name, 0, name.length(), textBounds);
				switch (placement) {
				case MIDDLE_CENTER:
					x -= textBounds.centerX();
					y -= textBounds.centerY();
					break;
				case MIDDLE_LEFT:
					y -= textBounds.centerY();
					x += LINE_LENGTH + LINE_MARGIN;
					break;
				case TOP_CENTER:
					x -= textBounds.centerX();
					y += textBounds.height() + LINE_LENGTH + LINE_MARGIN;
					break;
				}

				// translate the bounds
				textBounds.set(x + textBounds.left - MARGIN_X, y + textBounds.top
						- MARGIN_Y, x + textBounds.right + MARGIN_X, y
						+ textBounds.bottom + MARGIN_Y);

				boolean firstInstance = knownNames.add(name);
				everyLabels.add(new LabelInfo(name, textBounds, x, y, priority,
						firstInstance, placement, backColor));
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
