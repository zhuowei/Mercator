package net.zhuoweizhang.mercator;

import tga.*;
import android.graphics.*;
import java.io.*;
import java.nio.*;
import java.nio.charset.*;
import org.json.*;
import java.util.*;

import net.zhuoweizhang.mercator.restitchgen.*;

import static net.zhuoweizhang.mercator.UnstitchTGA.getFilename;

public final class RestitchTGA {

	//private static Bitmap cachedIconBitmap;
	private static int[] cachedColorsArray;
	public static boolean stitchingItems = false;

	private RestitchTGA() {}

	/**
	 * @returns A list of missing files that should've been present in the TGA file
	 */
	public static List<String> restitchTGA(File inputDir, JSONArray map, File outputDir, Map<String, String> nameMap)
		throws IOException, JSONException {
		List<String> missingFiles = new ArrayList<String>();
		Bitmap outBmp = restitch(inputDir, map, nameMap, missingFiles);
		outputDir.mkdirs();
		writeTGA(outBmp, new File(outputDir, "terrain-atlas.tga"));
		for (int mipLevel = 0; mipLevel < 4; mipLevel++) {
			int mipDivisor = 2 << mipLevel;
			Bitmap scaledBmp = Bitmap.createScaledBitmap(outBmp, outBmp.getWidth() / mipDivisor, outBmp.getHeight() / mipDivisor,
				false);
			writeTGA(scaledBmp, new File(outputDir, "terrain-atlas_mip" + mipLevel + ".tga"));
		}
		return missingFiles;
	}
	public static void writeTGA(Bitmap outBmp, File outputFile) throws IOException {
		ByteBuffer data = ByteBuffer.allocate(outBmp.getWidth() * outBmp.getHeight() * 4);
		int[] tempArr = new int[outBmp.getWidth() * outBmp.getHeight()];
		outBmp.getPixels(tempArr, 0, outBmp.getWidth(), 0, 0, outBmp.getWidth(), outBmp.getHeight());
		data.order(ByteOrder.LITTLE_ENDIAN).asIntBuffer().put(tempArr);
		tempArr = null;
		invertBuffer(data, outBmp.getWidth(), outBmp.getHeight());
		TGAImage tgaImage = TGAImage.createFromData(outBmp.getWidth(), outBmp.getHeight(),
			true, false, data);
		tgaImage.write(outputFile);
	}

	public static List<String> restitchOneTGA(File inputDir, JSONArray map, File outputFile, Map<String, String> nameMap)
		throws IOException, JSONException {
		List<String> missingFiles = new ArrayList<String>();
		Bitmap outBmp = restitch(inputDir, map, nameMap, missingFiles);
		writeTGA(outBmp, outputFile);
		return missingFiles;
	}

	/**
	 * @returns A list of missing files that should've been present in the PNG file
	 */
	public static List<String> restitchPNG(File inputDir, JSONArray map, File outputFile, Map<String, String> nameMap)
		throws IOException, JSONException {
		List<String> missingFiles = new ArrayList<String>();
		Bitmap outBmp = restitch(inputDir, map, nameMap, missingFiles);
		FileOutputStream fos = new FileOutputStream(outputFile);
		outBmp.compress(Bitmap.CompressFormat.PNG, 100, fos);
		fos.close();
		return missingFiles;
	}

	private static void invertBuffer(ByteBuffer buf, int width, int height) {
		//taken from BlockLauncher's screenshot function
		byte[] rowBuffer = new byte[width * 4 * 2];
		int stride = width * 4;
		for (int y = 0; y < height / 2; ++y) {
			//exchange the rows to
			//invert the image somewhat in-place.
			buf.position(y * stride);
			buf.get(rowBuffer, 0, stride); //top row
			buf.position((height - y - 1) * stride);
			buf.get(rowBuffer, stride, stride); //bottom row
			buf.position((height - y - 1) * stride);
			buf.put(rowBuffer, 0, stride);
			buf.position(y * stride);
			buf.put(rowBuffer, stride, stride);
		}
		rowBuffer = null;
		buf.rewind();
	}

	public static Bitmap restitch(File inputDir, JSONArray map, Map<String, String> nameMap, List<String> missingFiles)
		throws IOException, JSONException {
		Bitmap outBmp = null;
		int arrayLength = map.length();
		int scaleFactor = getScaleFactor(inputDir);
		for (int i = 0; i < arrayLength; i++) {
			JSONObject iconInfo = map.getJSONObject(i);
			if (outBmp == null) {
				outBmp = makeBmp(iconInfo, scaleFactor);
			}
			stitchOneItem(inputDir, iconInfo, outBmp, nameMap, missingFiles, scaleFactor);
		}

		//if (cachedIconBitmap != null) {
		//	cachedIconBitmap.recycle();
		//	cachedIconBitmap = null;
		//}
		cachedColorsArray = null;
		if (stitchingItems && scaleFactor != 1) return pictureInPicture(outBmp, scaleFactor);
		return outBmp;
	}

	private static void stitchOneItem(File inputDir, JSONObject iconInfo, Bitmap outBmp, Map<String, String> nameMap,
		List<String> missingFiles, int scaleFactor) throws IOException, JSONException {
		String rawName = iconInfo.getString("name");
		//String name = nameMap.get(rawName);
		//if (name == null) name = rawName;

		JSONArray textures = iconInfo.getJSONArray("uvs");
		int texturesLength = textures.length();

		for (int i = 0; i < texturesLength; i++) {
			JSONArray textureUV = textures.getJSONArray(i);
			String fileName = getFilename(rawName, i, texturesLength, nameMap);
			File inputFile = new File(inputDir, fileName + ".png");
			stitchOneIcon(inputFile, textureUV, outBmp, missingFiles, scaleFactor);
		}
	}

