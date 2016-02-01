package net.zhuoweizhang.mercator;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import java.io.*;
import java.util.*;
import android.view.View;
import android.widget.*;

import com.ipaulpro.afilechooser.FileChooserActivity;
import com.ipaulpro.afilechooser.utils.FileUtils;
public class MercatorActivity extends Activity implements View.OnClickListener
{

	private static final int REQUEST_SELECT_TGA = 0x1000;
	private static final int REQUEST_SELECT_PNG = 0x1001;
	private Button stitchButton, unstitchButton;
	private Button stitchItemsButton, unstitchItemsButton;
	private Button unstitchLegacyButton;
	private Button tgaToPngButton, pngToTgaButton;
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		stitchButton = (Button) findViewById(R.id.main_stitch_button);
		stitchButton.setOnClickListener(this);
		unstitchButton = (Button) findViewById(R.id.main_unstitch_button);
		unstitchButton.setOnClickListener(this);
		stitchItemsButton = (Button) findViewById(R.id.main_stitch_items_button);
		stitchItemsButton.setOnClickListener(this);
		unstitchItemsButton = (Button) findViewById(R.id.main_unstitch_items_button);
		unstitchItemsButton.setOnClickListener(this);
		unstitchLegacyButton = (Button) findViewById(R.id.main_unstitch_legacy_button);
		unstitchLegacyButton.setOnClickListener(this);
		tgaToPngButton = (Button) findViewById(R.id.main_tga_to_png_button);
		tgaToPngButton.setOnClickListener(this);
		pngToTgaButton = (Button) findViewById(R.id.main_png_to_tga_button);
		pngToTgaButton.setOnClickListener(this);
		createWorkingFolder();
	}
	public void onClick(View v) {
		if (v == stitchButton) {
			stitch();
		} else if (v == unstitchButton) {
			unstitch();
		} else if (v == stitchItemsButton) {
			stitchItems();
		} else if (v == unstitchItemsButton) {
			unstitchItems();
		} else if (v == unstitchLegacyButton) {
			unstitchLegacy();
		} else if (v == tgaToPngButton) {
			tgaToPng();
		} else if (v == pngToTgaButton) {
			pngToTga();
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
			case REQUEST_SELECT_TGA:  
				if (resultCode == RESULT_OK) {  
					final Uri uri = data.getData();
					File file = FileUtils.getFile(uri);
					String newFileName = file.getName().substring(0, file.getName().length() - 3) + "png";
					try {
						ConvertTGA.tgaToPng(file, new File(file.getParentFile(), newFileName));
					} catch (Exception e) {
						e.printStackTrace();
						reportError(e);
					}

				}
				break;
			case REQUEST_SELECT_PNG:  
				if (resultCode == RESULT_OK) {  
					final Uri uri = data.getData();
					File file = FileUtils.getFile(uri);
					String newFileName = file.getName().substring(0, file.getName().length() - 3) + "tga";
					try {
						ConvertTGA.pngToTga(file, new File(file.getParentFile(), newFileName));
					} catch (Exception e) {
						e.printStackTrace();
						reportError(e);
					}

				}
				break;
		}
	}

	public File getWorkingFolder() {
		return new File(Environment.getExternalStorageDirectory(), "Mercator");
	}

	public void stitch() {
		try {
			Map<String, String> myNameMap = UnstitchTGA.loadNameMap(getResources().openRawResource(R.raw.mapping));
			List<String> missingFiles = RestitchTGA.restitchTGA(new File(getWorkingFolder(), "unstitch/blocks"), 
				UnstitchTGA.readMap(getResources().openRawResource(R.raw.terrain)),
				new File(getWorkingFolder(), "output"), myNameMap);
			new AlertDialog.Builder(this).setTitle("Restitching successful").setMessage("Successful, with these missing files: \n"
				+ missingFiles.toString()).show();
			PrintWriter pw = new PrintWriter(new File(getWorkingFolder(), "terrain_rem.txt"));
			pw.println(missingFiles.toString());
			pw.close();
		} catch (Exception e) {
			e.printStackTrace();
			reportError(e);
		}
	}

	public void unstitch() {
		try {
			Map<String, String> myNameMap = UnstitchTGA.loadNameMap(getResources().openRawResource(R.raw.mapping));
			UnstitchTGA.unstitchTGA(new File(getWorkingFolder(), "input/terrain-atlas.tga"),
				UnstitchTGA.readMap(getResources().openRawResource(R.raw.terrain)),
				new File(getWorkingFolder(), "unstitch/blocks"), myNameMap);
		} catch (Exception e) {
			e.printStackTrace();
			reportError(e);
		}
	}

	public void stitchItems() {
		try {
			Map<String, String> myNameMap = UnstitchTGA.loadNameMap(getResources().openRawResource(R.raw.mapping_items));
			List<String> missingFiles = RestitchTGA.restitchOneTGA(new File(getWorkingFolder(), "unstitch/items"),
				UnstitchTGA.readMap(getResources().openRawResource(R.raw.items)),
				new File(getWorkingFolder(), "output/items-opaque.tga"), myNameMap);
			new AlertDialog.Builder(this).setTitle("Restitching successful").setMessage("Successful, with these missing files: \n"
				+ missingFiles.toString()).show();
			PrintWriter pw = new PrintWriter(new File(getWorkingFolder(), "items_rem.txt"));
			pw.println(missingFiles.toString());
			pw.close();
		} catch (Exception e) {
			e.printStackTrace();
			reportError(e);
		}
	}

	public void unstitchItems() {
		try {
			Map<String, String> myNameMap = UnstitchTGA.loadNameMap(getResources().openRawResource(R.raw.mapping_items));
			UnstitchTGA.unstitchTGA(new File(getWorkingFolder(), "input/items-opaque.tga"),
				UnstitchTGA.readMap(getResources().openRawResource(R.raw.items)),
				new File(getWorkingFolder(), "unstitch/items"), myNameMap);
		} catch (Exception e) {
			e.printStackTrace();
			reportError(e);
		}
	}

	public void unstitchLegacy() {
		try {
			List<UnstitchLegacy.LegacyCoord> blocksCoords = UnstitchLegacy.loadLegacyCoord(getResources().openRawResource(
				R.raw.mojang_unstitcher_blocks));
			List<UnstitchLegacy.LegacyCoord> itemsCoords = UnstitchLegacy.loadLegacyCoord(getResources().openRawResource(
				R.raw.mojang_unstitcher_items));
			File outputFolderTopLevel = new File(getWorkingFolder(), "unstitch");
			File blocksOutput = new File(outputFolderTopLevel, "blocks");
			File inputFolder = new File(getWorkingFolder(), "input");
			UnstitchLegacy.unstitchLegacy(new File(inputFolder, "terrain.png"), blocksCoords, blocksOutput);
			File itemsOutput = new File(outputFolderTopLevel, "items");
			UnstitchLegacy.unstitchLegacy(new File(inputFolder, "items.png"), itemsCoords, itemsOutput);
		} catch (Exception e) {
			e.printStackTrace();
			reportError(e);
		}
	}

	protected void tgaToPng() {
		Intent target = FileUtils.createGetContentIntent();
		target.setType("image/x-targa");
		target.setClass(this, FileChooserActivity.class);
		startActivityForResult(target, REQUEST_SELECT_TGA);
	}

	protected void pngToTga() {
		Intent target = FileUtils.createGetContentIntent();
		target.setType("image/png");
		target.setClass(this, FileChooserActivity.class);
		startActivityForResult(target, REQUEST_SELECT_PNG);
	}

	private void createWorkingFolder() {
		File workingFolder = getWorkingFolder();
		workingFolder.mkdirs();
		try {
			new File(workingFolder, ".nomedia").createNewFile();
		} catch (IOException ie) {
			ie.printStackTrace();
			reportError(ie);
		}
	}

	private void reportError(final Throwable t) {
		this.runOnUiThread(new Runnable() {
			public void run() {
				StringWriter strWriter = new StringWriter();
				PrintWriter pWriter = new PrintWriter(strWriter);
				t.printStackTrace(pWriter);
				new AlertDialog.Builder(MercatorActivity.this).setTitle("Oh nose everything broke").setMessage(strWriter.toString()).
					setPositiveButton(android.R.string.ok, null).
					show();
			}
		});
	}
}
