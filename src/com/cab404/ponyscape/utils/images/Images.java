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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/**
 * @author cab404
 */
public class Images {
	private HashSet<String> loading;
	private Map<String, Reference<Bitmap>> cache;
	public static final String LIMIT_CFG_ENTRY = "images.pixel_limit";
	public static final String LOAD_BLOCK_CFG_ENTRY = "images.blocked";
	public static final String FILE_CACHE_LIMIT_CFG_ENTRY = "images.file_cache_limit";

	private File cacheDir;
	private long cut, file_cache;
	private boolean load_blocked;

	public static class CorruptedImageException extends RuntimeException {
		public CorruptedImageException(String detailMessage) {
			super(detailMessage);
		}
	}

	public Images(Context context, File cacheDir) {
		this.cacheDir = cacheDir;

		cache = new HashMap<>();
		loading = new HashSet<>();
	}

	public void reconfigure() {
		load_blocked = Static.cfg.ensure(LOAD_BLOCK_CFG_ENTRY, false);
		file_cache = Static.cfg.ensure(FILE_CACHE_LIMIT_CFG_ENTRY, 30L * 1024L * 1024L);
		cut = Static.cfg.ensure(LIMIT_CFG_ENTRY, 3000000L);

	}

	public synchronized void download(final String src) {
		if (load_blocked) {
			Static.bus.send(new DataAcquired.Image.Error(src));
			return;
		}

		/* Смотрим в кэше. */
		if (cache.containsKey(src) && cache.get(src).get() != null) {
			Log.v("ImageLoader", "Got " + src + " from cache");
			Static.bus.send(new DataAcquired.Image.Loaded(cache.get(src).get(), src));
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
					opt.inPurgeable = true;

					if (!file.exists()) {
						Log.v("ImageLoader", "Loading " + src);
						/// Connecting.

						HttpUriRequest get = new HttpGet(src);
						HttpResponse response = new DefaultHttpClient().execute(get);

//						int length = -1;
//						if (response.getFirstHeader("Content-Length") != null)
//							length = Integer.parseInt(response.getFirstHeader("Content-Length").getValue());


						Log.v("ImageLoader", "Got response for " + src);


						BufferedInputStream upstream = new BufferedInputStream(response.getEntity().getContent());
						upstream.mark(10 * 1024); // Should be enough for any mime type out there.

						/// Downloading bounds
						opt.inJustDecodeBounds = true;
						BitmapFactory.decodeStream(upstream, null, opt);

						Log.v("ImageLoader", "Loaded bounds for " + src + ", " + opt.outMimeType);


					/* Тут броадкастить данные картинки (?)*/
//					boolean cont = handler.handleParams(src, opt.outMimeType, opt.outWidth, opt.outHeight);

						if (opt.outWidth * opt.outHeight > cut)
							throw new IOException
									("Image is bigger than limit (" + cut + ")");

						opt.inJustDecodeBounds = false;

						// Rewinding back to start.
						upstream.reset();

						// Writing cache
						BufferedOutputStream file_cache = new BufferedOutputStream(new FileOutputStream(file));
						byte[] buf = new byte[16 * 1024]; // 16K should be enough for everything

						int read;
						int progress = 0;
						while ((read = upstream.read(buf)) > 0) {
							file_cache.write(buf, 0, read);
						}

						file_cache.close();
						upstream.close();
					} else
						Log.v("ImageLoader", "Getting " + src + " from file cache");


					// Reading saved image from file
					BufferedInputStream upstream = new BufferedInputStream(new FileInputStream(file));

					Bitmap bitmap = BitmapFactory.decodeStream(upstream, null, opt);

					/// Putting into file cache.
					if (bitmap == null) {
						if (!file.delete()) throw new RuntimeException("CANNOT REMOVE CACHE ENTRY " + file);
						Log.v("ImageLoader", "Bitmap from " + src + " is null. Cancelling.");
						return;
					}

					Static.bus.send(new DataAcquired.Image.Loaded(bitmap, src));


//					handler.handleBitmap(src, bitmap);

					// Используем крайние меры отлова бродячих ошибок.
				} catch (Throwable e) {
					Log.e("Images", "Не могу загрузить картинку из" + src, e);
					Static.bus.send(new DataAcquired.Image.Error(src));
				}
				loading.remove(src);

			}
		}).start();
	}

}
