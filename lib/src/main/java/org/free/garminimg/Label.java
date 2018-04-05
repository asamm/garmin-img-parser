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
package org.free.garminimg;

import java.io.IOException;

import android.util.Log;

/**
 * Base class for a label.
 */
public abstract class Label {
	
	protected ImgFileBag file;

	protected final int labelOffset;

	protected String name = null;

	protected boolean initDone = false;

	public Label(ImgFileBag file, int labelOffset) {
		this.file = file;
		this.labelOffset = labelOffset;
	}

	protected synchronized void initIfNeeded() throws IOException {
		if (!initDone) {
			init();
			initDone = true;
		}
	}

	public String getName() throws IOException {
		initIfNeeded();
		return name;
	}

	public String getNameNoException() {
		try {
			return getName();
		} catch (IOException e) {
			e.printStackTrace();
			return "";
		}
	}

	protected abstract void init() throws IOException;

	public String toString() {
		try {
			return getName();
		} catch (IOException e) {
			String msg = e.getMessage();
			Log.e("Label-->", msg != null ? msg : "");
			return "ERROR";
		}
	}

	public int hashCode() {
		if (file != null)
			return labelOffset + 31 * file.getFile().hashCode();
		else
			return name.hashCode();
	}

	public boolean equals(Object o) {
		if (o == this)
			return true;
		Label obj = (Label) o;
		if (file != null && obj.file != null)
			return labelOffset == obj.labelOffset
					&& file.getFile().equals(obj.file.getFile());
		else
			return file == null && obj.file == null && name.equals(obj.name);
	}

}
