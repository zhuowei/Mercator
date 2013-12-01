package net.zhuoweizhang.mercator;

import android.app.Activity;
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
	Map<String, String> myNameMap = new HashMap<String, String>();
	myNameMap.put("wool_0", "wool_colored_black");
        try {
		UnstitchTGA.unstitchTGA(new File("/sdcard/terrain-atlas.tga"), new File("/sdcard/terrain.meta"), new File("/sdcard/winprogress/terrainout"), myNameMap);
	} catch (Exception e) {
		throw new RuntimeException(e);
	}
    }
}
