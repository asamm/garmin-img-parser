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

import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathDashPathEffect;

/**
 * Draw small "+" to show a country border.
 */
public class InternationalBorderStroke extends Paint {

	public InternationalBorderStroke(float delta, float crossWidth,
			float lineWidth) {
		lineWidth /= 2.0;
		crossWidth /= 2.0;
		Path line = new Path();
		line.moveTo(lineWidth, crossWidth);
		line.lineTo(-lineWidth, crossWidth);
		line.lineTo(-lineWidth, lineWidth);
		line.lineTo(-crossWidth, lineWidth);
		line.lineTo(-crossWidth, -lineWidth);
		line.lineTo(-lineWidth, -lineWidth);
		line.lineTo(-lineWidth, -crossWidth);
		line.lineTo(lineWidth, -crossWidth);
		line.lineTo(lineWidth, -lineWidth);
		line.lineTo(crossWidth, -lineWidth);
		line.lineTo(crossWidth, lineWidth);
		line.lineTo(lineWidth, lineWidth);
		line.lineTo(lineWidth, crossWidth);

		PathDashPathEffect pe = new PathDashPathEffect(line, delta, 0,
				PathDashPathEffect.Style.ROTATE);
		setAntiAlias(true);
		setStyle(Paint.Style.STROKE);
		setPathEffect(pe);

	}
}
