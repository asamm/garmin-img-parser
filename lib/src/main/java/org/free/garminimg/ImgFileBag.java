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

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;

/**
 * One .img file.
 */
public class ImgFileBag {

	public static final int IMG_T = 0;
	public static final int IMG_L = 1;
	public static final int IMG_B = 2;
	public static final int IMG_R = 3;

	private File file;

	private String mapCode;

	private LblSubFile lbl;
	private NetSubFile net;
	private RgnSubFile rgn;
	private TreSubFile tre;

	private ArrayList<ImgSubFile> otherSubFiles = new ArrayList<ImgSubFile>(1);

	private String description;

	private ImgFileInputStream inputPrivate;

	private boolean inputLocked;

	private ImgFilesBag parent;

	private boolean initBoundariesDone;

	/**
	 * Keep a cache of used blocs. Simple implementation, there is room for
	 * improvement...
	 */
	private HashMap<Long, WeakReference<byte[]>> blocCache = new HashMap<Long, WeakReference<byte[]>>();

	private int northBoundary;

	private int southBoundary;

	private int eastBoundary;

	private int westBoundary;

	private final boolean xored;

	public ImgFileBag(File file, ImgFilesBag parent, boolean xored)
			throws IOException {
		this.file = file;
		this.parent = parent;
		this.xored = xored;
	}

	public ImgContext getImgContext() {

		return parent.getImgContext();
	}

	public String getDescription() {
		return description;
	}

	public synchronized byte[] getBloc(long pos, long blocSize)
			throws IOException {
		WeakReference<byte[]> reference = blocCache.get(pos);
		byte[] result;
		if (reference == null || (result = reference.get()) == null) {
			ImgFileInputStream input = getInput();
			input.seek(pos);
			result = new byte[(int) blocSize];
			input.readBloc(result);
			blocCache.put(pos, new WeakReference<byte[]>(result));
			releaseInput();
		}
		return result;
	}

	public long getFullSurface() throws IOException {
		initBoundaries();
		if (tre != null)
			return tre.getFullSurface();
		else
			return ((long) northBoundary - southBoundary)
					* ((long) eastBoundary - westBoundary);
	}

	public RgnSubFile getRgnFile() throws IOException {
		return rgn;
	}

	public TreSubFile getTreFile() throws IOException {
		return tre;
	}

	public LblSubFile getLblFile() throws IOException {
		return lbl;
	}

	public NetSubFile getNetFile() throws IOException {
		return net;
	}

	public void setLbl(LblSubFile lbl) {

		this.lbl = lbl;
	}

	public void setNet(NetSubFile net) {

		this.net = net;
	}

	public void setRgn(RgnSubFile rgn) {

		this.rgn = rgn;
	}

	public void setTre(TreSubFile tre) {

		this.tre = tre;
	}

	public boolean isComplete() {

		return tre != null && rgn != null && lbl != null;
	}

	public ArrayList<ImgSubFile> getOtherFiles() throws IOException {
		return otherSubFiles;
	}

	public void addSubFile(ImgSubFile sub) {
		if (otherSubFiles.contains(sub)) {
			otherSubFiles.add(sub);
		}
	}

	public boolean containsCoordinate(int longitude, int latitude)
			throws IOException {
		return tre != null && tre.matchesCoordinate(longitude, latitude);
	}

	public void readMap(int minLong, int maxLong, int minLat, int maxLat,
			int resolution, int objectKindFilter, BitSet objectTypeFilter,
			MapListener listener) throws IOException {
		if (containsCoordinates(minLong, maxLong, minLat, maxLat)) {
			listener.startMap(this);
			if (tre != null) {
				tre.readMap(minLong, maxLong, minLat, maxLat, resolution,
						objectKindFilter, objectTypeFilter, rgn, lbl, net,
						listener);
			}
		}
	}

	public File getFile() {
		return file;
	}

	public synchronized boolean close() throws IOException {
		if (!inputLocked && inputPrivate != null) {
			inputPrivate.close();
			inputPrivate = null;
			return true;
		}
		return false;
	}

	public int getNorthBoundary() throws IOException {
		initBoundaries();
		if (tre != null)
			return tre.getNorthBoundary();
		return northBoundary;
	}

	public int getSouthBoundary() throws IOException {
		initBoundaries();
		if (tre != null)
			return tre.getSouthBoundary();
		return southBoundary;
	}

	public int getEastBoundary() throws IOException {
		initBoundaries();
		if (tre != null)
			return tre.getEastBoundary();
		return eastBoundary;
	}

	public int getWestBoundary() throws IOException {
		initBoundaries();
		if (tre != null)
			return tre.getWestBoundary();
		return westBoundary;
	}

	public int getXorByte() throws IOException {
		int result = getInput().getXor();
		releaseInput();
		return result;
	}

	public String getMapCode() {

		return mapCode;
	}

	public void setMapCode(String mapCode) {

		this.mapCode = mapCode;
	}

	void buildFromSubFiles(LblSubFile lbl, NetSubFile net, RgnSubFile rgn,
			TreSubFile tre, String description) {
		this.description = description;
		this.lbl = lbl;
		this.net = net;
		this.rgn = rgn;
		this.tre = tre;
	}

	private synchronized void releaseInput() {
		inputLocked = false;
	}

	private synchronized ImgFileInputStream getInput() throws IOException {
		if (inputPrivate == null) {
			if (parent != null)
				parent.registerOpenFile(this);
			inputPrivate = new ImgFileInputStream(file, xored);
		}
		inputLocked = true;
		return inputPrivate;
	}

	private boolean containsCoordinates(int minLong, int maxLong, int minLat,
			int maxLat) throws IOException {
		initBoundaries();
		if (tre != null)
			return tre.matchesCoordinates(minLong, maxLong, minLat, maxLat);
		else
			return CoordUtils.matchesCoordinates(westBoundary, eastBoundary,
					southBoundary, northBoundary, minLong, maxLong, minLat,
					maxLat);
	}

	private void initBoundaries() throws IOException {
		if (!initBoundariesDone) {
			if (tre != null) {
				northBoundary = tre.getNorthBoundary();// mapDescription.getCoordN();
				southBoundary = tre.getSouthBoundary();// mapDescription.getCoordS();
				eastBoundary = tre.getEastBoundary();// mapDescription.getCoordE();
				westBoundary = tre.getWestBoundary();// mapDescription.getCoordW();
			} else
				throw new IllegalStateException("TRE not initiated");
			initBoundariesDone = true;
		}
	}
}
