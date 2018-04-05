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
import org.free.garminimg.SubDivision;

import java.util.HashSet;

/**
 * Usually, streets are split between more than one polyline. To avoid having a
 * street name repeated more than once, you can use this filter.
 */
public class PolyLabelReducerListener implements TransformedMapListener {
	
	private final TransformedMapListener next;

	private final HashSet<Label> knowns = new HashSet<Label>();

	public PolyLabelReducerListener(TransformedMapListener next) {
		this.next = next;
	}

	public void addPoint(int type, int subType, int x, int y, Label label,
			boolean indexed) {
		next.addPoint(type, subType, x, y, label, indexed);
	}

	public void addPoly(int type, int[] xPoints, int[] yPoints, int nbPoints,
			Label label, boolean line) {
		if (label != null) {
			if (knowns.contains(label)) {
				label = null;
			} else {
				knowns.add(label);
			}
		}
		next.addPoly(type, xPoints, yPoints, nbPoints, label, line);
	}

	public void startMap(ImgFileBag file) {
		next.startMap(file);
	}

	public void startSubDivision(SubDivision subDivision) {
		next.startSubDivision(subDivision);
	}

	public void finishPainting() {
		next.finishPainting();
	}

	public void clear() {
		knowns.clear();
	}
}
