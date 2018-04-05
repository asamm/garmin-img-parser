/*
 * JGarminImgParser - A java library to parse .IMG Garmin map files.
 *
 * Copyright (C) 2007 Patrick Valsecchi
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

public class MapConfig implements Cloneable {

	public enum Quality {
		TEMP, // no label, no aliasing
		DRAFT, // no aliasing
		FINAL // all the fancy stuff
	}

	// quality of rendered items
	private Quality quality = Quality.FINAL;

	// display labels for points
	private boolean showPointLabel = true;
	// display labels for lines
	private boolean showLineLabel = true;
	// display labels for polygons
	private boolean showPolygonLabel = true;

	private int poiThreshold = 0x15;
	// level of details
	private int detailLevel;
	// using of anti-aliasing
	private boolean wantAntialiasing = true;

	// flag if layer is overlay
	private boolean isOverlay = false;

	// LABELS

	public Quality getQuality() {
		return quality;
	}

	public void setQuality(Quality quality) {
		this.quality = quality;
	}

	public boolean isShowPointLabel() {
		return showPointLabel;
	}

	public void setShowPointLabel(boolean showPointLabel) {
		this.showPointLabel = showPointLabel;
	}

	public boolean isShowLineLabel() {
		return showLineLabel;
	}

	public void setShowLineLabel(boolean showLineLabel) {
		this.showLineLabel = showLineLabel;
	}

	public boolean isShowPolygonLabel() {
		return showPolygonLabel;
	}

	public void setShowPolygonLabel(boolean showPolygonLabel) {
		this.showPolygonLabel = showPolygonLabel;
	}

	public int getPoiThreshold() {
		return poiThreshold;
	}

	public void setPoiThreshold(int poiThreshold) {
		this.poiThreshold = poiThreshold;
	}

	public int getDetailLevel() {
		return detailLevel;
	}

	public void setDetailLevel(int detailLevel) {
		this.detailLevel = detailLevel;
	}

	public boolean isAntialiasingEnabled() {
		return wantAntialiasing;
	}

	public void setAntialiasingEnabled(boolean bool) {
		this.wantAntialiasing = bool;
	}

	public boolean isOverlay() {
		return isOverlay;
	}

	public void setOverlay(boolean overlay) {
		this.isOverlay = overlay;
	}

	@Override
	protected MapConfig clone() {
		try {
			return (MapConfig) super.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
			return this;
		}
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof MapConfig) {
			MapConfig other = (MapConfig) obj;
			return quality == other.quality
					&& showLineLabel == other.showLineLabel
					&& showPolygonLabel == other.showPolygonLabel
					&& showPointLabel == other.showPointLabel
					&& poiThreshold == other.poiThreshold
					&& detailLevel == other.detailLevel
					&& wantAntialiasing == other.wantAntialiasing
					&& isOverlay == other.isOverlay;
		}
		return false;
	}
}
