package com.cab404.ponyscape.utils.images;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import com.cab404.moonlight.util.SU;
import com.cab404.ponyscape.bus.events.DataAcquired;
import com.cab404.ponyscape.utils.Static;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.*;
import java.lang.ref.Reference;
import java.net.URI;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/**
 * @author cab404
 */
public class Images {
	private HashSet<String> loading;
	private Map<String, Reference<Bitmap>> cache;
	private File cacheDir;
	private int cut;
	private Context context;

	public interface BitmapHandler {

		public void handleBitmap(String src, Bitmap bitmap);
		/**
		 * @param src  Адрес изображения
		 * @param mime Тип изображения
		 * @param w    Ширина изображения
		 * @param h    Высота изображения
		 * @return Загружать ли картинку дальше.
		 * <p/>
		 * Вызывается при получении типа изображения.
		 */
		public boolean handleParams(String src, String mime, int w, int h);
		/**
		 * Сюда передаётся всё о загрузке.
		 */
		public boolean handleLoadingProgress(String src, int bytes, int full_size);
		/**
		 * @param src Адрес изображения
		 * @param err Ошибка, с которой вылетел загрузщик.
		 *            <p/>
		 */
		public void onFailure(String src, Throwable err);
	}

	public static class CorruptedImageException extends RuntimeException {
		public CorruptedImageException(String detailMessage) {
			super(detailMessage);
		}
	}

	public Images(Context context, File cacheDir) {
		this.context = context;
		this.cacheDir = cacheDir;
		cache = new HashMap<>();
		loading = new HashSet<>();
		cut = Math.max(
				context.getResources().getDisplayMetrics().widthPixels,
				context.getResources().getDisplayMetrics().heightPixels
		);
		cut *= cut;
	}


	public synchronized void download(final String src) {

		Log.v("ImageLoader", "Requested " + src);
		/* Смотрим в кэше. */
		if (cache.containsKey(src) && cache.get(src).get() != null) {
			Log.v("ImageLoader", "Got " + src + " from cache");
			Static.bus.send(new DataAcquired.ImageLoaded(cache.get(src).get(), src));
			return;
		}

		/* Нету в кэше, смотрим в загружаемых.*/
		if (loading.contains(src)) return;
		Log.v("ImageLoader", "For " + src + " was no loaders invoked.");

		/* Нету в загружаемых, добавляем и загружаем. */
		loading.add(src);
		new Thread(new Runnable() {
			@Override public void run() {
				try {

					File file = new File(cacheDir, SU.rl(src));
					BitmapFactory.Options opt = new BitmapFactory.Options();

					if (!file.exists()) {
						Log.v("ImageLoader", "Loading " + src);
						/// Connecting.

						HttpUriRequest get = new HttpGet(URI.create(src));
						HttpResponse response = new DefaultHttpClient().execute(get);

						int length = -1;
						if (response.getFirstHeader("Content-Length") != null)
							length = Integer.parseInt(response.getFirstHeader("Content-Length").getValue());

						Log.v("ImageLoader", "Got response for " + src);


						BufferedInputStream upstream = new BufferedInputStream(response.getEntity().getContent());
						upstream.mark(10 * 1024); // Should be enough for any mime type out there.

						/// Downloading bounds
						opt.inJustDecodeBounds = true;
						BitmapFactory.decodeStream(upstream, null, opt);

						Log.v("ImageLoader", "Loaded bounds for " + src);

					/* Тут броадкастить данные картинки (?)*/
//					boolean cont = handler.handleParams(src, opt.outMimeType, opt.outWidth, opt.outHeight);

						if (opt.outWidth * opt.outHeight > cut)
							opt.inSampleSize = opt.outWidth * opt.outHeight / cut;
						opt.inJustDecodeBounds = false;

						// Rewinding back to start.
						upstream.reset();


						// Writing cache
						BufferedOutputStream file_cache = new BufferedOutputStream(new FileOutputStream(file));
						byte[] buf = new byte[16 * 1024]; // 16K should be enough for buffer

						int read;
						int progress = 0;
						while ((read = upstream.read(buf)) > 0) {
							file_cache.write(buf, 0, read);
						}

						file_cache.close();
						upstream.close();
					}

					// Reading saved image from file
					BufferedInputStream upstream = new BufferedInputStream(new FileInputStream(file));

					Bitmap bitmap = BitmapFactory.decodeStream(upstream, null, opt);

					/// Putting into file cache.
					if (bitmap == null) {
						if (!file.delete()) throw new RuntimeException("CANNOT REMOVE CACHE ENTRY " + file);
						Log.v("ImageLoader", "Bitmap from " + src + " is null. Cancelling.");
						return;
					}

					Static.bus.send(new DataAcquired.ImageLoaded(bitmap, src));


//					handler.handleBitmap(src, bitmap);
				} catch (IOException e) {
					Log.e("Images", "Не могу загрузить картинку.", e);

				}
				loading.remove(src);

			}
		}).start();
	}

}
