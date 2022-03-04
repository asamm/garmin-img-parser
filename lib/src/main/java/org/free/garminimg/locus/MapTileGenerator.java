package org.free.garminimg.locus;

import android.graphics.Canvas;
import android.graphics.Color;

import org.free.garminimg.ImgFilesBag;
import org.free.garminimg.ObjectKind;
import org.free.garminimg.UtilsGarminImg;
import org.free.garminimg.utils.ClippingMapListener;
import org.free.garminimg.utils.CoordinateConverterListener;
import org.free.garminimg.utils.MapConfig;
import org.free.garminimg.utils.MapDrawer;
import org.free.garminimg.utils.MapTransformer;
import org.free.garminimg.utils.Rectangle;
import org.free.garminimg.utils.StatsListener;

import java.io.IOException;

public class MapTileGenerator<T> {

//	private static final String TAG = MapTileGenerator.class.getSimpleName();
	
	public void paintMap(Canvas c, int zoomLevel, MapConfig mapConfig, 
			MapTransformer<T> workTransformer, ImgFilesBag map) throws IOException {
//		final long milliStart = System.currentTimeMillis();
		Rectangle bbox = workTransformer.getGarminBoundingBox();
		final int minLon = bbox.x;
		final int maxLon = bbox.x + bbox.width;
		final int minLat = bbox.y;
		final int maxLat = bbox.y + bbox.height;
		int resolution = getResolution(workTransformer);
//		resolution = -1;

		// prepare drawer
		StatsListener drawer = new StatsListener(new MapDrawer(mapConfig, c,
				zoomLevel, UtilsGarminImg.getBaseTextSize(),
				Color.BLACK, Color.argb(255, 255, 255, 180), 1.0f));

		// start rendering
		int border = 5;
		ClippingMapListener clippingMapListener = new ClippingMapListener(
				-border, workTransformer.getWidth() + border, 
				-border, workTransformer.getHeight() + border, drawer);
		CoordinateConverterListener<T> cooConvertListener = 
				new CoordinateConverterListener<T>(workTransformer, clippingMapListener);
//		DiscoverTypesMapListener typesMapListener = new DiscoverTypesMapListener();
//		DualTransformedMapListener dualMapListener = new DualTransformedMapListener(
//				clippingMapListener, typesMapListener);
		
		map.readMapForDrawing(minLon, maxLon, minLat, maxLat, resolution,
				ObjectKind.ALL, cooConvertListener);
//		map.readMap(minLon, maxLon, minLat, maxLat, resolution,
//				ObjectKind.ALL, null, cooConvertListener);
//		cooConvertListener.finishPainting();

		// print debug result
//		if (Const.STATE_DEBUG_LOGS) {
//			long milliEnd = System.currentTimeMillis();
//			Logger.d(TAG, "Time to draw, size: " + workTransformer.getWidth() + "x" + workTransformer.getHeight() +
//					", time: " + (milliEnd - milliStart) + ", drawn: " + drawer.toString());
//		}
	}

	private int getResolution(MapTransformer<T> transformer) {
		Rectangle bbox = transformer.getGarminBoundingBox();
		int minLon = bbox.x;
		int maxLon = bbox.x + bbox.width;
		return getResolution(minLon, maxLon, transformer.getWidth());
	}

	private static int getResolution(int minLon, int maxLon, int width) {
		return (maxLon - minLon) / width;
	}
}
