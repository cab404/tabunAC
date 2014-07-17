package com.cab404.ponyscape.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.File;

/**
 * @author cab404
 */
public class Images {
	File cacheDir;

	public interface BitmapHandler {
		public void handleBitmap(Bitmap bitmap);
		public void handleMime(String mime);
	}

	public Images(Context context) {
		cacheDir = context.getCacheDir();
	}

	public static void load(final String src, final BitmapHandler handler) {
		new Thread(new Runnable() {
			@Override public void run() {

				BitmapFactory.Options op = new BitmapFactory.Options();
				op.inJustDecodeBounds = true;




			}
		}).start();
	}
}
