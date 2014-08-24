package com.cab404.ponyscape.utils.images;

import android.graphics.Bitmap;
import android.util.Log;

/**
 * @author cab404
 */
public class BitmapUtils {

	/**
	 * Использует двойную буферизацию
	 */
	public static Bitmap blur(Bitmap bitmap, int level) {
		int w = bitmap.getWidth();
		int h = bitmap.getHeight();

		long time_start = System.currentTimeMillis();
		Log.v("TestBlur", "Starting blur on " + w + ":" + h + ", " + w * h + " pixels total.");
		final Bitmap blurred = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);

		for (int x = 0; x < w; x++)
			for (int y = 0; y < h; y++) {

				int count = 0;
				int a = 0, r = 0, g = 0, b = 0;

				for (int rx = -level; rx <= level; rx++)
					for (int ry = -level; ry <= level; ry++)
						if (x + rx >= 0 && x + rx < w && y + ry >= 0 && y + ry < h) {
							count++;
							int c = bitmap.getPixel(x + rx, y + ry);

							a += c >>> 24;
							r += c >>> 16 & 0xFF;
							g += c >>> 8 & 0xFF;
							b += c & 0xFF;

						}

				a /= count;
				r /= count;
				g /= count;
				b /= count;

				blurred.setPixel(x, y, a << 24 | r << 16 | g << 8 | b);

			}

		Log.v("TestBlur", "Finished blur on " + w + ":" + h + ", " + w * h + " pixels total in " + (System.currentTimeMillis() - time_start) + "ms");

		return blurred;
	}

	public static Bitmap bevel(Bitmap bitmap, int pixels) {
		int w = bitmap.getWidth();
		int h = bitmap.getHeight();

		pixels = pixels > Math.min(w, h) / 2 ? Math.min(w, h) / 2 : pixels;

		Log.v("Pixels", "PS" + pixels);

		int pxpow = (int) Math.pow(pixels, 2);
		int sx = pixels, sy = pixels;
		for (int x = 0; x <= pixels; x++)
			for (int y = 0; y <= pixels; y++)
				if (Math.pow(sx - x, 2) + Math.pow(sy - y, 2) > pxpow)
					bitmap.setPixel(x, y, 0);

		sy = h - pixels - 1;
		for (int x = 0; x <= pixels; x++)
			for (int y = sy; y < h; y++)
				if (Math.pow(sx - x, 2) + Math.pow(sy - y, 2) > pxpow)
					bitmap.setPixel(x, y, 0);

		sx = w - pixels - 1;
		for (int x = sx; x < h; x++)
			for (int y = h - pixels - 1; y < h; y++)
				if (Math.pow(sx - x, 2) + Math.pow(sy - y, 2) > pxpow)
					bitmap.setPixel(x, y, 0);

		sy = pixels;
		for (int x = sx; x < h; x++)
			for (int y = 0; y <= pixels; y++)
				if (Math.pow(sx - x, 2) + Math.pow(sy - y, 2) > pxpow)
					bitmap.setPixel(x, y, 0);

		return bitmap;
	}

}
