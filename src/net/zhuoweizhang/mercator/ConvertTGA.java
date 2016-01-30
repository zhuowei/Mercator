package net.zhuoweizhang.mercator;

import java.io.*;
import android.os.Build;
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
		BitmapFactory.Options opts = new BitmapFactory.Options();
		//setBitmapOptionsPremultplied(opts, false);
		Bitmap inBmp = BitmapFactory.decodeFile(input.getAbsolutePath(), opts);
		RestitchTGA.writeTGA(inBmp, output);
	}
/*	public static boolean setBitmapOptionsPremultiplied(BitmapFactory.Options opts, boolean value) {
		try {
			Field f = opts.getClass().getField("inPremultiplied");
			f.setValue(opts, value);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
*/
}
