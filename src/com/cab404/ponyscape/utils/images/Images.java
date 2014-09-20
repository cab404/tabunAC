package com.cab404.ponyscape.utils.images;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.util.SparseArray;
import com.cab404.ponyscape.bus.events.GotData;
import com.cab404.ponyscape.utils.text.DateUtils;
import com.cab404.ponyscape.utils.Simple;
import com.cab404.ponyscape.utils.Static;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.*;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.*;

/**
 * @author cab404
 */
public class Images {
	private HashSet<String> loading;
	private Map<String, Reference<Bitmap>> cache;
	private Map<String, SparseArray<Reference<Bitmap>>> scaled;
	public static final String LIMIT_CFG_ENTRY = "images.pixel_limit";
	public static final String LOAD_BLOCK_CFG_ENTRY = "images.blocked";
	public static final String FILE_CACHE_LIMIT_CFG_ENTRY = "images.file_cache_limit";
	public static final String DOWNSCALE_IMAGES_CFG_ENTRY = "images.downscale";

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
		scaled = new HashMap<>();
		loading = new HashSet<>();
	}

	public void reconfigure() {
		load_blocked = Static.cfg.ensure(LOAD_BLOCK_CFG_ENTRY, false);
		file_cache = Static.cfg.ensure(FILE_CACHE_LIMIT_CFG_ENTRY, 30L * 1024L * 1024L);
		cut = Static.cfg.ensure(LIMIT_CFG_ENTRY, 3L * 1024L * 1024L);

		Static.cfg.ensure(DOWNSCALE_IMAGES_CFG_ENTRY, true);

		clear();
	}

	public synchronized Bitmap scale(GotData.Image.Loaded loaded, int w, int h) {
		if (scaled.containsKey(loaded.src))
			scaled.put(loaded.src, new SparseArray<Reference<Bitmap>>());

		SparseArray<Reference<Bitmap>> versions = scaled.get(loaded.src);
		final int encoded_size = (w << 16) | h;

		if (versions == null) {
			versions = new SparseArray<>();
			scaled.put(loaded.src, versions);
		}
		if (versions.get(encoded_size) != null)
			return versions.get(encoded_size).get();

		final Bitmap scaled = Bitmap.createScaledBitmap(loaded.loaded, w, h, true);
		versions.put(encoded_size, new WeakReference<>(scaled));
		return scaled;
	}

	public void clear() {
		List<File> files = new ArrayList<>(Arrays.asList(cacheDir.listFiles()));
		Collections.sort(files, new Comparator<File>() {
			@Override public int compare(File lhs, File rhs) {
				return (int) Math.signum(rhs.lastModified() - lhs.lastModified());
			}
		});
		long limit = 0;

		for (File file : files) {
			limit += file.length();
			if (limit > file_cache) {
				Calendar instance = Calendar.getInstance();
				instance.clear();
				instance.setTimeInMillis(file.lastModified());
				Log.v("Images", "Удаляю " + file.getName() + ", последнее изменение " + DateUtils.convertToString(instance, Static.app_context));
				if (!file.delete())
					Log.wtf("Images", "Не удалось удалить изображение из кэша!");
				else
					limit -= file.length();
			}
		}

		Log.v("Images", "Очистка выполнена, в кэше оставлено " + limit + " байт картинок из " + file_cache + " разрешенных.");
	}

	public synchronized void download(final String src) {
		if (load_blocked) {
			Static.bus.send(new GotData.Image.Error(src));
			return;
		}

		/* Смотрим в кэше. */
		if (cache.containsKey(src) && cache.get(src).get() != null) {
			Log.v("ImageLoader", "Got " + src + " from cache");
			Static.bus.send(new GotData.Image.Loaded(cache.get(src).get(), src));
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

					File file = new File(cacheDir, Simple.md5(src));
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

					Static.bus.send(new GotData.Image.Loaded(bitmap, src));


//					handler.handleBitmap(src, bitmap);

					// Используем крайние меры отлова бродячих ошибок.
				} catch (Throwable e) {
					Log.e("Images", "Не могу загрузить картинку из" + src, e);
					Static.bus.send(new GotData.Image.Error(src));
				}
				loading.remove(src);

			}
		}).start();
	}

}
