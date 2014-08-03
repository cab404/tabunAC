package com.cab404.ponyscape.utils;

import android.animation.Animator;
import android.graphics.drawable.BitmapDrawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.util.HashMap;

/**
 * @author cab404
 */
public class Anim {
	public static void fadeOut(final View view) {
		view.animate().alpha(0).setDuration(500).setListener(new Animator.AnimatorListener() {
			@Override public void onAnimationStart(Animator animator) {}
			@Override public void onAnimationCancel(Animator animator) {}
			@Override public void onAnimationRepeat(Animator animator) {}
			@Override public void onAnimationEnd(Animator animator) {
				view.setVisibility(View.INVISIBLE);
			}
		});
	}

	public static void fadeIn(final View view) {
		view.animate().alpha(1).setDuration(500).setListener(null);
	}

	/**
	 * Replaces view with it's image
	 */
	public static void capsulize(ViewGroup layout) {
		layout.buildDrawingCache();

		ImageView imageView = new ImageView(layout.getContext());
		imageView.setImageDrawable(
				new BitmapDrawable(
						layout.getContext().getResources(),
						layout.getDrawingCache()
				)
		);
		for (int i = 0; i < layout.getChildCount(); i++)
			layout.getChildAt(i).setVisibility(View.GONE);

		layout.addView(imageView);

		imageView.getLayoutParams().width =
				imageView.getLayoutParams().height =
						ViewGroup.LayoutParams.MATCH_PARENT;
	}


	/**
	 * Replaces view with it's image
	 */
	public static void decapsulize(ViewGroup layout) {
		layout.removeViewAt(layout.getChildCount() - 1);
		for (int i = 0; i < layout.getChildCount(); i++)
			layout.getChildAt(i).setVisibility(View.VISIBLE);
	}


	public static void resize(final ViewGroup view, final int newHeight, final int newWidth, final int animLen, final Runnable onFinish) {
		final int startWidth = view.getLayoutParams().width;
		final int startHeight = view.getLayoutParams().height;

		final HashMap<View, Integer> state = new HashMap<>();

		view.buildDrawingCache();
		ImageView imageView = new ImageView(view.getContext());
		imageView.setImageDrawable(
				new BitmapDrawable(
						view.getContext().getResources(),
						view.getDrawingCache()
				)
		);
		for (int i = 0; i < view.getChildCount(); i++) {
			state.put(view.getChildAt(i), view.getVisibility());
			view.getChildAt(i).setVisibility(View.GONE);
		}
		view.addView(imageView);


		imageView.getLayoutParams().width =
				imageView.getLayoutParams().height =
						ViewGroup.LayoutParams.MATCH_PARENT;

		Static.handler.post(new Runnable() {
			int go = 0;
			int delay = 15;  // ~30 FPS
			@Override public void run() {
				go += delay;
				if (go < animLen) {

					if (newWidth != -1)
						view.getLayoutParams().width = (int) (startWidth + (newWidth - startWidth) * ((float) go / animLen));

					if (newHeight != -1)
						view.getLayoutParams().height = (int) (startHeight + (newHeight - startHeight) * ((float) go / animLen));

					Static.handler.postDelayed(this, delay);
				} else {

					if (newWidth != -1)
						view.getLayoutParams().width = newWidth;

					if (newHeight != -1)
						view.getLayoutParams().height = newHeight;

					if (onFinish != null)
						onFinish.run();

					view.removeViewAt(view.getChildCount() - 1);
					for (int i = 0; i < view.getChildCount(); i++)
						//noinspection ResourceType
						view.getChildAt(i).setVisibility(state.get(view.getChildAt(i)));

				}
				view.requestLayout();
			}
		});
	}
}
