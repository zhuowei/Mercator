package net.zhuoweizhang.mercator;

import java.io.*;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class GenerateMissing {
	private GenerateMissing() {}

	public static void generateMissingTextures(File inputDir) throws IOException {
		generateSheepTextures(inputDir, inputDir);
		generateGrassTextures(inputDir, inputDir);
	}

	private static void generateSheepTextures(File inputDir, File outputDir) throws IOException {
		Bitmap whiteSheep = getWhiteSheep(inputDir);
		if (whiteSheep == null) return;
		int sheepWidth = whiteSheep.getWidth();
		int sheepHeight = whiteSheep.getHeight();
		int[] allPixels = new int[sheepWidth * sheepHeight];
		for (int i = 0; i < 16; i++) {
			File sheepColourFile = new File(inputDir, "mob/sheep_ " + i + ".png");
			if (sheepColourFile.exists()) continue;
			//colour-blend the white sheep for this
			whiteSheep.getPixels(allPixels, 0, sheepWidth, 0, 0, sheepWidth, sheepHeight);
			for (int y = sheepHeight / 2; y < sheepHeight; y++) { //only re-colour the bottom half for now TODO: top
				for (int x = 0; x < sheepWidth; x++) {	
					int c = allPixels[y * sheepWidth + x];
					int r = c & 0xff;
					int g = (c >> 8) & 0xff;
					int b = (c >> 16) & 0xff;
					int a = (c >> 24) & 0xff;
					if (a == 0) continue;
					int bc = 0xff0000;
					int br = bc & 0xff;
					int bg = (bc >> 8) & 0xff;
					int bb = (bc >> 16) & 0xff;
					int ba = 0x7f;
					int fr = ((r - br) * ba + r) / 0xff;
					int fg = ((g - bg) * ba + g) / 0xff;
					int fb = ((b - br) * ba + b) / 0xff;
					int finalCol = fr | fg << 8 | fb << 16 | a << 24;
					allPixels[y * sheepWidth + x] = finalCol;
				}
			}
			Bitmap coloured = Bitmap.createBitmap(sheepWidth, sheepHeight, Bitmap.Config.ARGB_8888);
			coloured.setPixels(allPixels, 0, sheepWidth, 0, 0, sheepWidth, sheepHeight);
			FileOutputStream fos = new FileOutputStream(sheepColourFile);
			coloured.compress(Bitmap.CompressFormat.PNG, 100, fos);
			fos.close();
		}
	}

	private static Bitmap getWhiteSheep(File inputDir) {
		File alreadyThere = new File(inputDir, "mob/sheep_0.png");
		if (alreadyThere.exists()) {
			return BitmapFactory.decodeFile(alreadyThere.getAbsolutePath());
		}
		File topHalf = new File(inputDir, "mob/sheep.png");
		if (!topHalf.exists()) {
			topHalf = new File(inputDir, "entity/sheep/sheep.png");
		}

		// We have to build this one by hand
		File bottomHalf = new File(inputDir, "mob/sheep_fur.png");
		if (!bottomHalf.exists()) {
			bottomHalf = new File(inputDir, "entity/sheep/sheep_fur.png");
		}
		if (!topHalf.exists() || !bottomHalf.exists()) {
			return null;
		}
		Bitmap topHalfBitmap = BitmapFactory.decodeFile(topHalf.getAbsolutePath());
		Bitmap bottomHalfBitmap = BitmapFactory.decodeFile(bottomHalf.getAbsolutePath());
		Bitmap finalBitmap = Bitmap.createBitmap(topHalfBitmap.getWidth(), topHalfBitmap.getHeight() * 2, Bitmap.Config.ARGB_8888);
		int[] pixels = new int[topHalfBitmap.getWidth() * topHalfBitmap.getHeight()];
		topHalfBitmap.getPixels(pixels, 0, topHalfBitmap.getWidth(), 0, 0, topHalfBitmap.getWidth(), topHalfBitmap.getHeight());
		finalBitmap.setPixels(pixels, 0, topHalfBitmap.getWidth(), 0, 0, topHalfBitmap.getWidth(), topHalfBitmap.getHeight());
		bottomHalfBitmap.getPixels(pixels, 0, topHalfBitmap.getWidth(), 0, 0, topHalfBitmap.getWidth(), topHalfBitmap.getHeight());
		finalBitmap.setPixels(pixels, 0, topHalfBitmap.getWidth(), 0, topHalfBitmap.getHeight(), topHalfBitmap.getWidth(), topHalfBitmap.getHeight());
		return finalBitmap;
	}

	private static void generateGrassTextures(File inputDir, File outputDir) throws IOException {
	}
}
