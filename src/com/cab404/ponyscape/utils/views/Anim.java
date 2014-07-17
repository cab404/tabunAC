package com.cab404.ponyscape.utils.views;

import android.animation.Animator;
import android.view.View;

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
		view.setVisibility(View.VISIBLE);
		view.animate().alpha(1).setDuration(500).setListener(null);
	}
}
