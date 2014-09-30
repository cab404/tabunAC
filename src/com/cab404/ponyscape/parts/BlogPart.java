package com.cab404.ponyscape.parts;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.cab404.acli.Part;
import com.cab404.libtabun.data.Blog;
import com.cab404.moonlight.util.SU;
import com.cab404.ponyscape.R;
import com.cab404.ponyscape.bus.E;
import com.cab404.ponyscape.utils.Static;
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
	public void handleTitleImage(final E.GotData.Image.Loaded img) {
		if (img.src.equals(blog.icon)) {
			Static.handler.post(new Runnable() {
				public void run() {
					((ImageView) view.findViewById(R.id.icon)).setImageBitmap(img.loaded);
				}
			});
		}
	}

	View view;
	@Override protected View create(LayoutInflater inflater, ViewGroup viewGroup, Context context) {
		Static.bus.register(this);

		view = inflater.inflate(R.layout.part_blog, viewGroup, false);


		((TextView) view.findViewById(R.id.title)).setText(SU.deEntity(blog.name));
		if (blog.id != -1)
			view.findViewById(R.id.create).setOnClickListener(new View.OnClickListener() {
				@Override public void onClick(View v) {
					Static.bus.send(new E.Commands.Run("post write " + blog.id));
				}
			});
		else
			view.findViewById(R.id.create).setVisibility(View.GONE);

		Static.img.download(blog.icon);

		return view;
	}

	@Override protected void onRemove(View view, ViewGroup parent, Context context) {
		Static.bus.unregister(this);
		super.onRemove(view, parent, context);
	}
}
