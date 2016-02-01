package net.zhuoweizhang.mercator.restitchgen;

import java.io.File;
import java.io.IOException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import static net.zhuoweizhang.mercator.restitchgen.SpawnEggGen.*;

public class PotionGen extends RestitchGen {
	public static final int[] potionColours = {
0x385dc6,	/* 0 */
0x7cafc6,	/* 1 */
0x5a6c81,	/* 2 */
0xd9c043,	/* 3 */
0x4a4217,	/* 4 */
0x932423,	/* 5 */
0xf82423,	/* 6 */
0x430a09,	/* 7 */
0x22ff4c,	/* 8 */
0x551d4a,	/* 9 */
0xcd5cab,	/* 10 */
0x99453a,	/* 11 */
0xe49a3a,	/* 12 */
0x2e5299,	/* 13 */
0x7f8392,	/* 14 */
0x1f1f23,	/* 15 */
0x1f1fa1,	/* 16 */
0x587653,	/* 17 */
0x484d48,	/* 18 */
0x4e9331,	/* 19 */
0x352a27,	/* 20 */
0xf87d23,	/* 21 */
0x2552a5,	/* 22 */
0xf82423,	/* 23 */
};
	public boolean forceGen(String fileName, File sourceDir) {
		return fileName.equals("potion_bottle_drinkable.png") || fileName.equals("potion_bottle_splash.png");
	}

	public Bitmap restitchGen(String fileName, File sourceDir) throws IOException {
		if (!(fileName.startsWith("potion_bottle_drinkable") || fileName.startsWith("potion_bottle_splash"))) return null;
		boolean isSplash = fileName.startsWith("potion_bottle_splash");
		boolean isZeroth = fileName.equals("potion_bottle_drinkable.png") || fileName.equals("potion_bottle_splash.png");
		int id = isZeroth? 0: Integer.parseInt(fileName.substring(fileName.lastIndexOf("_") + 1, fileName.lastIndexOf(".")));
		if (id >= potionColours.length) return null;
		int colour1 = potionColours[id];
		Bitmap baseBmp = BitmapFactory.decodeFile(new File(sourceDir, isSplash? "potion_bottle_splash.png":
			"potion_bottle_drinkable.png").getAbsolutePath());
		Bitmap overlayBmp = BitmapFactory.decodeFile(new File(sourceDir, "potion_overlay.png").getAbsolutePath());
		if (baseBmp == null || overlayBmp == null) return null;
		int[] pixels1 = getPixels(baseBmp);
		int[] pixels2 = getPixels(overlayBmp);
		if (pixels1.length != pixels2.length) return null;
		for (int i = 0; i < pixels1.length; i++) {
			final int a = pixels1[i];
			final int b = pixels2[i];
			final int c = mix(a, mul(b, colour1));
			pixels1[i] = c;
		}
		return Bitmap.createBitmap(pixels1, baseBmp.getWidth(), baseBmp.getHeight(), Bitmap.Config.ARGB_8888);
	}
}
