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

import org.free.garminimg.ImgFileBag;
import org.free.garminimg.Label;
import org.free.garminimg.MapListener;
import org.free.garminimg.SubDivision;
import org.free.garminimg.utils.ImgConstants.DrawLabel;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Special map listener used to find an object according to its name.
 */
public class FindObjectByNameListener implements MapListener {
	
	private final List<FoundObject> founds = new ArrayList<FoundObject>();

	private Matcher matcher;

	/**
	 * Will use a regular expression.
	 */
	public FindObjectByNameListener(Pattern regExp) {
		matcher = regExp.matcher("");
	}

	public List<FoundObject> getFounds() {
		return founds;
	}

	public void addPoint(int type, int subType, int longitude, int latitude,
			Label label, boolean indexed) {
		if (ImgConstants.getPointType(type, subType).getDrawLabel() != DrawLabel.NEVER
				&& matchLabel(label)) {
			founds.add(new FoundPoint(type, subType, longitude, latitude,
					label, indexed));
		}
	}

	private boolean matchLabel(Label label) {
		if (label == null)
			return false;
		try {
			String name = label.getName();
			if (name != null && name.length() > 0) {
				if (matcher != null) {
					matcher.reset(name);
					return matcher.find();
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}

	public void addPoly(int type, int[] longitudes, int[] latitudes,
			int nbPoints, Label label, boolean line) {
		if (matchLabel(label)) {
			if (line) {
				if (ImgConstants.getLineType(type).getDrawLabel() != DrawLabel.NEVER) {
					founds.add(new FoundPoly(type, longitudes, latitudes,
							nbPoints, label, true));
				}
			} else {
				if (ImgConstants.getPolygonType(type).getDrawLabel() != DrawLabel.NEVER) {
					founds.add(new FoundPoly(type, longitudes, latitudes,
							nbPoints, label, false));
				}
			}
		}
	}

	public void startMap(ImgFileBag file) {
	}

	public void startSubDivision(SubDivision subDivision) {
	}

	public void finishPainting() {
	}
}
