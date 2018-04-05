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
package org.free.garminimg.render.paint;

import org.free.garminimg.UtilsGarminImg;

import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathDashPathEffect;

/**
 * Used to draw train rails.
 */
public class PaintPath extends Paint {

	public PaintPath(Path path, int color, float stroke, float advance) {
		super();
		setAntiAlias(true);
		setColor(color);
		setStyle(Paint.Style.STROKE);
		setStrokeWidth(UtilsGarminImg.getDpPixels(stroke));
		PathDashPathEffect pe = new PathDashPathEffect(path, 
				UtilsGarminImg.getDpPixels(advance), 0,
				PathDashPathEffect.Style.ROTATE);
		setPathEffect(pe);
	}
}
