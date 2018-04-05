package org.free.garminimg.locus;

import org.free.garminimg.ImgFileBag;
import org.free.garminimg.ImgSubFile;
import org.free.garminimg.SubFileReader;

public class MapSubFileReader extends SubFileReader {

	@Override
	public ImgSubFile parse(String filename, String filetype, int fileSize,
			int blocSize, int offset, ImgFileBag imgFileBag) {
		return ImgSubFile.create(filename, filetype, fileSize, blocSize, offset, imgFileBag);
	}

}
