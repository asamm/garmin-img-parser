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

import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ImgFileBagExtractor {
	private final File file;

	private static final int FAT_BLOC_SIZE = 512;

	private ImgFileInputStream inputPrivate;

	private final ImgFilesBag parent;

	private final SubFileReader subFileReader;

	ImgFileBagExtractor(File file, ImgFilesBag parent,
			SubFileReader subFileReader) throws IOException {
		this.file = file;
		this.parent = parent;
		this.subFileReader = subFileReader;
	}

	List<ImgFileBag> createImgFileBags(boolean fullInit) {

		List<ImgFileBag> result = new ArrayList<ImgFileBag>();
		if (file == null || parent == null) {
			throw new IllegalStateException("Img Extractor not initiated");
		} else {
			try {
				extract(result, fullInit);
			} catch (IOException e) {
				String m = e.getMessage();
				Log.e("-img extractor->", m != null ? m : "");
			}
		}
		return result;
	}

	private void extract(List<ImgFileBag> result, boolean fullInit)
			throws IOException {
		ImgFileInputStream input = openInput();
		input.seek(0x49);
		String description = input.readString(20);
		input.seek(0x61);
		int e1 = input.readByte();
		int e2 = input.readByte();
		int blocSize = 1 << (e1 + e2);
		boolean nt = false;
		input.seek(0x1FE);
		int endOfPartitions = input.readUInt16();
		if (endOfPartitions != 0xAA55) {
			throw new IOException("Bad end of partition table: 0x"
					+ Integer.toHexString(endOfPartitions));
		}

		// jump over some un-interesting blocs (usually only one, but can be
		// more)
		int interestingFatStart = 0x200;
		while (true) {
			input.seek(interestingFatStart);
			int firstByte = input.readByte();
			if (firstByte == 1) {
				break;
			} else {
				interestingFatStart += FAT_BLOC_SIZE;
			}
		}

		// the first block with startByte==1 contains the length and other
		// unknown stuff
		input.seek(interestingFatStart + 0xc);
		int fatLength = input.readInt32() - interestingFatStart;
		if (fatLength < 0) {
			throw new IOException("Invalid FAT length: " + fatLength);
		}
		interestingFatStart += FAT_BLOC_SIZE;

		ImgFileBag imgtemp = null;// = new ImgFileBag(file, parent);

		// the rest of the blocs contains the information to find the other
		// files
		input.seek(interestingFatStart);
		final int nbBlocks = fatLength / FAT_BLOC_SIZE;

		ArrayList<FatBloc> fatblocs = new ArrayList<FatBloc>();

		for (int fatBlock = 0; fatBlock < nbBlocks; fatBlock++) {
			input.seek(interestingFatStart + fatBlock * FAT_BLOC_SIZE);
			int firstByte = input.readByte();
			if (firstByte == 0x1) {
				String filename = input.readString(8);
				String filetype = input.readString(3);
				int fileSize = input.readInt32();
				int partNumber = input.readUInt16();

				if (partNumber == 0) {
					if (!nt && "gmp".equalsIgnoreCase(filetype)) {
						nt = true;
					}
					FatBloc fb = new FatBloc(filename, filetype, fileSize,
							interestingFatStart + fatBlock * FAT_BLOC_SIZE);
					fatblocs.add(fb);
				}
			}
		}

		if (nt) {
			for (FatBloc fb : fatblocs) {
				if ("gmp".equalsIgnoreCase(fb.filetype)) {

					imgtemp = new ImgFileBag(file, parent, true);
					imgtemp.setMapCode(fb.filenam);

					LblSubFile lbl = null;
					NetSubFile net = null;
					RgnSubFile rgn = null;
					TreSubFile tre = null;

					int firstBloc = 0;
					int numBlocs = 0;

					input.seek(fb.offset + 0x20);
					firstBloc = input.readUInt16();

					long gmpInit = firstBloc * blocSize;

					input.seek(gmpInit + 0x19);
					long treOf = input.readInt32();
					long rgnOf = input.readInt32();
					long lblOf = input.readInt32();
					long netOf = input.readInt32();
					// long nod = input.readInt32();
					// long dem = input.readInt32();
					// long mar = input.readInt32();

					if (treOf > 0) {
						tre = (TreSubFile) ImgSubFile.create(fb.filenam, "TRE",
								fb.fileSize, blocSize, (int) treOf, imgtemp);
						tre.setBlocks(firstBloc, numBlocs);// .addBloc(blocs.get(i));
						tre.init();
						if (fullInit)
							tre.fullInit();
					}
					if (fullInit) {// los demas no interesan
						if (rgnOf > 0) {
							rgn = (RgnSubFile) ImgSubFile.create(fb.filenam,
									"RGN", fb.fileSize, blocSize, (int) rgnOf,
									imgtemp);
							rgn.setBlocks(firstBloc, numBlocs);
							rgn.init();
							rgn.fullInit();
						}
						if (lblOf > 0) {
							lbl = (LblSubFile) ImgSubFile.create(fb.filenam,
									"LBL", fb.fileSize, blocSize, (int) lblOf,
									imgtemp);
							lbl.setBlocks(firstBloc, numBlocs);
							lbl.init();
							lbl.fullInit();
						}
						if (netOf > 0) {
							net = (NetSubFile) ImgSubFile.create(fb.filenam,
									"NET", fb.fileSize, blocSize, (int) netOf,
									imgtemp);
							net.setBlocks(firstBloc, numBlocs);
							net.init();
							net.fullInit();
						}
					}
					imgtemp.buildFromSubFiles(lbl, net, rgn, tre, description);
					if (result != null)
						result.add(imgtemp);
				} else if (fullInit && subFileReader != null) {// otros
																// ficheros,
																// punto de
																// extension
					// TODO
					if (imgtemp == null) {
						imgtemp = new ImgFileBag(file, parent, true);
						imgtemp.setMapCode(fb.filenam);
					}
					ImgSubFile subFile = subFileReader.parse(fb.filenam,
							fb.filetype, fb.fileSize, blocSize, 0, imgtemp);
					if (subFile != null) {
						input.seek(fb.offset + 0x20);
						int firstBloc = input.readUInt16();
						subFile.setBlocks(firstBloc, 1);
						try {
							subFile.init();
							subFile.fullInit();
							if (result != null) {// lo añadimos a los ya
													// existentes; TODO si es el
													// primer fichero, se pierde
								for (ImgFileBag img : result)
									img.addSubFile(subFile);
							}
						} catch (Exception e) {
							e.printStackTrace();
							Log.e("ImgFileBagExtractor-->",
									"fallo leyendo subfile" + file);
						}
					}
				}
			}
		} else {
			ImgSubFile subFile;

			HashMap<String, ImgFileBag> files = new HashMap<String, ImgFileBag>();
			for (FatBloc fb : fatblocs) {
				ImgFileBag fileBag = files.get(fb.filenam);
				if (fileBag == null) {
					fileBag = new ImgFileBag(file, parent, true);
					fileBag.setMapCode(fb.filenam);
					files.put(fb.filenam, fileBag);
				}
				subFile = ImgSubFile.create(fb.filenam, fb.filetype,
						fb.fileSize, blocSize, 0, fileBag);
				if ("RGN".equals(fb.filetype))
					fileBag.setRgn((RgnSubFile) subFile);
				else if ("TRE".equals(fb.filetype))
					fileBag.setTre((TreSubFile) subFile);
				else if ("LBL".equals(fb.filetype))
					fileBag.setLbl((LblSubFile) subFile);
				else if ("NET".equals(fb.filetype))
					fileBag.setNet((NetSubFile) subFile);
				else if (subFileReader != null) {
					subFile = subFileReader.parse(fb.filenam, fb.filetype,
							fb.fileSize, blocSize, 0, fileBag);
					if (subFile != null) {
						fileBag.addSubFile(subFile);
					} else
						continue;
				}
				int firstBloc = 0;
				int numBlocs = 0;
				input.seek(fb.offset + 0x20);
				firstBloc = input.readUInt16();
				subFile.setBlocks(firstBloc, numBlocs);
				subFile.init();
				if (fullInit) {
					subFile.fullInit();
				}
			}

			for (ImgFileBag f : files.values()) {
				if (f.isComplete()) {
					result.add(f);
				}
			}
		}
		closeInput();
	}

	private synchronized ImgFileInputStream openInput() throws IOException {
		inputPrivate = new ImgFileInputStream(file, true);
		return inputPrivate;
	}

	private synchronized boolean closeInput() throws IOException {
		if (inputPrivate != null) {
			inputPrivate.close();
			inputPrivate = null;
			return true;
		}
		return false;
	}

	public class FatBloc {

		String filenam;// e=input.readString(8);
		String filetype;// =input.readString(3);

		int fileSize;
		long offset;

		public FatBloc(String filenam, String filetype, int fileSize,
				long offset) {

			this.filenam = filenam;
			this.filetype = filetype;
			this.fileSize = fileSize;
			this.offset = offset;
		}
	}

}
