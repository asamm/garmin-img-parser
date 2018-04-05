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

import java.io.EOFException;
import java.io.IOException;

/**
 * One sub-file within a .img file.
 */
public abstract class ImgSubFile {

	protected int initialOffset;

	protected int headerLength;

	protected ImgFileBag fileBag;

	private String filename;

	private String filetype;

	private int fileSize;

	private long blocSize;

	private int firstBloc;

	public ImgFileBag getImgFileBag() {

		return fileBag;
	}

	public static ImgSubFile create(String filename, String filetype,
			int fileSize, int blocSize, int initialOffset, ImgFileBag fileBag) {
		if (filetype.equalsIgnoreCase("LBL"))
			return new LblSubFile(filename, filetype, fileSize, blocSize,
					initialOffset, fileBag);
		else if (filetype.equalsIgnoreCase("TRE"))
			return new TreSubFile(filename, filetype, fileSize, blocSize,
					initialOffset, fileBag);
		else if (filetype.equalsIgnoreCase("RGN"))
			return new RgnSubFile(filename, filetype, fileSize, blocSize,
					initialOffset, fileBag);
		else if (filetype.equalsIgnoreCase("NET"))
			return new NetSubFile(filename, filetype, fileSize, blocSize,
					initialOffset, fileBag);
		else
			return new UnknownSubFile(filename, filetype, fileSize, blocSize,
					initialOffset, fileBag);
	}

	public ImgSubFile(String filename, String filetype, int fileSize,
			int blocSize, int initialOffset, ImgFileBag fileBag) {
		this.filename = filename;
		this.filetype = filetype;
		this.fileSize = fileSize;
		this.blocSize = blocSize;
		this.fileBag = fileBag;
		this.initialOffset = initialOffset;
	}

	public void setBlocks(int firstBloc, int numBlocs) {

		this.firstBloc = firstBloc;
	}

	public abstract void init() throws IOException;

	public abstract void fullInit() throws IOException;

	protected void superInit(FileContext context) throws IOException {
		seek(initialOffset + 0, context);
		headerLength = readUInt16(context);

		seek(initialOffset + 0xD, context);
		int locked = readByte(context);
		if (locked != 0)
			throw new IOException("Locked map, not supported!");
	}

	public String getFilename() {
		return filename;
	}

	public int getFileSize() {
		return fileSize;
	}

	public String getFiletype() {
		return filetype;
	}

	protected int getHeaderLength() {
		return headerLength;
	}

	public static class FileContext {
		private byte[] curBlocContent = null;

		private int curPosInBloc = -1;

		private long curPos = -1;

		private int curBloc = -1;
	}

	/**
	 * @param relative
	 *            The position relative to the start of the sub-file
	 * @return The position relative to the start of the IMG file.
	 * @throws EOFException
	 */
	public long getAbsolutePosition(long relative) throws EOFException {
		int bloc = (int) ((relative) / blocSize);
		long blocOffset = (relative) % blocSize;
		return (firstBloc + bloc) * blocSize + blocOffset;
	}

	public void seek(long pos, FileContext context) throws IOException {
		context.curPos = pos;
		int newBloc = (int) (context.curPos / blocSize);
		if (context.curBloc != newBloc) {
			context.curBloc = newBloc;
			context.curBlocContent = fileBag.getBloc((firstBloc + newBloc)
					* blocSize, blocSize);
		}
		context.curPosInBloc = (int) (context.curPos % blocSize);
	}

	public long getNextReadPos(FileContext context) {
		return context.curPos;
	}

	public int readByte(FileContext context) throws IOException {
		int result = context.curBlocContent[context.curPosInBloc++] & 0xFF;
		context.curPos++;
		if (context.curPosInBloc >= blocSize) {
			seek(context.curPos, context);
		}
		return result;
	}

	public String readString(int len, FileContext context) throws IOException {
		StringBuffer result = new StringBuffer(len);
		for (int cpt = 0; cpt < len; ++cpt)
			result.append(readByte(context));
		return result.toString();
	}

	public byte[] readBytes(int len, FileContext context) throws IOException {
		byte[] res = new byte[len];
		for (int i = 0; i < len; i++) {
			res[i] = (byte) (context.curBlocContent[context.curPosInBloc++] & 0xFF);
			context.curPos++;
			if (context.curPosInBloc >= blocSize) {
				seek(context.curPos, context);
			}
		}
		return res;
	}

	public int readUInt16(FileContext context) throws IOException {
		return readByte(context) | readByte(context) << 8;
	}

	public int readUInt24(FileContext context) throws IOException {
		return readByte(context) | readByte(context) << 8
				| readByte(context) << 16;
	}

	public long readUInt32(FileContext context) throws IOException {
		return readByte(context) | readByte(context) << 8
				| readByte(context) << 16 | readByte(context) << 24;
	}

	public int readInt16(FileContext context) throws IOException {
		int result = readUInt16(context);
		if (result > 0x7FFF) {
			result = (result & 0x7FFF) - 0x8000;
		}
		return result;
	}

	public int readInt24(FileContext context) throws IOException {
		int result = readUInt24(context);
		if (result > 0x7FFFFF) {
			result = (result & 0x7FFFFF) - 0x800000;
		}
		return result;
	}

	public int getFirstBloc() {

		return firstBloc;
	}

}
