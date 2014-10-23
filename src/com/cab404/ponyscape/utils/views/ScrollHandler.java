package com.cab404.ponyscape.utils.views;

/**
* @author cab404
*/
public interface ScrollHandler {
	/**
	 * Сюда напрямую шлются форкнутые данные из onScrollChanged.
	 */
	void onScrolled(int y, int old_y);
	/**
	 * Сюда напрямую шлются форкнутые данные из onOverScroll.
	 * Если в onTouchEvent появился ACTION_UP, то сюда шлется (-100, false)
	 */
	void onOverScrolled(float y, boolean clamped);
}
