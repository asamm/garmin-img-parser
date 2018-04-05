package org.free.garminimg;

public abstract class SubFileReader {

	public abstract ImgSubFile parse(String filenam, String filetype,
			int fileSize, int blocSize, int offset, ImgFileBag imgFileBag);

}
