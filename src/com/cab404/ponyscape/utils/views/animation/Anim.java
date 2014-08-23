package com.cab404.ponyscape.utils.views.animation;

import android.animation.Animator;
import android.view.View;
import com.cab404.ponyscape.utils.Static;

/**
 * @author cab404
 */
public class Anim {
	private final static Runnable EMPTY = new Runnable() {
		@Override public void run() {}
	};

	public static void fadeOut(final View view) {
		fadeOut(view, 500);
	}

	public static void fadeOut(final View view, int duration) {
		fadeOut(view, duration, EMPTY);
	}

	public static void fadeOut(final View view, int duration, final Runnable onFinish) {
		view.animate().alpha(0).setDuration(duration).setListener(new AnimatorListenerImpl() {
			@Override public void onAnimationEnd(Animator animator) {
				view.setVisibility(View.INVISIBLE);
				onFinish.run();
			}
		});
	}

	public static void fadeIn(final View view) {
		fadeIn(view, 500);
	}

	public static void fadeIn(final View view, int duration) {
		fadeIn(view, duration, EMPTY);
	}

	public static void fadeIn(final View view, int duration, final Runnable onFinish) {
		view.setVisibility(View.VISIBLE);
		view.animate().alpha(1).setDuration(duration).setListener(new AnimatorListenerImpl() {
			@Override public void onAnimationEnd(Animator animator) {
				onFinish.run();
			}
		});
	}

	public static void resize(final View view, final int newHeight, final int newWidth, final int animLen, final Runnable onFinish) {
		final int startWidth = view.getLayoutParams().width;
		final int startHeight = view.getLayoutParams().height;

		Static.handler.post(new Runnable() {
			int go = 0;
			int delay = 17;  // ~60 FPS
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

				}
				view.requestLayout();
			}
		});
	}

	public static void shift(final View view, final int scrollX, final int animLen, final Runnable onFinish) {
		final int startScroll = view.getScrollX();

		Static.handler.post(new Runnable() {
			int go = 0;
			int delay = 17;  // ~60 FPS
			@Override public void run() {
				go += delay;
				if (go < animLen) {

					if (scrollX != -1)
						view.scrollTo((int) (startScroll + (scrollX - startScroll) * ((float) go / animLen)), 0);

					Static.handler.postDelayed(this, delay);
				} else {

					if (scrollX != -1)
						view.scrollTo(scrollX, 0);

					if (onFinish != null)
						onFinish.run();

				}
				view.requestLayout();
			}
		});
	}

	public static class AnimatorListenerImpl implements Animator.AnimatorListener {
		@Override public void onAnimationStart(Animator animation) {}
		@Override public void onAnimationEnd(Animator animation) {}
		@Override public void onAnimationCancel(Animator animation) {}
		@Override public void onAnimationRepeat(Animator animation) {}
	}
}
