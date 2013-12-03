package net.zhuoweizhang.mercator;

import android.graphics.*;
import java.io.*;
import java.nio.*;
import java.nio.charset.*;
import java.util.*;

public final class UnstitchLegacy {

	private UnstitchLegacy(){}

	public static class LegacyCoord {
		public int x, y;
		public String name;
		public LegacyCoord(int x, int y, String name) {
			this.x = x;
			this.y = y;
			this.name = name;
		}
	}

	public static List<LegacyCoord> loadLegacyCoord(InputStream is) throws IOException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		String curLine;
		List<LegacyCoord> retval = new ArrayList<LegacyCoord>();
		while ((curLine = reader.readLine()) != null) {
			curLine = curLine.trim();
			if (curLine.length() < 1 || curLine.charAt(0) == '#') continue;
			String[] parts = curLine.split(" - ");
			if (parts.length != 2) continue;
			String[] frontCoords = parts[0].split(",");
			LegacyCoord coord = new LegacyCoord(Integer.parseInt(frontCoords[0]), Integer.parseInt(frontCoords[1]), parts[1]);
			retval.add(coord);
		}
		reader.close();
		return retval;
	}

	public static void unstitchLegacy(File inputFile, List<LegacyCoord> map, File outputFolder)
		throws IOException {
		outputFolder.mkdirs();
		Bitmap bmp = BitmapFactory.decodeFile(inputFile.getAbsolutePath());
		unstitchLegacy(bmp, map, outputFolder);
	}

	public static void unstitchLegacy(Bitmap inputBitmap, List<LegacyCoord> map, File outputFolder)
		throws IOException {
		for (LegacyCoord l: map) {
			unstitchOneIcon(inputBitmap, l, outputFolder);
		}
	}

	private static void unstitchOneIcon(Bitmap inputBitmap, LegacyCoord coord, File outputFolder)
		throws IOException {
		int sx = coord.x * (inputBitmap.getWidth() / 16);
		int sy = coord.y * (inputBitmap.getHeight() / 16);
		int width = inputBitmap.getWidth() / 16;
		int height = inputBitmap.getHeight() / 16;
		Bitmap out = Bitmap.createBitmap(inputBitmap, sx, sy, width, height);
		File output = new File(outputFolder, coord.name + ".png");
		FileOutputStream fos = new FileOutputStream(output);
		out.compress(Bitmap.CompressFormat.PNG, 100, fos);
		fos.close();
	}
}
