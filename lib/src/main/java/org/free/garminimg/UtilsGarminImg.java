package org.free.garminimg;

import android.app.Application;
import android.content.Context;
import android.graphics.Bitmap;

import org.free.garminimg.utils.Point2D;
import org.free.garminimg.utils.Utils;

public class UtilsGarminImg {

    private static final Application app;
    private static float density = 2.0f;
    private static float textScale = 1.0f;

    public static void  setBase(float density, float textScale) {
        UtilsGarminImg.density = density;
        UtilsGarminImg.textScale = textScale;
    }

	public static float getDpPixels(float pixels) {
		return density * pixels;
	}
	
	public static float getBaseTextSize() {
		return density * 10.0f * textScale;
	}

	public static Bitmap prepareLoadedIcon(Bitmap icon) {
		return Utils.resize(icon, (int) getDpPixels(12.0f));
    }

    public static Context getContext() {
        return app.getApplicationContext();
    }

    static {
        try {
            Class<?> c = Class.forName("android.app.ActivityThread");
            app = (Application) c.getDeclaredMethod("currentApplication").invoke(null);
        } catch (Exception e) {
            throw new AssertionError(e);
        }
    }
	
	// VARIOUS TOOLS
	
	public static Point2D computeCenter(int[] xPoints, int[] yPoints, int nbPoints) {
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
		return new Point2D.Double((minX + maxX) / 2, (minY + maxY) / 2);
	}
}
