package com.cab404.ponyscape.utils.animation;

import android.animation.TimeInterpolator;
import android.annotation.TargetApi;
import android.os.Build;

/**
 * Hop, skip and jump!
 *
 * @author cab404
 */

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class BounceInterpolator implements TimeInterpolator {

	@Override public float getInterpolation(float input) {
		float clamp = 1.5f; // Насколько далеко уходим за единицу
		float pre_clamp = 0.5f; // Когда начинаем возвращать значение к единице.

		if (input < pre_clamp)
			return (float) (Math.sqrt(input / pre_clamp) * clamp);
		else
			return (float) (clamp - (clamp - 1) * Math.sqrt((input - pre_clamp) / (1 - pre_clamp)));
	}

}
