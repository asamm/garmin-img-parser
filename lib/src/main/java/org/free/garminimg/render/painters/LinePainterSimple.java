package org.free.garminimg.render.painters;

import org.free.garminimg.utils.ImgConstants.PainterStep;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;

public class LinePainterSimple extends LinePolyPainter {

	// main paint object
	private Paint paint;
	// defined color
	private int color;

	public LinePainterSimple(Paint paint) {
		this(paint, paint.getColor());
	}
	
	public LinePainterSimple(Paint paint, int color) {
		super();
		this.paint = paint;
		this.color = color;
	}

	@Override
	protected void paintPrivate(Canvas canvas, boolean overlay, int zoomLevel,
			int[] xPoints, int[] yPoints, int nbPoints, 
			PainterStep step, boolean antialiasing) {
		// get valid path
		Path p = preparePath(xPoints, yPoints, nbPoints, false);
		
		// prepare paint and draw
		if (step == PainterStep.FIRST) {
			paint.setAntiAlias(antialiasing);
			paint.setColor(color);
			canvas.drawPath(p, paint);
		}
	}
	
	@Override
	protected void drawTextPrivate(Canvas canvas, int zoomLevel, 
			int[] xPoints, int[] yPoints, int nbPoints, String text) {
		drawTextCentered(canvas, zoomLevel,
				xPoints, yPoints, nbPoints, text, paint.getColor());
	}
}
