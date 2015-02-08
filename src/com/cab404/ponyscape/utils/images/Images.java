package com.cab404.ponyscape.utils.images;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.util.SparseArray;
import com.cab404.moonlight.util.SU;
import com.cab404.ponyscape.bus.E;
import com.cab404.ponyscape.utils.Keys;
import com.cab404.ponyscape.utils.Simple;
import com.cab404.ponyscape.utils.Static;
import com.cab404.ponyscape.utils.text.DateUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.simple.JSONArray;

import java.io.*;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.*;

/**
 * @author cab404
 */
public class Images {
    private HashSet<String> loading;
    private Map<String, Reference<Bitmap>> memory_cache;
    private Map<String, SparseArray<Reference<Bitmap>>> scaled;
    public static final String DOWNSCALE_IMAGES_CFG_ENTRY = "images.downscale";

    private File cacheDir;
    private long cut, file_cache;
    private boolean load_blocked;


    public boolean log = false;

    private void log(String string) {
        if (log)
            log(string);
    }

    public static class CorruptedImageException extends RuntimeException {
        public CorruptedImageException(String detailMessage) {
            super(detailMessage);
        }
    }

    public Images(Context context, File cacheDir) {
        this.cacheDir = cacheDir;

        memory_cache = new HashMap<>();
        scaled = new HashMap<>();
        loading = new HashSet<>();
    }

    public void reconfigure() {
        load_blocked = Static.cfg.ensure(Keys.IMAGES_BLOCKED, false);
        file_cache = Static.cfg.ensure(Keys.IMAGES_FILE_CACHE_LIMIT, 30L * 1024L * 1024L);
        cut = Static.cfg.ensure(Keys.IMAGES_PIXEL_LIMIT, 3L * 1024L * 1024L);

        Static.cfg.ensure(Keys.IMAGES_DOWNSCALE, true);

        clear();
    }

