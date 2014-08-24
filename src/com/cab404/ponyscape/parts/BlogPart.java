package com.cab404.ponyscape.parts;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.cab404.acli.Part;
import com.cab404.libtabun.data.Blog;
import com.cab404.moonlight.util.SU;
import com.cab404.ponyscape.R;
import com.cab404.ponyscape.bus.events.DataAcquired;
import com.cab404.ponyscape.utils.Static;
import com.cab404.ponyscape.utils.images.BitmapUtils;
import com.cab404.sjbus.Bus;

/**
 * Часть, в которой находится всякая инфа о блоге.
 *
 * @author cab404
 */
public class BlogPart extends Part {

	final Blog blog;

	public BlogPart(Blog blog) {this.blog = blog;}

	@Bus.Handler
	public void handleTitleImage(DataAcquired.Image.Loaded img) {
		if (img.src.equals(blog.icon)) {
			int w = img.loaded.getWidth();
			int h = img.loaded.getHeight();

			long time_start = System.currentTimeMillis();
			Log.v("TestBlur", "Starting blur on " + w + ":" + h + ", " + w * h + " pixels total.");
			final Bitmap blurred = BitmapUtils.bevel(BitmapUtils.blur(img.loaded, 0), 8);
			Log.v("TestBlur", "Finished blur on " + w + ":" + h + ", " + w * h + " pixels total in " + (System.currentTimeMillis() - time_start) + "ms");

			Static.handler.post(new Runnable() {
				public void run() {
					((ImageView) view.findViewById(R.id.icon)).setImageBitmap(blurred);
				}
			});

		}
	}
	View view;
	@Override protected View create(LayoutInflater inflater, ViewGroup viewGroup, Context context) {
		Static.bus.register(this);

		view = inflater.inflate(R.layout.part_blog, viewGroup, false);

		((TextView) view.findViewById(R.id.title)).setText(SU.deEntity(blog.name));

		Static.img.download(blog.icon);

		return view;
	}

	@Override protected void onRemove(View view, ViewGroup parent, Context context) {
		Static.bus.unregister(this);
		super.onRemove(view, parent, context);
	}
}
