package org.free.garminimg.render.painters;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;

import org.free.garminimg.utils.ImgConstants.PainterStep;

import timber.log.Timber;

public class PolyPainterSimple extends LinePolyPainter {

	// main paint object
	private Paint paintFill;

	// flag to draw border
	private boolean drawBorder;
	// main paint object
	private Paint paintBorder;
	
	public PolyPainterSimple(int baseColor, boolean border) {
		super();
		setBase(new Paint(), baseColor);
		this.drawBorder = border;
		if (drawBorder) {
			setBorder(new Paint(paintFill), Color.BLACK);
		}
	}
	
	public PolyPainterSimple(Paint paint, boolean border) {
		super();
		setBase(paint, paint.getColor());
		this.drawBorder = border;
		if (drawBorder) {
			setBorder(new Paint(paintFill), Color.BLACK);
		}
	}
	
	private void setBase(Paint paint, int color) {
		this.paintFill = paint;
		this.paintFill.setColor(color);
		this.paintFill.setStyle(Paint.Style.FILL);
	}
	
	private void setBorder(Paint paint, int color) {
		// check paint
		if (paint == null) {
			Timber.w("attempt to set empty paint for Poly border");
			return;
		}
		
		// set parameters
		this.paintBorder = paint;
		this.paintBorder.setColor(color);
//		this.paintBorder.setStrokeWidth(UtilsGarminImg.getDpPixels(2.0f));
		this.paintBorder.setStrokeWidth(0);
		this.paintBorder.setStyle(Paint.Style.STROKE);
	}

	@Override
	protected void paintPrivate(Canvas canvas, boolean overlay, int zoomLevel,
			int[] xPoints, int[] yPoints, int nbPoints, 
			PainterStep step, boolean antialiasing) {
		// get valid path
		Path p = preparePath(xPoints, yPoints, nbPoints, false);
//		Logger.d(TAG, "paint(" + canvas + ", " + xPoints + ", " + yPoints + ", " + nbPoints + ", " + 
//				step + ", " + antialiasing + "), drawBoder:" + drawBorder);
		
		// prepare paint and draw
		if (step == PainterStep.FIRST) {
			paintFill.setAntiAlias(antialiasing);

			// finally draw
			if (overlay && isBackground()) {
				if (paintBorder == null) {
					paintBorder = new Paint(paintFill);
					paintBorder.setStyle(Paint.Style.STROKE);
				}
				paintBorder.setAntiAlias(antialiasing);
				canvas.drawPath(p, paintBorder);
			} else {
				// draw border
				if (drawBorder) {
					paintBorder.setAntiAlias(antialiasing);
					canvas.drawPath(p, paintBorder);
				}

				// draw fill now
				canvas.drawPath(p, paintFill);
			}
		}
	}

//	protected void drawTextPrivate(Canvas canvas, int zoomLevel, 
//			int[] xPoints, int[] yPoints, int nbPoints, String text) {
//		// compute center of polygon
//		Point2D center = UtilsGarminImg.computeCenter(xPoints, yPoints, nbPoints);
//		
//		// draw text
//		Paint pFg = ImgConstants.pLabelsFg;
//		Align currentAlign = pFg.getTextAlign();
//		pFg.setTextAlign(Align.CENTER);
//		pFg.setColor(Color.BLACK);
//		canvas.drawText(text, 
//				(float) center.getX(), (float) center.getY(), pFg);
//		pFg.setTextAlign(currentAlign);
//	}
}