	private static Bitmap getStitchInputBitmap(File inputFile) throws IOException {
		boolean nofile = !inputFile.exists();
		String name = inputFile.getName();
		File parent = inputFile.getParentFile();
		for (RestitchGen gen: restitchGens) {
			boolean force = nofile || gen.forceGen(name, parent);
			if (!force) continue;
			Bitmap b = gen.restitchGen(name, parent);
			if (b != null) return b;
		}
		if (nofile) {
			return null;
		}
		BitmapFactory.Options opts = new BitmapFactory.Options();
		Bitmap bmp = BitmapFactory.decodeFile(inputFile.getAbsolutePath(), opts);
		return bmp;
	}

	private static void stitchOneIcon(File inputFile, JSONArray uv, Bitmap outBmp, List<String> missingFiles, int scaleFactor)
		throws IOException, JSONException {
		Bitmap bmp = getStitchInputBitmap(inputFile);
		if (bmp == null) {
			missingFiles.add(inputFile.getName());
			return;
		}

		double x1 = uv.getDouble(0);
		double y1 = uv.getDouble(1);
		double x2 = uv.getDouble(2);
		double y2 = uv.getDouble(3);
		double imgWidth = uv.getDouble(4);
		double imgHeight = uv.getDouble(5);
		int sx = (int) x1;
		int sy = (int) y1;
		int width = (int) x2 - sx;
		int height = (int) y2 - sy;
		sx *= scaleFactor; sy *= scaleFactor; width *= scaleFactor; height *= scaleFactor;
		// FIXME: scale smaller pictures
		width = Math.min(width, bmp.getWidth()); height = Math.min(height, bmp.getHeight());
		if (scaleFactor != 1) sy = moveYIfNeeded(sx, sy, (int)imgWidth, (int)imgHeight, outBmp.getHeight());
		//System.out.println("sx " + sx + " sy " + sy + " width " + width + " height " + height);

		int supposedArrayLength = width * height;
		if (cachedColorsArray == null || cachedColorsArray.length != supposedArrayLength) {
			cachedColorsArray = new int[supposedArrayLength];
		}
		bmp.getPixels(cachedColorsArray, 0, width, 0, 0, width, height);
		outBmp.setPixels(cachedColorsArray, 0, width, sx, sy, width, height);
		//cachedIconBitmap = bmp;
	}

	private static Bitmap makeBmp(JSONObject iconInfo, int scaleFactor) throws JSONException {
		JSONArray uv = iconInfo.getJSONArray("uvs").getJSONArray(0);
		int imgWidth = (int) uv.getDouble(4);
		int imgHeight = (int) uv.getDouble(5);
		return Bitmap.createBitmap(imgWidth*scaleFactor, imgHeight*scaleFactor, Bitmap.Config.ARGB_8888);
	}

	private static int getScaleFactor(File inputDir) {
		File sizingFile = new File(inputDir, "dirt.png");
		if (!sizingFile.exists()) {
			sizingFile = new File(inputDir, "apple.png");
		}
		if (!sizingFile.exists()) return 1;
		Bitmap bmp = BitmapFactory.decodeFile(sizingFile.getAbsolutePath());
		if (bmp == null) return 1;
		return bmp.getWidth() / 16;
	}

	public static void generateMeta(File inputDir, File outputFile, JSONArray map) throws IOException, JSONException {
		int arrayLength = map.length();
		final int scaleFactor = getScaleFactor(inputDir);
		for (int i = 0; scaleFactor != 1 && i < arrayLength; i++) {
			JSONObject iconInfo = map.getJSONObject(i);
			JSONArray uvs = iconInfo.getJSONArray("uvs");
			int arrayLength2 = uvs.length();
			for (int j = 0; j < arrayLength2; j++) {
				JSONArray uv = uvs.getJSONArray(j);
				int sx = (int) uv.getDouble(0);
				int sy = (int) uv.getDouble(1);
				int imgWidth = (int) uv.getDouble(4);
				int imgHeight = (int) uv.getDouble(5);
				int newy = moveYIfNeeded(sx*scaleFactor, sy*scaleFactor, imgWidth, imgHeight, imgHeight * scaleFactor)
						/ scaleFactor;
				if (newy == sy) continue;
				uv.put(1, newy);
				uv.put(3, newy + 16);
			}
		}
		FileOutputStream fos = new FileOutputStream(outputFile);
		fos.write(map.toString().getBytes("UTF-8"));
		fos.close();
	}

	public static int moveYIfNeeded(int sx, int sy, int imgWidth, int imgHeight, int fullHeight) {
		if (!stitchingItems) return sy;
		//System.out.println("move y if needed " + sx + ":" + sy + ":" + imgWidth + ":" + imgHeight + ":" + fullHeight);
		if (sy >= imgHeight || sx >= imgWidth) return sy;
		return fullHeight - imgHeight + sy;
	}

	public static Bitmap pictureInPicture(Bitmap bmp, int scaleFactor) {
		Bitmap tiny = Bitmap.createScaledBitmap(bmp, bmp.getWidth() / scaleFactor, bmp.getHeight() / scaleFactor, true);
		int[] tinyPixels = new int[tiny.getWidth() * tiny.getHeight()];
		tiny.getPixels(tinyPixels, 0, tiny.getWidth(), 0, 0, tiny.getWidth(), tiny.getHeight());
		bmp.setPixels(tinyPixels, 0, tiny.getWidth(), 0, 0, tiny.getWidth(), tiny.getHeight());
		return bmp;
	}

	private static RestitchGen[] restitchGens = {
		new SpawnEggGen(),
		new PotionGen()
	};
}
