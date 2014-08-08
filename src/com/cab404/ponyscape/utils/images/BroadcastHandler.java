package com.cab404.ponyscape.utils.images;

import android.graphics.Bitmap;

/**
 * @author cab404
 */
public class BroadcastHandler implements Images.BitmapHandler {
	@Override public void handleBitmap(String src, Bitmap bitmap) {

	}
	@Override public boolean handleParams(String src, String mime, int w, int h) {
		return false;
	}
	@Override public boolean handleLoadingProgress(String src, int bytes, int full_size) {
		return false;
	}
	@Override public void onFailure(String src, Throwable err) {

	}
}
