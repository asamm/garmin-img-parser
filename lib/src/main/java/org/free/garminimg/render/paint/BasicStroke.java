package org.free.garminimg.render.paint;

import org.free.garminimg.UtilsGarminImg;

import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.PathEffect;

public class BasicStroke extends Paint {

	// SIMPLE LINE
	
	public BasicStroke(float strokeWidth, int color) {
		this(strokeWidth);
		setColor(color);
	}
	
	public BasicStroke(float strokeWidth) {
		setAntiAlias(true);
		setStrokeWidth(UtilsGarminImg.getDpPixels(strokeWidth));
		setStyle(Style.STROKE);
	}
	
	// LINE WITH DASH PATH EFFECT
	
	public BasicStroke(float strokeWidth, float[] tinydash) {
		this(strokeWidth, Paint.Cap.BUTT, Paint.Join.MITER, 10.0f, tinydash, 0.0f);
	}
	
	public BasicStroke(float strokeWidth, float[] tinydash, int color) {
		this(strokeWidth, Paint.Cap.BUTT, Paint.Join.MITER, 10.0f, tinydash, 0.0f, color);
	}

	public BasicStroke(float strokeWidth, Paint.Cap cap, Paint.Join join,
			float strokeMiter, float[] tinydash, float dashOffset, int color) {
		this(strokeWidth, cap, join, strokeMiter, tinydash, dashOffset);
		setColor(color);
	}
	
	public BasicStroke(float strokeWidth, Paint.Cap cap, Paint.Join join,
			float strokeMiter, float[] tinydash, float dashOffset) {
		this(strokeWidth);
		
		// set other parameters
		setStrokeCap(cap);
		setStrokeJoin(join);
		setStrokeMiter(strokeMiter);
		PathEffect effect = null;
		if (tinydash != null) {
			effect = new DashPathEffect(tinydash, UtilsGarminImg.getDpPixels(dashOffset));
			setPathEffect(effect);
		}
	}
}
