package com.cab404.ponyscape.parts;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.cab404.acli.Part;
import com.cab404.libtabun.data.Profile;
import com.cab404.ponyscape.R;
import com.cab404.ponyscape.bus.events.DataAcquired;
import com.cab404.ponyscape.utils.Static;
import com.cab404.ponyscape.utils.images.BitmapMorph;
import com.cab404.sjbus.Bus;

/**
 * @author cab404
 */
public class ProfilePart extends Part {

	View view;
	private final Profile profile;

	public ProfilePart(Profile profile) {
		this.profile = profile;
	}
	Bitmap bg;


	@Bus.Handler
	public void handleImages(final DataAcquired.Image.Loaded image) {


		if (image.src.equals(profile.big_icon))
			new Thread(new Runnable() {
				@Override public void run() {
					Bitmap loaded = image.loaded;

					final Bitmap bitmap = BitmapMorph.bevel(BitmapMorph.manualCopy(loaded), 16);

					Static.handler.post(
							new Runnable() {
								public void run() {
									((ImageView) view.findViewById(R.id.avatar)).setImageBitmap(bitmap);
								}
							}
					);
				}
			}).start();


		if (image.src.equals(profile.photo)) {
			new Thread(new Runnable() {
				@Override public void run() {
					Bitmap loaded = image.loaded;

					final ImageView bg_view = (ImageView) view.findViewById(R.id.background);
					bg = image.loaded;

					int width = bg.getWidth();
					int height = (int) (bg.getWidth() * ((float) bg_view.getHeight() / bg_view.getWidth()));

					int y = (loaded.getHeight() - height) / 2;

					final Bitmap bitmap = BitmapMorph.blur(BitmapMorph.cut(loaded, new Rect(0, y, width, y + height)), 3);

					Static.handler.post(
							new Runnable() {
								public void run() {
									bg_view.setImageBitmap(bitmap);
									bg_view.setColorFilter(0x77000000, PorterDuff.Mode.DARKEN);
								}
							}
					);
				}
			}).start();

		}

	}


	@Override protected View create(LayoutInflater inflater, ViewGroup viewGroup, Context context) {
		view = inflater.inflate(R.layout.part_user_info, viewGroup, false);
		Static.bus.register(this);

		profile.big_icon = "http://tabun.everypony.ru/uploads/images/00/00/07/2012/11/01/avatar_100x100.png?095631";
		((TextView) view.findViewById(R.id.nick)).setText(profile.login);
		((TextView) view.findViewById(R.id.name)).setText(profile.name);

		profile.fillImages();

		Log.v("PROFILE", "asdf");
		Log.v("PROFILE", "" + profile.big_icon + " ICO");

		Static.img.download(profile.photo);
		Static.img.download(profile.big_icon);

		return view;
	}

	@Override protected void onRemove(View view, ViewGroup parent, Context context) {
		Static.bus.unregister(this);
		super.onRemove(view, parent, context);
	}
}
