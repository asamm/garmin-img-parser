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

/**
 * Kinds of objects available in a map and kinds of maps.
 */
public abstract class ObjectKind {
	
	public static final int POINT = 0x10;

	public static final int INDEXED_POINT = 0x20;

	public static final int POLYLINE = 0x40;

	public static final int POLYGON = 0x80;

	public static final int UNKNOWN1 = 0x01;

	public static final int UNKNOWN2 = 0x02;

	public static final int UNKNOWN3 = 0x04;

	public static final int UNKNOWN4 = 0x08;

	public static final int EXTENDED_POLYGON = 0x100;

	public static final int EXTENDED_POLYLINE = 0x200;

	public static final int EXTENDED_POINT = 0x400;

	public static final int ALL_MAPS = 0xF000;

	public static final int BASE_MAP = 0x1000;

	public static final int NORMAL_MAP = 0x2000;

	public static final int ALL = 0xFFFFF;

}
