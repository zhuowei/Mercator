package net.zhuoweizhang.mercator;

import java.io.*;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public final class ConvertTGA {

	private ConvertTGA(){};

	public static void tgaToPng(File input, File output) throws IOException {
		Bitmap inBmp = UnstitchTGA.readTGA(input);
		FileOutputStream fos = new FileOutputStream(output);
		inBmp.compress(Bitmap.CompressFormat.PNG, 100, fos);
		fos.close();
	}

	public static void pngToTga(File input, File output) throws IOException {
		Bitmap inBmp = BitmapFactory.decodeFile(input.getAbsolutePath());
		RestitchTGA.writeTGA(inBmp, output);
	}
}