    public synchronized Bitmap scale(E.GotData.Image.Loaded loaded, int w, int h) {
        /* NOPENOPENOPENOPENOPENOPE */
        if (w == 0 || h == 0) return loaded.loaded;

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

    /**
     * Cleaning file cache down to the maximum size.
     */
    public void clear() {
        List<File> files = new ArrayList<>(Arrays.asList(cacheDir.listFiles()));
        Collections.sort(files, new Comparator<File>() {
            @Override
            public int compare(File lhs, File rhs) {
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
                log("Удаляю " + file.getName() + ", последнее изменение " + DateUtils.convertToString(instance, Static.ctx));
                if (!file.delete())
                    Log.wtf("Images", "Не удалось удалить изображение из кэша!");
                else
                    limit -= file.length();
            }
        }

        Log.v("ImagesLoader", "Очистка выполнена, в кэше оставлено " + limit + " байт картинок из " + file_cache + " разрешенных.");
    }

    public synchronized void download(String in) {
		/* Copying original url, so we'll be able to send image to whom requested */
        final String original_url = in;

		/* If images are BLOCKED, then we can do nothing and just give up. */
        if (load_blocked) {
            Static.bus.send(new E.GotData.Image.Error(original_url));
            return;
        }

		/* Fixing address */
        if (in.startsWith("//"))
            in = "http:" + in;
        if (in.contains("poniez.net"))
            in = "http://andreymal.org/poniez/?q=" + SU.rl(in);
        in = in
                .replace("[", "%5B")
                .replace("]", "%5D");

        final String src = in;

		/* File cache... file */
        final File file_cache_entry = new File(cacheDir, Simple.md5(src));

		/* Checking blacklists */
        JSONArray blocked_patterns = Static.cfg.ensure(Keys.IMAGES_BLOCKED_PATTERNS, new JSONArray());
        for (Object object : blocked_patterns) {
            String str = String.valueOf(object);
            if (SU.fast_match(str, in)) {
                Static.bus.send(new E.GotData.Image.Error(original_url));
                return;
            }
        }


		/* Looking for entry in memory cache. */
        if (memory_cache.containsKey(src) && memory_cache.get(src).get() != null) {
            log("Got " + src + " from cache");
            Static.bus.send(new E.GotData.Image.Loaded(memory_cache.get(src).get(), original_url));
            return;
        }

        try {
			/* We don't care about network, if there's an entry in file cache. */
            if (!file_cache_entry.exists())
                if (Static.cfg.ensure(Keys.IMAGES_ONLY_OVER_NON_CELLULAR, false))
                    Simple.checkNonCellularConnection();
                else
                    Simple.checkNetworkConnection();
        } catch (Simple.NetworkNotFound e) {
            Static.bus.send(new E.GotData.Image.Error(original_url));
            return;
        }

		/* No cache entry, adding and downloading */
        if (loading.contains(src)) {
            log("For " + src + " was no loaders invoked.");
            return;
        }

		/* No entry in loading list, so we'll just start it on our own */
        loading.add(src);
        Runnable load_task = new Runnable() {
            @Override
            public void run() {
                try {

                    BitmapFactory.Options opt = new BitmapFactory.Options();
                    opt.inPurgeable = true;

					/* If we already have an image in file cache, then buck it all */
                    if (!file_cache_entry.exists()) {
                        log("Loading " + src);
                        /// Connecting.

                        HttpUriRequest get = new HttpGet(src);
                        HttpResponse response = new DefaultHttpClient().execute(get);

                        int length = -1;
                        if (response.getFirstHeader("Content-Length") != null)
                            length = Integer.parseInt(response.getFirstHeader("Content-Length").getValue());
                        BufferedInputStream upstream = new BufferedInputStream(response.getEntity().getContent());
                        log("Got response for " + src);

						/* Decoding and processing metadata */
                        {
							/* Nobody will ever hate us for allocating 50K for meta... yeah? */
                            int max_meta = 50 * 1024;
							/* Just making sure that meta is shorter than image ;D */
                            upstream.mark(length != -1 ? length > max_meta ? max_meta : length : max_meta);

							/* Downloading metadata of an image */
                            opt.inJustDecodeBounds = true;
                            BitmapFactory.decodeStream(upstream, null, opt);

                            log("Loaded bounds for " + src + ", " + opt.outMimeType);

							/*
							 * BROADCAST-YOUR-META-HERE (?)
							 */

                            if (opt.outWidth * opt.outHeight > cut)
                                throw new IOException
                                        ("Image is bigger than limit (" + cut + ")");

                            opt.inJustDecodeBounds = false;

							/* Rewinding stream back to the start. */
                            upstream.reset();
                        }

						/* Writing it all to file */
                        {
							/* 16K should be enough for everything */
                            byte[] buf = new byte[16 * 1024];
                            int read;

                            BufferedOutputStream file_cache = new BufferedOutputStream(new FileOutputStream(file_cache_entry));
                            while ((read = upstream.read(buf)) > 0) {
                                file_cache.write(buf, 0, read);
                            }

                            file_cache.close();

                        }

                        upstream.close();
                    } else
                        log("Getting " + src + " from file cache");

					/* Reading saved image from file */
                    BufferedInputStream upstream = new BufferedInputStream(new FileInputStream(file_cache_entry));

					/* Touching the file, so cache cleaning system think we've just downloaded it. */
                    if (!file_cache_entry.setLastModified(System.currentTimeMillis()))
                        Log.wtf("Images", "I can't modify the timestream in file cache. How unfortunate.");

                    Bitmap bitmap = BitmapFactory.decodeStream(upstream, null, opt);

                    upstream.close();

					/* If bitmap is null, then something went wrong, and it's our duty to delete that thing. */
                    if (bitmap == null) {
                        if (!file_cache_entry.delete())
                            throw new RuntimeException("CANNOT REMOVE CACHE ENTRY " + file_cache_entry);
                        log("Bitmap from " + src + " is null. Cancelling.");
                        return;
                    }

                    memory_cache.put(original_url, new WeakReference<>(bitmap));
                    Static.bus.send(new E.GotData.Image.Loaded(bitmap, original_url));

					/* Anything may happen in high-memory-consumption-web-image-byte-stream-play procedure*/
                } catch (Throwable e) {
                    Log.e("Images", "Не могу загрузить картинку из " + src, e);
                    Static.bus.send(new E.GotData.Image.Error(original_url));
                }
                loading.remove(src);

            }
        };

        Static.pools.img_load.execute(load_task);

    }

}
