package net.zhuoweizhang.mercator;

import tga.*;
import android.graphics.*;
import java.io.*;
import java.nio.*;
import java.nio.charset.*;
import org.json.*;
import java.util.*;

public final class UnstitchTGA {
	private UnstitchTGA(){}

	public static JSONArray readMap(File mapFile) throws IOException, JSONException {
		byte[] inputBytes = new byte[(int) mapFile.length()];
		FileInputStream fis = new FileInputStream(mapFile);
		fis.read(inputBytes);
		fis.close();
		String inputStr = new String(inputBytes, Charset.forName("UTF-8"));
		JSONArray map = new JSONArray(inputStr);
		return map;
	}
	public static void unstitchTGA(File inputFile, File mapFile, File outputDir, Map<String, String> nameMap)
		throws IOException, JSONException {
		JSONArray map = readMap(mapFile);
		unstitchTGA(inputFile, map, outputDir, nameMap);
	}
	public static void unstitchTGA(File inputFile, JSONArray map, File outputDir, Map<String, String> nameMap)
		throws IOException, JSONException {
		outputDir.mkdirs();
		FileInputStream fis = new FileInputStream(inputFile);
		TGAImage img = TGAImage.read(fis);
		Bitmap bmp = Bitmap.createBitmap(img.getWidth(), img.getHeight(), Bitmap.Config.ARGB_8888);
		//bmp.copyPixelsFromBuffer(img.getData()); //TODO: premultiplied alphas?
		int[] outArr = new int[img.getWidth() * img.getHeight()];
		img.getData().order(ByteOrder.LITTLE_ENDIAN);
		IntBuffer myIntBuffer = img.getData().asIntBuffer();
		myIntBuffer.position(0);
		myIntBuffer.get(outArr);
		bmp.setPixels(outArr, 0, img.getWidth(), 0, 0, img.getWidth(), img.getHeight());
		outArr = null;
		img = null;
		myIntBuffer = null;
		unstitch(bmp, map, outputDir, nameMap);
	}

	public static void unstitchPNG(File inputFile, JSONArray map, File outputDir, Map<String, String> nameMap)
		throws IOException, JSONException {
		outputDir.mkdirs();
		Bitmap bmp = BitmapFactory.decodeFile(inputFile.getAbsolutePath());
		unstitch(bmp, map, outputDir, nameMap);
	}

	public static void unstitch(Bitmap bmp, JSONArray map, File outputDir, Map<String, String> nameMap) throws IOException, JSONException {
		int arrayLength = map.length();
		for (int i = 0; i < arrayLength; i++) {
			JSONObject iconInfo = map.getJSONObject(i);
			unstitchOne(bmp, iconInfo, outputDir, nameMap);
		}
	}

	private static void unstitchOne(Bitmap bmp, JSONObject iconInfo, File outputDir, Map<String, String> nameMap) 
		throws IOException, JSONException {
		String rawName = iconInfo.getString("name");
		//String name = nameMap.get(rawName);
		//if (name == null) name = rawName;

		JSONArray primaryTextureUV = iconInfo.getJSONArray("uv");
		JSONArray secondaryTextures = iconInfo.getJSONArray("additonal_textures");
		int secondaryLength = secondaryTextures.length();

		String fileName = getFilename(rawName, 0, secondaryLength, nameMap);
		File outputFile = new File(outputDir, fileName + ".png");
		copyOneIcon(bmp, primaryTextureUV, outputFile);

		for (int i = 0; i < secondaryLength; i++) {
			JSONArray secondaryTextureUV = secondaryTextures.getJSONArray(i);
			fileName = getFilename(rawName, i + 1, secondaryLength, nameMap);
			outputFile = new File(outputDir, fileName + ".png");
			copyOneIcon(bmp, secondaryTextureUV, outputFile);
		}

	}

	public static String getFilename(String blockName, int number, int secondaryLength, Map<String, String> nameMap) {
		if (blockName.length() > 2 && blockName.substring(blockName.length() - 2).equals("_x")) {
			blockName = blockName.substring(0, blockName.length() - 2);
		}
		String fileName = blockName + (secondaryLength != 0? "_" + number: "");
		String altFileName = nameMap.get(fileName);
		if (altFileName != null) fileName = altFileName;
		return fileName;
	}

	private static void copyOneIcon(Bitmap bmp, JSONArray uv, File output) throws IOException, JSONException {
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
		Bitmap out = Bitmap.createBitmap(bmp, sx, sy, width, height);
		FileOutputStream fos = new FileOutputStream(output);
		out.compress(Bitmap.CompressFormat.PNG, 100, fos);
		fos.close();
	}

	public static Map<String, String> loadNameMap(InputStream is) throws IOException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		String curLine;
		Map<String, String> retval = new HashMap<String, String>();
		while ((curLine = reader.readLine()) != null) {
			curLine = curLine.trim();
			if (curLine.length() < 1 || curLine.charAt(0) == '#') continue;
			String[] parts = curLine.split(",");
			if (parts.length != 2) continue;
			if (retval.containsKey(parts[0]))
				throw new RuntimeException("Duplicate in name map: " + curLine);
			retval.put(parts[0], parts[1]);
		}
		reader.close();
		return retval;
	}
}
