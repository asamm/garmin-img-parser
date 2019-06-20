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
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;

import org.free.garminimg.Label;
import org.free.garminimg.UtilsGarminImg;
import org.free.garminimg.render.painters.LinePolyPainter;
import org.free.garminimg.render.style.StyleInternal;

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;

public class ImgConstants {

    // private static final String TAG = ImgConstants.class.getSimpleName();

    // CONSTANTS

    public static final int BG_COLOR = Color.rgb(255, 255, 240);

    public static final int ZOOM_MIN = 0;
    public static final int ZOOM_MAX = 100;

    // POINTS IDS

    public static final int ELEVATION = 0x6300;

    public static final int ELEVATION1 = 0x6616;

    // LINES IDS

    public static final int ID_LINE_PAVEMENT = 0x13;
    public static final int ID_LINE_RAILROAD = 0x14;
    public static final int ID_LINE_TRAIL = 0x16;

    // contours
    public static final int MINOR_LAND_CONTOUR = 0x20;
    public static final int INTERMEDIATE_LAND_CONTOUR = 0x21;
    public static final int MAJOR_LAND_CONTOUR = 0x22;

    public static final int MINOR_DEPTH_CONTOUR = 0x23;
    public static final int INTERMEDIATE_DEPTH_CONTOUR = 0x24;
    public static final int MAJOR_DEPTH_CONTOUR = 0x25;

    public static final int TUNNEL_SHIFT = 0x100;

    public static final int RUINS = 0x150;

    // POLYGONS IDS

    public static final int AIRPORT = 0x07;

    public static final int SHOPPING_CENTER = 0x08;

    public static final int DEFINITION_AREA = 0x4a;

    public static final int BACKGROUND = 0x4b;

    public static final int FOREST = 0x101;

    public static final int STATION_AREA = 0x102;

    public static final int GRAVEL_AREA = 0x103;

    // PUBLIC PARAMETERS

    public static Paint pLabelsBg;
    public static Paint pLabelsFg;

    static {
        pLabelsBg = new Paint(Paint.ANTI_ALIAS_FLAG);
        pLabelsBg.setStyle(Style.FILL_AND_STROKE);
        pLabelsBg.setStrokeWidth(4.0f);
        pLabelsBg.setTextSize(UtilsGarminImg.getBaseTextSize());
        pLabelsBg.setColor(Color.WHITE);

        pLabelsFg = new Paint(pLabelsBg);
        pLabelsFg.setStrokeWidth(0.0f);
        pLabelsFg.setColor(Color.BLACK);
    }

    // PRIORITIES

    /**
     * Points have the highest priority
     */
    public static final int POINT_BASE_PRIORITY = 1 << 8;

    /**
     * Polygons have a middle priority
     */
    public static final int POLYGON_BASE_PRIORITY = 3 << 8;

    /**
     * Lines have the lowest priority
     */
    public static final int POLYLINE_BASE_PRIORITY = 5 << 8;

    /**************************************************/
    /*                 PUBLIC GETTERS                 */
    /**************************************************/

    // instance of style
    private static StyleInternal style = StyleInternal.getInstance();

    // GET POINT

    public static PointDrawSpec getPointType(int type, int subType) {
        return style.getPointType(type, subType);
    }

    // GET LINE

    public static LinePolyDrawSpec getLineType(int type) {
        return getLineType(type, null);
    }

    public static LinePolyDrawSpec getLineType(int type, Label label) {
        return style.getLineType(type, label);
    }

    // GET POLY

    public static LinePolyDrawSpec getPolygonType(int type) {
        return style.getPolygonType(type);
    }

    /**************************************************/
    /*               DRAW CONTAINERS                  */

    /**************************************************/

    public enum PainterStep {

        FIRST, SECOND
    }

    public enum DrawLabel {

        /**
         * Never draw texts for this items
         */
        NEVER,
        /**
         * Painter itself will handle texts
         */
        HANDLE,
        /**
         * Use Global existing labeling system
         */
        GLOBAL
    }

    static class DrawSpecification {

        // ID of current item (type from img file)
        private int type;
        // description, sometimes name, sometimes label
        private String description;
        // priority in draw
        private int priority;

        // flag how to draw labels
        private DrawLabel drawlabel;

