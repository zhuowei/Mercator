package net.zhuoweizhang.mercator.restitchgen;

import java.io.File;
import java.io.IOException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class SpawnEggGen extends RestitchGen {

	public static int[] spawnEggColours = {
0xa1a1a1, 0xff0000, /* 0 */
0x443626, 0xa1a1a1, /* 1 */
0xf0a5a2, 0xdb635f, /* 2 */
0xe7e7e7, 0xffb5b5, /* 3 */
0xd7d3d3, 0xceaf96, /* 4 */
0xa00f10, 0xb7b7b7, /* 5 */
0xda60b, 0x0, /* 6 */
0x161616, 0x151515, /* 7 */
0x6e6e6e, 0x6d6d6d, /* 8 */
0xc1c1c1, 0x494949, /* 9 */
0x51a03e, 0x7ebf6d, /* 10 */
0x342d27, 0xa80e0e, /* 11 */
0xafaf, 0x799c66, /* 12 */
0xea9494, 0x4c7129, /* 13 */
0x563c33, 0xbe8b72, /* 14 */
0x223b4d, 0x708999, /* 15 */
0xefde7d, 0x564434, /* 16 */
0x340000, 0x51a03e, /* 17 */
0x4c3e30, 0xf0f0f, /* 18 */
0xf9f9f9, 0xbcbcbc, /* 19 */
0x340000, 0xfcfc00, /* 20 */
0xf6b201, 0xfff87e, /* 21 */
0xc424e, 0xa80e0e, /* 22 */
0xc09e7d, 0xeee500, /* 23 */
0x995f40, 0x734831, /* 24 */
0x161616, 0x6d6d6d, /* 25 */
0x5a8272, 0xf17d31, /* 26 */
};

	public static int[] getPixels(Bitmap bmp) {
		int[] retval = new int[bmp.getWidth() * bmp.getHeight()];
		bmp.getPixels(retval, 0, bmp.getWidth(), 0, 0, bmp.getWidth(), bmp.getHeight());
		return retval;
	}

	public static int mul(int a, int b) {
		int bb = ((a & 0xff) * (b & 0xff))/0xff & 0xff;
		int g = (((a >> 8) & 0xff) * ((b >> 8) & 0xff))/0xff & 0xff;
		int r = (((a >> 16) & 0xff) * ((b >> 16) & 0xff))/0xff & 0xff;
		return r << 16 | g << 8 | bb << 0 | (a & 0xff000000);
	}

	public static int mix(int a, int b) {
		int ba = (b >> 24 & 0xff);
		int bb = ((a & 0xff) + (b & 0xff)*ba/0xff) & 0xff;
		int g = (((a >> 8) & 0xff) + ((b >> 8) & 0xff)*ba/0xff) & 0xff;
		int r = (((a >> 16) & 0xff) + ((b >> 16) & 0xff)*ba/0xff) & 0xff;
		int aa = (a >> 24 & 0xff);
		int aaa = (aa + ba*(0xff-aa)/0xff)&0xff;
		return r << 16 | g << 8 | bb << 0 | aaa << 24;
	}

	public boolean forceGen(String fileName, File sourceDir) {
		return fileName.equals("spawn_egg.png") && new File(sourceDir, "spawn_egg_overlay.png").exists();
	}

	public Bitmap restitchGen(String fileName, File sourceDir) throws IOException {
		boolean isZeroth = fileName.equals("spawn_egg.png");
		if (!(fileName.startsWith("spawn_egg_") || isZeroth)) return null;
		int id = isZeroth? 0: Integer.parseInt(fileName.substring("spawn_egg_".length(), fileName.lastIndexOf(".")));
		if (id*2 >= spawnEggColours.length) return null;
		int colour1 = spawnEggColours[id*2];
		int colour2 = spawnEggColours[id*2 + 1];
		Bitmap baseBmp = BitmapFactory.decodeFile(new File(sourceDir, "spawn_egg.png").getAbsolutePath());
		Bitmap overlayBmp = BitmapFactory.decodeFile(new File(sourceDir, "spawn_egg_overlay.png").getAbsolutePath());
		if (baseBmp == null || overlayBmp == null) return null;
		int[] pixels1 = getPixels(baseBmp);
		int[] pixels2 = getPixels(overlayBmp);
		if (pixels1.length != pixels2.length) return null;
		for (int i = 0; i < pixels1.length; i++) {
			final int a = pixels1[i];
			final int b = pixels2[i];
			final int c = mix(mul(a, colour1), mul(b, colour2));
			pixels1[i] = c;
		}
		return Bitmap.createBitmap(pixels1, baseBmp.getWidth(), baseBmp.getHeight(), Bitmap.Config.ARGB_8888);
	}
}
