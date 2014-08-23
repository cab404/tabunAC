package com.cab404.ponyscape.utils.images;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import com.cab404.moonlight.util.SU;
import com.cab404.ponyscape.bus.events.DataAcquired;
import com.cab404.ponyscape.utils.Simple;
import com.cab404.ponyscape.utils.Static;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.*;
import java.lang.ref.Reference;
import java.net.URISyntaxException;
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

	public static class CorruptedImageException extends RuntimeException {
		public CorruptedImageException(String detailMessage) {
			super(detailMessage);
		}
	}

	public Images(Context context, File cacheDir) {
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

					if (!file.exists()) {
						Log.v("ImageLoader", "Loading " + src);
						/// Connecting.

						HttpUriRequest get = new HttpGet(Simple.parse(src));
						HttpResponse response = new DefaultHttpClient().execute(get);

						// TODO: Добавить лимит по размеру изображения.
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
				} catch (IOException e) {
					Log.e("Images", "Не могу загрузить картинку из" + src, e);
					Static.bus.send(new DataAcquired.Image.Error(src));
				} catch (URISyntaxException e) {
					throw new RuntimeException(e);
				}
				loading.remove(src);

			}
		}).start();
	}

}
