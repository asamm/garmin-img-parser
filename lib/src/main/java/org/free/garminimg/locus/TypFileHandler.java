package org.free.garminimg.locus;

import org.free.garminimg.utils.EndianDataInputStream;
import org.free.garminimg.utils.Utils;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Calendar;

import timber.log.Timber;

public class TypFileHandler {

	private static final String TAG = TypFileHandler.class.getSimpleName();
	
	// path to existing TYP file
	private File file;
	// raw file data
	private byte[] data;
	
	public TypFileHandler(File file) throws IOException {
		this.file = file;
		this.data = Utils.loadBytes(file);
	}
	
	public void parse() {
		EndianDataInputStream eis = null;
		try {
			FileInputStream fis = new FileInputStream(file);
			BufferedInputStream bis = new BufferedInputStream(fis);
			eis = new EndianDataInputStream(bis);
			parsePrivate(eis);
		} catch (Exception e) {
			Timber.e(e, "parse()");
		} finally {
			eis.closeQuietly();
		}
	}
	
	@SuppressWarnings("unused")
	private void parsePrivate(EndianDataInputStream eis) throws IOException {
		int headerLength = eis.readByteLE();
		eis.readByteLE(); // 0
		
		// read signature
		byte[] bSig = new byte[10];
		eis.readByteLEnum(bSig);
		String signature = new String(bSig);
        Timber.d("header:" + headerLength + ", sig:" + bSig);
		
		eis.readByteLE(); // 1
		eis.readByteLE(); // 0

		// read time
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.YEAR, eis.readShortLE());
		cal.set(Calendar.MONTH, eis.readByteLE());
		cal.set(Calendar.DAY_OF_MONTH, eis.readByteLE());
		cal.set(Calendar.HOUR_OF_DAY, eis.readByteLE());
		cal.set(Calendar.MINUTE, eis.readByteLE());
		cal.set(Calendar.SECOND, eis.readByteLE());
        Timber.d("cal:" + cal.toString());
		
		int codePage = eis.readShortLE();
		
		int pointsOffset = eis.readIntLE();
		int pointsLength = eis.readIntLE();
		int lineOffset = eis.readIntLE();
		int lineLength = eis.readIntLE();
		int polyOffset = eis.readIntLE();
		int polyLength = eis.readIntLE();
		
		int familyId = eis.readShortLE();
		int productCode = eis.readShortLE();
		
		int pointsOffset2 = eis.readIntLE();
		int pointsBytesPerBlock2 = eis.readShortLE();
		int pontsLength2 = eis.readIntLE();
		
		int lineOffset2 = eis.readIntLE();
		int lineBytesPerBlock2 = eis.readShortLE();	
		int lineLength2 = eis.readIntLE();
		
		int polyOffset2 = eis.readIntLE();
		int polyBytesPerBlock2 = eis.readByteLE();
		int polyLinked2 = eis.readByteLE();
		int polyLength2 = eis.readIntLE();
		
		int polyOffset3 = eis.readIntLE();
		int polyBytesPerBlock3 = eis.readShortLE();	
		int polyLength3 = eis.readIntLE();
		
		// try to parse points first
		parsePoints(pointsOffset, pointsLength);
	}
	
	// PROBLEM WITH READING DATA - required this paid text - http://www.scribd.com/doc/91751692/TYP-Format
	
	private void parsePoints(int offset, int length) {
		EndianDataInputStream edis = getInputStream(offset, length);
		try {
			int flag = edis.readByteLE();
			int bmpLength = edis.readByteLE();
			int bmpWidth = edis.readByteLE();
			int numOfColors = edis.readByteLE();
			int colorMode = edis.readByteLE();
			byte[] colours = new byte[3 * numOfColors];
			edis.readByteLEnum(colours);
			byte[] img = new byte[(bmpLength * bmpWidth * numOfColors) / 8];
			edis.readByteLEnum(img);
			int lengthOfText = edis.readByteLE();
			int languageString = edis.readByteLE();
			
		} catch (Exception e) {
            Timber.e(e, "parsePoints(" + offset + ", " + length + ")");
		} finally {
			edis.closeQuietly();
		}
	}

	private EndianDataInputStream getInputStream(int offset, int length) {
		byte[] newData = Utils.copyOfRange(data, offset, length);
		ByteArrayInputStream bais = new ByteArrayInputStream(newData);
		EndianDataInputStream edis = new EndianDataInputStream(bais);
		return edis;
	}
}
