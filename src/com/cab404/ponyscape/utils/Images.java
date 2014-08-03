package com.cab404.ponyscape.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import com.cab404.moonlight.util.SU;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.*;
import java.lang.ref.Reference;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author cab404
 */
public class Images {
	private Map<String, List<BitmapHandler>> loaders;
	private Map<String, Reference<Bitmap>> cache;
	private File cacheDir;
	private int cut;
	private Context context;

	public interface BitmapHandler {
		public void handleBitmap(String src, Bitmap bitmap);
		public void handleMime(String src, String mime);
		public void onFailure(String src);
	}

	public Images(Context context) {
		this.context = context;
		cacheDir = context.getCacheDir();
		loaders = new HashMap<>();
		cache = new HashMap<>();
		cut = Math.max(
				context.getResources().getDisplayMetrics().widthPixels,
				context.getResources().getDisplayMetrics().heightPixels
		);
		cut *= cut;
	}


	public void download(final String src, final BitmapHandler handler) {
		new Thread(new Runnable() {
			@Override public void run() {
				try {

					/// Connecting.

					HttpUriRequest get = new HttpGet(URI.create(src));
					HttpResponse response = new DefaultHttpClient().execute(get);

					File file = new File(cacheDir, SU.rl(src));

					InputStream stream = new BufferedInputStream(response.getEntity().getContent());

					/// Downloading bounds

					BitmapFactory.Options opt = new BitmapFactory.Options();
					opt.inJustDecodeBounds = true;
					BitmapFactory.decodeStream(stream, null, opt);
					handler.handleMime(src, opt.outMimeType);


					if (opt.outWidth * opt.outHeight > cut)
						opt.inSampleSize = opt.outWidth * opt.outHeight / cut;
					opt.inJustDecodeBounds = false;

					Bitmap bitmap = BitmapFactory.decodeStream(stream, null, opt);

					/// Putting into file cache.
					bitmap.compress(
							Bitmap.CompressFormat.PNG,
							100,
							new BufferedOutputStream(new FileOutputStream(file))
					);

					handler.handleBitmap(src, bitmap);
				} catch (IOException e) {
					handler.onFailure(src);
				}

			}
		}).start();
	}
}
