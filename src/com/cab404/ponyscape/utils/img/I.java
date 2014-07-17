package com.cab404.ponyscape.utils.img;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.Reference;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

/**
 * ImageLoader, the second.
 *
 * @author cab404
 */
public class I {

	private final Map<String, Reference<Bitmap>> linked_cache;

	private final int max_pixel_size;

	public I(int max_pixel_size) {
		this.max_pixel_size = max_pixel_size;
		linked_cache = new HashMap<>();
	}

	public void load(final String address, final ImageHandler handler) {

		synchronized (linked_cache) {
			Reference<Bitmap> reference = linked_cache.get(address);
			if (reference != null && reference.get() != null) {
				handler.onSuccess(reference.get());
			} else {
				linked_cache.remove(address);
			}

		}

		new Thread(new Runnable() {
			@Override public void run() {
				try {

					HttpUriRequest get = new HttpGet(URI.create(address));
					HttpResponse response = new DefaultHttpClient().execute(get);



					InputStream stream = new BufferedInputStream(response.getEntity().getContent());

					BitmapFactory.Options opt = new BitmapFactory.Options();
					opt.inJustDecodeBounds = true;
					BitmapFactory.decodeStream(stream, null, opt);

					if (opt.outWidth * opt.outHeight > max_pixel_size)
						opt.inSampleSize = opt.outWidth * opt.outHeight / max_pixel_size;
					opt.inJustDecodeBounds = false;

					Bitmap bitmap = BitmapFactory.decodeStream(stream, null, opt);

					handler.onSuccess(bitmap);

				} catch (IOException e) {
					handler.onFailure(e);
				}
			}
		}).start();
	}


}
