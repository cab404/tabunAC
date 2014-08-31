package com.cab404.ponyscape.utils.images;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Rect;
import android.util.Log;

import static android.graphics.Color.*;

/**
 * @author cab404
 */
public class BitmapMorph {

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
				int q_level = level * level;
				int a = 0, r = 0, g = 0, b = 0;

				for (int rx = -level; rx <= level; rx++)
					for (int ry = -level; ry <= level; ry++)
						if (x + rx >= 0 && x + rx < w && y + ry >= 0 && y + ry < h) {
							if (rx * rx + ry * ry <= q_level) {
								count++;
								int c = bitmap.getPixel(x + rx, y + ry);

								a += c >>> 24;
								r += c >>> 16 & 0xFF;
								g += c >>> 8 & 0xFF;
								b += c & 0xFF;

							}
						}

				if (count == 0) continue;

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

		int pxpow = (int) Math.pow(pixels, 2);
		int[][] sx_s = {{0, pixels, 1}, {w - pixels - 1, w - 1, 0}}, sy_s = {{0, pixels, 1}, {h - pixels - 1, h - 1, 0}};

		for (int[] arr_d_x : sx_s)
			for (int[] arr_d_y : sy_s) {

				int sx = arr_d_x[0];
				int sy = arr_d_y[0];
				int ox = arr_d_x[arr_d_x[2]];
				int oy = arr_d_y[arr_d_y[2]];


				for (int x = sx; x <= arr_d_x[1]; x++)
					for (int y = sy; y <= arr_d_y[1]; y++) {

						int len = (ox - x) * (ox - x) + (oy - y) * (oy - y);

						float squared = (float) Math.sqrt(len);

						if (squared > pixels)
							bitmap.setPixel(x, y, scaleAlpha(bitmap.getPixel(x, y), squared - pixels > 1 ? 0 : 1 - (squared - pixels)));

					}

			}

		return bitmap;
	}

	public static Bitmap background(Bitmap source, int color) {
		int alpha_2 = alpha(color);
		if (alpha_2 == 0) return source;

		for (int x = 0; x < source.getWidth(); x++)
			for (int y = 0; y < source.getHeight(); y++) {
				int sc = source.getPixel(x, y);
				int alpha_1 = alpha(sc);

				if (alpha_1 == 0) {
					source.setPixel(x, y, color);
					continue;
				}

				if (alpha_1 == 0xFF) {
					continue;
				}

				float sum = alpha_1 + alpha_2;
				float mul1 = alpha_1 / sum;
				float mul2 = alpha_2 / sum;

				int a = color >>> 24;
				int r = (int) (red(color) * mul2 + red(sc) * mul1);
				int g = (int) (green(color) * mul2 + green(sc) * mul1);
				int b = (int) (blue(color) * mul2 + blue(sc) * mul1);

				source.setPixel(x, y, Color.argb(a, r, g, b));

			}
		return source;
	}

	private static int scaleColor(int color, float value) {
		return Color.argb(
				((int) (((color & 0xff000000) >>> 24) * value) << 24),
				((int) (((color & 0x00ff0000) >>> 16) * value) << 16),
				((int) (((color & 0x0000ff00) >>> 8) * value) << 8),
				((int) (((color & 0x000000ff)) * value))
		);
	}

	private static int scaleAlpha(int color, float value) {
		return ((color | 0xff000000) ^ 0xff000000) | ((int) (((color & 0xff000000) >>> 24) * value) << 24);
	}

	private static int mix(int c1, int c2) {
		return Color.argb(
				(alpha(c1) + alpha(c2)) / 2,
				(red(c1) + red(c2)) / 2,
				(green(c1) + green(c2)) / 2,
				(blue(c1) + blue(c2)) / 2
		);
	}

	public static Bitmap cut(Bitmap source, Rect rect) {
		Bitmap bitmap = Bitmap.createBitmap(rect.right - rect.left, rect.bottom - rect.top, Bitmap.Config.ARGB_8888);

		for (int x = rect.left; x < rect.right; x++)
			for (int y = rect.top; y < rect.bottom; y++)
				if (x >= 0 && x < source.getWidth())
					if (y >= 0 && y < source.getHeight())
						bitmap.setPixel(x - rect.left, y - rect.top, source.getPixel(x, y));

		return bitmap;
	}

	public static Bitmap manualCopy(Bitmap loaded) {
		Bitmap bitmap = Bitmap.createBitmap(loaded.getWidth(), loaded.getHeight(), Bitmap.Config.ARGB_8888);

		for (int x = 0; x < bitmap.getWidth(); x++)
			for (int y = 0; y < bitmap.getHeight(); y++)
				bitmap.setPixel(x, y, loaded.getPixel(x, y));

		return bitmap;
	}

	public static Bitmap tint(Bitmap bitmap, int tint) {
		for (int x = 0; x < bitmap.getWidth(); x++)
			for (int y = 0; y < bitmap.getHeight(); y++)
				bitmap.setPixel(x, y, mix(tint, bitmap.getPixel(x, y)));

		return bitmap;
	}
}
