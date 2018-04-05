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



public class ShapeStroke extends Paint // implements Stroke
{
//    private Shape shapes[];
//
//    private float advance;
//
//    private boolean repeat=true;
//
//    private AffineTransform t=new AffineTransform();
//
//    private static final float FLATNESS=1;
//
//    public ShapeStroke(Shape shapes, float advance)
//    {
//        this(new Shape[]
//                {shapes}, advance);
//    }
//
//    public ShapeStroke(Shape shapes[], float advance)
//    {
//        this.advance=advance;
//        this.shapes=new Shape[shapes.length];
//
//        for(int i=0; i<this.shapes.length; i++)
//        {
//            Rectangle2D bounds=shapes[i].getBounds2D();
//            t.setToTranslation(-bounds.getCenterX(), -bounds.getCenterY());
//            this.shapes[i]=t.createTransformedShape(shapes[i]);
//        }
//    }
//
//    public Shape createStrokedShape(Shape shape)
//    {
//        GeneralPath result=new GeneralPath();
//        PathIterator it=new FlatteningPathIterator(shape.getPathIterator(null), FLATNESS);
//        float points[]=new float[6];
//        float moveX=0, moveY=0;
//        float lastX=0, lastY=0;
//        float thisX, thisY;
//        int type;
//        float next=0;
//        int currentShape=0;
//        int length=shapes.length;
//
//        while(currentShape<length && !it.isDone())
//        {
//            type=it.currentSegment(points);
//            switch(type)
//            {
//                case PathIterator.SEG_MOVETO:
//                    moveX=lastX=points[0];
//                    moveY=lastY=points[1];
//                    result.moveTo(moveX, moveY);
//                    next=0;
//                    break;
//
//                case PathIterator.SEG_CLOSE:
//                    points[0]=moveX;
//                    points[1]=moveY;
//                    // Fall into....
//
//                case PathIterator.SEG_LINETO:
//                    thisX=points[0];
//                    thisY=points[1];
//                    float dx=thisX-lastX;
//                    float dy=thisY-lastY;
//                    float distance=(float)Math.sqrt(dx*dx+dy*dy);
//                    if(distance>=next)
//                    {
//                        float r=1.0f/distance;
//                        float angle=(float)Math.atan2(dy, dx);
//                        while(currentShape<length && distance>=next)
//                        {
//                            float x=lastX+next*dx*r;
//                            float y=lastY+next*dy*r;
//                            t.setToTranslation(x, y);
//                            t.rotate(angle);
//                            result.append(t.createTransformedShape(shapes[currentShape]), false);
//                            next+=advance;
//                            currentShape++;
//                            if(repeat)
//                                currentShape%=length;
//                        }
//                    }
//                    next-=distance;
//                    lastX=thisX;
//                    lastY=thisY;
//                    break;
//            }
//            it.next();
//        }
//
//        return result;
//    }

}