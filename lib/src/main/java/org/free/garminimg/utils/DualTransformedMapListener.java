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

import org.free.garminimg.ImgFileBag;
import org.free.garminimg.Label;
import org.free.garminimg.SubDivision;

/**
 * A TransformedMapListener that forwards to two other TransformedMapListener.
 */
public class DualTransformedMapListener implements TransformedMapListener {
	
	private TransformedMapListener listener1;

	private TransformedMapListener listener2;

	public DualTransformedMapListener(TransformedMapListener listener1,
			TransformedMapListener listener2) {
		this.listener1 = listener1;
		this.listener2 = listener2;
	}

	public void addPoint(int type, int subType, int x, int y, Label label,
			boolean indexed) {
		listener1.addPoint(type, subType, x, y, label, indexed);
		listener2.addPoint(type, subType, x, y, label, indexed);
	}

	public void addPoly(int type, int[] xPoints, int[] yPoints, int nbPoints,
			Label label, boolean line) {
		listener1.addPoly(type, xPoints, yPoints, nbPoints, label, line);
		listener2.addPoly(type, xPoints, yPoints, nbPoints, label, line);
	}

	public void startMap(ImgFileBag file) {
		listener1.startMap(file);
		listener2.startMap(file);
	}

	public void startSubDivision(SubDivision subDivision) {
		listener1.startSubDivision(subDivision);
		listener2.startSubDivision(subDivision);
	}

	public void finishPainting() {
		listener1.finishPainting();
		listener2.finishPainting();
	}
}
