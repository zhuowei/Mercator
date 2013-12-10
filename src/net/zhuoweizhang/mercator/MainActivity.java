package net.zhuoweizhang.mercator;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.os.Environment;
import java.io.*;
import java.util.*;
import android.view.View;
import android.widget.*;
public class MainActivity extends Activity implements View.OnClickListener
{

	private Button stitchButton, unstitchButton;
	private Button stitchItemsButton, unstitchItemsButton;
	private Button unstitchLegacyButton;
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
		}
	}

	public File getWorkingFolder() {
		return new File(Environment.getExternalStorageDirectory(), "Mercator");
	}

	public void stitch() {
		try {
			Map<String, String> myNameMap = UnstitchTGA.loadNameMap(getResources().openRawResource(R.raw.mapping));
			List<String> missingFiles = RestitchTGA.restitchTGA(new File(getWorkingFolder(), "unstitch/blocks"), 
				UnstitchTGA.readMap(getResources().openRawResource(R.raw.mojang_terrain)),
				new File(getWorkingFolder(), "output"), myNameMap);
			new AlertDialog.Builder(this).setTitle("Restitching successful").setMessage("Successful, with these missing files: \n"
				+ missingFiles.toString()).show();
			PrintWriter pw = new PrintWriter(new File("/sdcard/winprogress/terrain_rem.txt"));
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
				UnstitchTGA.readMap(getResources().openRawResource(R.raw.mojang_terrain)),
				new File(getWorkingFolder(), "unstitch/blocks"), myNameMap);
		} catch (Exception e) {
			e.printStackTrace();
			reportError(e);
		}
	}

	public void stitchItems() {
		try {
			Map<String, String> myNameMap = UnstitchTGA.loadNameMap(getResources().openRawResource(R.raw.mapping));
			List<String> missingFiles = RestitchTGA.restitchPNG(new File(getWorkingFolder(), "unstitch/items"), 
				UnstitchTGA.readMap(getResources().openRawResource(R.raw.mojang_items)),
				new File(getWorkingFolder(), "output/items-opaque.png"), myNameMap);
			new AlertDialog.Builder(this).setTitle("Restitching successful").setMessage("Successful, with these missing files: \n"
				+ missingFiles.toString()).show();
			PrintWriter pw = new PrintWriter(new File("/sdcard/winprogress/items_rem.txt"));
			pw.println(missingFiles.toString());
			pw.close();
		} catch (Exception e) {
			e.printStackTrace();
			reportError(e);
		}
	}

	public void unstitchItems() {
		try {
			Map<String, String> myNameMap = UnstitchTGA.loadNameMap(getResources().openRawResource(R.raw.mapping));
			UnstitchTGA.unstitchPNG(new File(getWorkingFolder(), "input/items-opaque.png"), 
				UnstitchTGA.readMap(getResources().openRawResource(R.raw.mojang_items)),
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
			

	private void reportError(final Throwable t) {
		this.runOnUiThread(new Runnable() {
			public void run() {
				StringWriter strWriter = new StringWriter();
				PrintWriter pWriter = new PrintWriter(strWriter);
				t.printStackTrace(pWriter);
				new AlertDialog.Builder(MainActivity.this).setTitle("Oh nose everything broke").setMessage(strWriter.toString()).
					setPositiveButton(android.R.string.ok, null).
					show();
			}
		});
	}
}
