package org.free.garminimg.render.painters;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;

import org.free.garminimg.UtilsGarminImg;
import org.free.garminimg.utils.ImgConstants.PainterStep;

public class LinePainterDouble extends LinePolyPainter {
	
	public static final float STROKE_BG = UtilsGarminImg.getDpPixels(2.0f);
	
	// main paint object
	private final Paint paintFill;
	// paint for background
	private final Paint paintBg;

	public LinePainterDouble(Paint paint, int colorFill, int colorBg) {
		super();
		
		// set base paint
		paint.setColor(colorFill);
		
		// prepare background paint
		Paint paintBg = new Paint(paint);
		paintBg.setColor(colorBg);
		paintBg.setStrokeWidth(paint.getStrokeWidth() + STROKE_BG);
		
		// set paints
		this.paintFill = paint;
		this.paintBg = paintBg;
	}

	public LinePainterDouble(Paint paintFill, Paint paintBg) {
		super();
		
		this.paintFill = paintFill;
		this.paintBg = paintBg;
	}
	
	@Override
	protected void paintPrivate(Canvas canvas, boolean overlay, int zoomLevel,
			int[] xPoints, int[] yPoints, int nbPoints, 
			PainterStep step, boolean antialiasing) {
		// get valid path
		Path p = preparePath(xPoints, yPoints, nbPoints, false);
		
		// finally draw
		if (step == PainterStep.FIRST) {
			// draw background
			paintBg.setAntiAlias(antialiasing);
			canvas.drawPath(p, paintBg);
		} else if (step == PainterStep.SECOND) {
			// draw fill
			paintFill.setAntiAlias(antialiasing);
			canvas.drawPath(p, paintFill);
		}
	}
	
	protected void drawTextPrivate(Canvas canvas, int zoomLevel, 
			int[] xPoints, int[] yPoints, int nbPoints, String text) {
		drawTextCentered(canvas, zoomLevel,
				xPoints, yPoints, nbPoints, text, Color.BLACK);
	}
}
