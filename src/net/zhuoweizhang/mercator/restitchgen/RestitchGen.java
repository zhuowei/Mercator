package net.zhuoweizhang.mercator.restitchgen;

import java.io.File;
import java.io.IOException;
import android.graphics.Bitmap;

public abstract class RestitchGen {
	public abstract boolean forceGen(String fileName, File sourceDir);
	public abstract Bitmap restitchGen(String fileName, File sourceDir) throws IOException;
}
