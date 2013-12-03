package net.zhuoweizhang.mercator;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import java.io.*;
import java.util.*;
import android.view.View;
import android.widget.*;
public class MainActivity extends Activity implements View.OnClickListener
{

	private Button stitchButton, unstitchButton;
	private Button stitchItemsButton, unstitchItemsButton;
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
		}
	}

	public void stitch() {
		try {
			Map<String, String> myNameMap = UnstitchTGA.loadNameMap(getResources().openRawResource(R.raw.mapping));
			List<String> missingFiles = RestitchTGA.restitchTGA(new File("/sdcard/winprogress/terrainout"), 
				UnstitchTGA.readMap(new File("/sdcard/terrain.meta")),
				new File("/sdcard/winprogress/terrainstitch"), myNameMap);
			new AlertDialog.Builder(this).setTitle("Restitching successful").setMessage("Successful, with these missing files: \n"
				+ missingFiles.toString()).show();
		} catch (Exception e) {
			e.printStackTrace();
			reportError(e);
		}
	}

	public void unstitch() {
		try {
			Map<String, String> myNameMap = UnstitchTGA.loadNameMap(getResources().openRawResource(R.raw.mapping));
			UnstitchTGA.unstitchTGA(new File("/sdcard/terrain-atlas.tga"), new File("/sdcard/terrain.meta"),
				new File("/sdcard/winprogress/terrainout"), myNameMap);
		} catch (Exception e) {
			e.printStackTrace();
			reportError(e);
		}
	}

	public void stitchItems() {
		try {
			Map<String, String> myNameMap = UnstitchTGA.loadNameMap(getResources().openRawResource(R.raw.mapping));
			List<String> missingFiles = RestitchTGA.restitchPNG(new File("/sdcard/winprogress/itemout"), 
				UnstitchTGA.readMap(new File("/sdcard/items.meta")),
				new File("/sdcard/winprogress/items-opaque.png"), myNameMap);
			new AlertDialog.Builder(this).setTitle("Restitching successful").setMessage("Successful, with these missing files: \n"
				+ missingFiles.toString()).show();
		} catch (Exception e) {
			e.printStackTrace();
			reportError(e);
		}
	}

	public void unstitchItems() {
		try {
			Map<String, String> myNameMap = UnstitchTGA.loadNameMap(getResources().openRawResource(R.raw.mapping));
			UnstitchTGA.unstitchPNG(new File("/sdcard/items-opaque.png"), UnstitchTGA.readMap(new File("/sdcard/items.meta")),
				new File("/sdcard/winprogress/itemout"), myNameMap);
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
