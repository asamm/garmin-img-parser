package org.free.garminimg.render.painters;

import android.graphics.Path;

import org.free.garminimg.UtilsGarminImg;

public class UtilsRender {

	public static Path createPathArrows() {
		Path path = new Path();
		path.moveTo(0.0f, UtilsGarminImg.getDpPixels(5.0f));
		path.lineTo(UtilsGarminImg.getDpPixels(6.0f), 0.0f);
		path.lineTo(0.0f, -1 * UtilsGarminImg.getDpPixels(5.0f));
		return path;
	}
}
