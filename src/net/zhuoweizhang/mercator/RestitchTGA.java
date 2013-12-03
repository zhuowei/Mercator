package net.zhuoweizhang.mercator;

import tga.*;
import android.graphics.*;
import java.io.*;
import java.nio.*;
import java.nio.charset.*;
import org.json.*;
import java.util.*;

import static net.zhuoweizhang.mercator.UnstitchTGA.getFilename;

public final class RestitchTGA {

	//private static Bitmap cachedIconBitmap;
	private static int[] cachedColorsArray;

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
	private static void writeTGA(Bitmap outBmp, File outputFile) throws IOException {
		ByteBuffer data = ByteBuffer.allocate(outBmp.getWidth() * outBmp.getHeight() * 4);
		outBmp.copyPixelsToBuffer(data);
		invertBuffer(data, outBmp.getWidth(), outBmp.getHeight());
		//RGB -> BGR
		byte[] dataBytes = data.array();
		TGAImage.swapBGR(dataBytes, outBmp.getWidth() * 4, outBmp.getHeight(), 4);
		TGAImage tgaImage = TGAImage.createFromData(outBmp.getWidth(), outBmp.getHeight(),
			true, false, data);
		tgaImage.write(outputFile);
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
		for (int i = 0; i < arrayLength; i++) {
			JSONObject iconInfo = map.getJSONObject(i);
			if (outBmp == null) {
				outBmp = makeBmp(iconInfo);
			}
			stitchOneItem(inputDir, iconInfo, outBmp, nameMap, missingFiles);
		}

		//if (cachedIconBitmap != null) {
		//	cachedIconBitmap.recycle();
		//	cachedIconBitmap = null;
		//}
		cachedColorsArray = null;
		return outBmp;
	}

	private static void stitchOneItem(File inputDir, JSONObject iconInfo, Bitmap outBmp, Map<String, String> nameMap,
		List<String> missingFiles) throws IOException, JSONException {
		String rawName = iconInfo.getString("name");
		//String name = nameMap.get(rawName);
		//if (name == null) name = rawName;

		JSONArray primaryTextureUV = iconInfo.getJSONArray("uv");
		JSONArray secondaryTextures = iconInfo.getJSONArray("additonal_textures");
		int secondaryLength = secondaryTextures.length();

		String fileName = getFilename(rawName, 0, secondaryLength, nameMap);
		File inputFile = new File(inputDir, fileName + ".png");
		stitchOneIcon(inputFile, primaryTextureUV, outBmp, missingFiles);

		for (int i = 0; i < secondaryLength; i++) {
			JSONArray secondaryTextureUV = secondaryTextures.getJSONArray(i);
			fileName = getFilename(rawName, i + 1, secondaryLength, nameMap);
			inputFile = new File(inputDir, fileName + ".png");
			stitchOneIcon(inputFile, secondaryTextureUV, outBmp, missingFiles);
		}
	}

	private static void stitchOneIcon(File inputFile, JSONArray uv, Bitmap outBmp, List<String> missingFiles)
		throws IOException, JSONException {
		if (!inputFile.exists()) {
			missingFiles.add(inputFile.getName());
			return;
		}
		//BitmapFactory.Options opts = new BitmapFactory.Options();
		//opts.inBitmap = cachedIconBitmap;
		Bitmap bmp = BitmapFactory.decodeFile(inputFile.getAbsolutePath());
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
		int sx = (int) (imgWidth * x1 + 0.5);
		int sy = (int) (imgHeight * y1 + 0.5);
		int width = (int) (imgWidth * x2 + 0.5) - sx;
		int height = (int) (imgHeight * y2 + 0.5) - sy;

		int supposedArrayLength = width * height;
		if (cachedColorsArray == null || cachedColorsArray.length != supposedArrayLength) {
			cachedColorsArray = new int[supposedArrayLength];
		}
		bmp.getPixels(cachedColorsArray, 0, width, 0, 0, width, height);
		outBmp.setPixels(cachedColorsArray, 0, width, sx, sy, width, height);
		//cachedIconBitmap = bmp;
	}

	private static Bitmap makeBmp(JSONObject iconInfo) throws JSONException {
		JSONArray uv = iconInfo.getJSONArray("uv");
		int imgWidth = (int) uv.getDouble(4);
		int imgHeight = (int) uv.getDouble(5);
		return Bitmap.createBitmap(imgWidth, imgHeight, Bitmap.Config.ARGB_8888);
	}
}
