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

import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Shader;

public class PaintPattern extends Paint {

	public enum PatternStyle {
		/**
		 * Single horizontal line
		 */
		HORIZONTAL, 
		/**
		 * Diagonal line from left bottom, to top right
		 */
		DIAGONAL_1,
		/**
		 * Crossings
		 */
		CROSS,
		/**
		 * Dots style
		 */
		DOTS,
	}
	
	public PaintPattern(PatternStyle style, int color) {
		// convert to DPI sizes
		int baseSize = (int) UtilsGarminImg.getDpPixels(8.0f);
		
		// used variable for spaces
		int spaces = (int) UtilsGarminImg.getDpPixels(2.0f);
		if (spaces < 1) {
			spaces = 1;
		}
		
		// create object
		Bitmap img = Bitmap.createBitmap(baseSize, baseSize, Bitmap.Config.ARGB_8888);
		Canvas c = new Canvas(img);

		// prepare paint
		Paint p = new BasicStroke(1.0f);
		p.setColor(color);
		
		// draw object
		if (style == PatternStyle.HORIZONTAL) {
			c.drawLine(0, baseSize / 2.0f, baseSize, baseSize / 2.0f, p);
		} else if (style == PatternStyle.DIAGONAL_1) {
			c.drawLine(0, baseSize, baseSize, 0, p);
		} else if (style == PatternStyle.CROSS) {
			c.drawLine(spaces, baseSize - spaces, baseSize - spaces, spaces, p);
			c.drawLine(spaces, spaces, baseSize - spaces, baseSize - spaces, p);
		} else if (style == PatternStyle.DOTS) {
			// define parameters
			p.setStrokeWidth(0.0f);
			p.setAntiAlias(false);
			
			
			// draw every pixel
			for (int i = 0, m = img.getWidth(); i < m; i++) {
				boolean evenRow = (i / spaces) % 2 == 0;
				for (int j = 0, n = img.getHeight(); j < n; j++) {
					boolean evenCeil = (j / spaces) % 2 == 0;
					if (evenRow && evenCeil) {
						c.drawPoint(i, j, p);
					} else if (!evenRow && !evenCeil) {
						c.drawPoint(i, j, p);
					}
				}
			}
		}

		// set shader
		BitmapShader shader = new BitmapShader(img, Shader.TileMode.REPEAT,
				Shader.TileMode.REPEAT);
		setShader(shader);
	}
}
