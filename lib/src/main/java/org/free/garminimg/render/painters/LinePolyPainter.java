package org.free.garminimg.render.painters;

import org.free.garminimg.UtilsGarminImg;
import org.free.garminimg.utils.ImgConstants;
import org.free.garminimg.utils.ImgConstants.PainterStep;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;

public abstract class LinePolyPainter {

	// existing path object
	private final Path path;
	// zoom level limits
	private final int zoomMinItem;
	private final int zoomMaxItem;
	
	// flag if painter draw background area
	private boolean isBackground;
	
	public LinePolyPainter() {
		this.path = new Path();
		zoomMinItem = ImgConstants.ZOOM_MIN;
		zoomMaxItem = ImgConstants.ZOOM_MAX;
		this.isBackground = false;
	}
	
	public boolean isBackground() {
		return isBackground;
	}
	
	public void setBackgroundLayer(boolean background) {
		this.isBackground = background;
	}
	
	Path preparePath(int[] xPoints, int[] yPoints, int nbPoints,
			boolean reverseOrder) {
		preparePath(path, xPoints, yPoints, nbPoints, reverseOrder);
		return path;
	}
	
	public void paint(Canvas canvas, boolean overlay, int zoomLevel,
			int[] xPoints, int[] yPoints, int nbPoints, 
			PainterStep step, boolean antialiasing) {
		if (zoomLevel >= zoomMinItem && zoomLevel <= zoomMaxItem) {
			paintPrivate(canvas, overlay, zoomLevel, 
					xPoints, yPoints, nbPoints, step, antialiasing);
		}
	}
	
	protected abstract void paintPrivate(Canvas canvas, boolean overlay, int zoomLevel,
			int[] xPoints, int[] yPoints, int nbPoints, 
			PainterStep step, boolean antialiasing);
	
	public void drawText(Canvas canvas, int zoomLevel, 
			int[] xPoints, int[] yPoints, int nbPoints, String text) {
		drawTextPrivate(canvas, zoomLevel,
				xPoints, yPoints, nbPoints, text);
	}
	
	protected void drawTextPrivate(Canvas canvas, int zoomLevel, 
			int[] xPoints, int[] yPoints, int nbPoints, String text) {
		// handle in childs
	}
	
	protected void drawTextCentered(Canvas canvas, int zoomLevel, 
			int[] xPoints, int[] yPoints, int nbPoints, String text, int color) {
		// get valid data
		boolean reverse = xPoints[0] > xPoints[nbPoints - 1];
		Path p = preparePath(xPoints, yPoints, nbPoints, reverse);
		Paint pFg = ImgConstants.pLabelsFg;
		Paint pBg = ImgConstants.pLabelsBg;
		
		// compute and check horizontal offset
		float hOffset = computeHOffset(pFg.measureText(text), xPoints, yPoints, nbPoints);
		if (hOffset < 0) {
			return;
		}
		
		float vOffset = (pFg.descent() - pFg.ascent()) / 2.0f;
		vOffset = vOffset - pFg.descent();
		
		// draw background
		canvas.drawTextOnPath(text, p, hOffset, vOffset, pBg);
		
		// draw foreground
		pFg.setColor(color);
		canvas.drawTextOnPath(text, p, hOffset, vOffset, pFg);
	}
	
	private float computeHOffset(float textLength, int[] xPoints, int[] yPoints, int nbPoints) {
		// skip complicated track
		if (nbPoints > 5) {
			return UtilsGarminImg.getDpPixels(50.0f);
		}
		
		// sum length
		float dist = 0.0f;
		for (int i = 1; i < nbPoints - 1; i++) {
			dist += Math.sqrt((xPoints[i] - xPoints[i - 1]) * (xPoints[i] - xPoints[i - 1]) +
					(yPoints[i] - yPoints[i - 1]) * (yPoints[i] - yPoints[i - 1]));
		}
		
		// handle result
		if (textLength > dist) {
			return -1;
		} else if (dist > 3 * textLength) {
			return UtilsGarminImg.getDpPixels(50.0f);
		} else {
			return (dist - textLength) / 2.0f; 
		}
	}
	
	private void preparePath(Path path, 
			int[] xPoints, int[] yPoints, int nbPoints, boolean reverse) {
		path.reset();
		
		// fill data
		if (!reverse) {
			path.moveTo(xPoints[0], yPoints[0]);
			for (int i = 1; i < nbPoints; i++) {
				path.lineTo(xPoints[i], yPoints[i]);
			}
		} else {
			path.moveTo(xPoints[nbPoints - 1], yPoints[nbPoints - 1]);
			for (int i = nbPoints - 2; i >= 0; i--) {
				path.lineTo(xPoints[i], yPoints[i]);
			}
		}
	}
}
