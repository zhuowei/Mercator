package net.zhuoweizhang.mercator;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import java.io.*;
import java.util.*;
public class MainActivity extends Activity
{
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		try {
			Map<String, String> myNameMap = UnstitchTGA.loadNameMap(getResources().openRawResource(R.raw.mapping));
			UnstitchTGA.unstitchTGA(new File("/sdcard/terrain-atlas.tga"), new File("/sdcard/terrain.meta"),
				new File("/sdcard/winprogress/terrainout"), myNameMap);
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
