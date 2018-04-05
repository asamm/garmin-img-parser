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
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Shader;



public class RocksPaint extends Paint //implements Paint
{

    private static final int size=8;

    public RocksPaint()
    {
        Bitmap img = Bitmap.createBitmap(size, size, Bitmap.Config.RGB_565);//new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Canvas c = new Canvas(img);
        Paint p = new BasicStroke(1f);
        p.setColor(Color.GRAY);
        c.drawLine(0, 0, size, size, p);
        BitmapShader shader = new BitmapShader(img, Shader.TileMode.REPEAT, Shader.TileMode.REPEAT);
        this.setShader(shader);

    }


}