        // limits for labels
        private int zoomMinText;
        private int zoomMaxText;

        public DrawSpecification(int type, String description, int priority) {
            this.priority = priority;
            this.type = type;
            this.description = description;

            this.drawlabel = DrawLabel.GLOBAL;
            this.zoomMinText = ImgConstants.ZOOM_MIN;
            this.zoomMaxText = ImgConstants.ZOOM_MAX;
        }

        public String getDescription() {
            return description;
        }

        public int getType() {
            return type;
        }

        public void setType(int type) {
            this.type = type;
        }

        public DrawLabel getDrawLabel() {
            return drawlabel;
        }

        public boolean drawLabelGlobal(int zoomLevel) {
            return drawlabel == DrawLabel.GLOBAL && shouldDrawLabel(zoomLevel);
        }

        public boolean drawLabelHandle(int zoomLevel) {
            return drawlabel == DrawLabel.HANDLE && shouldDrawLabel(zoomLevel);
        }

        private boolean shouldDrawLabel(int zoomLevel) {
            return zoomLevel >= zoomMinText && zoomLevel <= zoomMaxText;
        }

        public void setDrawLabel(DrawLabel drawLabel) {
            this.drawlabel = drawLabel;
        }

        public void setDrawLabelsParams(int minLevel, int maxLevel) {
            this.zoomMinText = minLevel;
            this.zoomMaxText = maxLevel;
        }

        public int getPriority() {
            return priority;
        }

        public void setPriority(int priority) {
            this.priority = priority;
        }
    }

    public static class PointDrawSpec extends DrawSpecification {

        private static SparseArrayCompat<WeakReference<Bitmap>> cacheBitmaps = new SparseArrayCompat<>();

        private int subType;

        private int fullType;

        private String iconName;

        private Bitmap icon;

        private int[][] iconColors;

        private boolean point;

        public PointDrawSpec(int fullType, String description, String iconName,
                boolean point, int priority) {
            super(fullType >> 8, description, POINT_BASE_PRIORITY + priority);
            this.fullType = fullType;
            this.subType = fullType & 0xFF;
            this.iconName = iconName;
            this.icon = null;
            this.point = point;
        }

        public PointDrawSpec(int fullType, String description,
                int[][] iconColors, boolean point, int priority) {
            super(fullType >> 8, description, POINT_BASE_PRIORITY + priority);
            this.fullType = fullType;
            this.subType = fullType & 0xFF;
            this.iconColors = iconColors;
            this.icon = null;
            this.point = point;
        }

        public int getSubType() {
            return subType;
        }

        public String getIconName() {
            return iconName;
        }

        public Bitmap getIcon() throws IOException {
            if (icon == null) {
                WeakReference<Bitmap> iconRef = cacheBitmaps.get(fullType);
                if (iconRef != null) {
                    icon = iconRef.get();
                    if (icon != null) {
                        return icon;
                    }
                }
                if (iconName != null && iconName.length() > 0) {
                    String name = String.format("garminimg/icons/icon_%s.png", iconName);
                    InputStream is = UtilsGarminImg.getContext().getAssets().open(name);
                    icon = BitmapFactory.decodeStream(is);
                    is.close();
                } else if (iconColors != null) {
                    icon = Bitmap.createBitmap(iconColors[0].length,
                            iconColors.length, Bitmap.Config.ARGB_8888);
                    int w = iconColors[0].length;
                    for (int i = 0; i < iconColors.length; i++) {
                        icon.setPixels(iconColors[i], 0, w, 0, i, w, 1);
                    }
                }

                // finalize icon size and add it to cache
                icon = UtilsGarminImg.prepareLoadedIcon(icon);
                cacheBitmaps.put(fullType, new WeakReference<>(icon));
            }
            return icon;
        }

        public boolean hasPoint() {
            return point;
        }
    }

    public static class LinePolyDrawSpec extends DrawSpecification {

        private LinePolyPainter painter;

        public LinePolyPainter getPainter() {

            return painter;
        }

        public LinePolyDrawSpec(int type, String description,
                LinePolyPainter painter, int basePriority, int priority) {
            super(type, description, basePriority + priority);
            this.painter = painter;
        }
    }
}
