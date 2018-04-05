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

import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Paint;
import android.graphics.Shader;


public class CustomPaint extends Paint
{

    public CustomPaint(int [][] colors)
    {
        Bitmap img = Bitmap.createBitmap(colors[0].length, colors.length, Bitmap.Config.ARGB_8888);
        int w = colors[0].length;
    	for (int i = 0; i < colors.length; i++){
    		img.setPixels(colors[i], 0, w, 0, i, w, 1);                	
        }
    	setStrokeWidth(colors.length);
    	BitmapShader shader = new BitmapShader(img, Shader.TileMode.REPEAT, Shader.TileMode.REPEAT);
        setShader(shader);
    }


    
    

}
