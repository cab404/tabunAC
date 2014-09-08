package com.cab404.ponyscape.parts;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Rect;
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
import com.cab404.ponyscape.utils.ViewSugar;
import com.cab404.ponyscape.utils.images.BitmapMorph;
import com.cab404.sjbus.Bus;

/**
 * @author cab404
 */
public class ProfilePart extends Part {

	View view;
	@ViewSugar.Bind(R.id.strength)
	TextView powah;
	@ViewSugar.Bind(R.id.rating)
	TextView ratin;

	private final Profile profile;

	public ProfilePart(Profile profile) {
		this.profile = profile;
	}


	@Bus.Handler
	public void handleImages(final DataAcquired.Image.Loaded image) {

		if (image.src.equals(profile.big_icon)) {
			new Thread(new Runnable() {
				@Override public void run() {
					Bitmap loaded = image.loaded;
					final ImageView avatar = (ImageView) view.findViewById(R.id.avatar);

					int bevel = getContext().getResources().getDimensionPixelSize(R.dimen.corner_cut);

					final Bitmap bitmap = BitmapMorph.bevel(
							BitmapMorph.background(
									BitmapMorph.manualCopy(
											Bitmap.createScaledBitmap(
													image.loaded,
													avatar.getWidth(),
													avatar.getHeight(),
													true
											)
									),
									0x44ffffff),
							bevel);

					Static.handler.post(
							new Runnable() {
								public void run() {
									avatar.setImageBitmap(bitmap);
								}
							}
					);
				}
			}).start();
		}


		if (image.src.equals(profile.photo)) {
			new Thread(new Runnable() {
				@Override public void run() {
					final ImageView bg_view = (ImageView) view.findViewById(R.id.background);
					Bitmap bg;

					bg = image.loaded;

					int width = bg.getWidth();
					int height = (int) (bg.getWidth() * ((float) bg_view.getHeight() / bg_view.getWidth()));
					int y = (bg.getHeight() - height) / 2;
					int bevel = getContext().getResources().getDimensionPixelSize(R.dimen.corner_cut);

					bg =
							BitmapMorph.bevel(
									Bitmap.createScaledBitmap(
											BitmapMorph.blur(
													BitmapMorph.tint(
															BitmapMorph.cut(
																	bg,
																	new Rect(0, y, width, y + height)
															),
															0xff000000
													),
													3
											),
											bg_view.getWidth(),
											bg_view.getHeight(),
											true
									),
									bevel);


					final Bitmap finalBg = bg;
					Static.handler.post(
							new Runnable() {
								public void run() {
									bg_view.setImageBitmap(finalBg);
								}
							}
					);
				}
			}).start();

		}

	}

	@Override protected View create(LayoutInflater inflater, ViewGroup viewGroup, Context context) {
		view = inflater.inflate(R.layout.part_user_info, viewGroup, false);
		ViewSugar.bind(this, view);
		Static.bus.register(this);

		((TextView) view.findViewById(R.id.nick)).setText(profile.login);
		((TextView) view.findViewById(R.id.name)).setText(profile.name);



		view.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
			@Override public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
				Static.img.download(profile.photo);
				Static.img.download(profile.big_icon);
			}
		});

		return view;
	}

	@Override protected void onRemove(View view, ViewGroup parent, Context context) {
		Static.bus.unregister(this);
		super.onRemove(view, parent, context);
	}
}
