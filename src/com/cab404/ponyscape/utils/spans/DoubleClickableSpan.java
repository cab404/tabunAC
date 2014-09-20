package com.cab404.ponyscape.utils.spans;

import android.text.style.ClickableSpan;
import android.view.View;

/**
 * Скопипастил из sweetieBot. Активирует onDoubleClick при двойном нажатии.
 *
 * @author cab404
 */

public abstract class DoubleClickableSpan extends ClickableSpan {
	/**
	 * Максимальная задержка двойного тапа.
	 */
	public int delay = 500;
	private long last_click;

	@Override public final void onClick(View widget) {
		if (last_click == 0) last_click = System.currentTimeMillis();
		else {
			if (System.currentTimeMillis() - last_click < delay) {
				onDoubleClick(widget);
			}
			last_click = 0;
		}
	}

	public abstract void onDoubleClick(View widget);


}