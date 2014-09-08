package com.cab404.ponyscape.utils.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ScrollView;

/**
 * Transparent socket above ScrollView
 *
 * @author cab404
 */
public class FollowableScrollView extends ScrollView {

	public FollowableScrollView(Context context) {
		super(context);
	}
	public FollowableScrollView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	public FollowableScrollView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	private ScrollHandler handler;

	public void setHandler(ScrollHandler handler) {
		this.handler = handler;
	}

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

	boolean scroll_enabled = true;

	@Override public int getMaxScrollAmount() {
		return isScrollEnabled() ? super.getMaxScrollAmount() : 0;
	}

	@Override public boolean onTouchEvent(MotionEvent ev) {
		boolean b = isScrollEnabled() && super.onTouchEvent(ev);

		if (ev.getAction() == MotionEvent.ACTION_UP) {
			if (handler != null)
				handler.onOverScrolled(-100, false); // Небольшой хак для отключения обновлялки.
		} else {

			if (ev.getHistorySize() > 0) {

				MotionEvent.PointerCoords coords = new MotionEvent.PointerCoords();
				MotionEvent.PointerCoords old_coords = new MotionEvent.PointerCoords();

				ev.getPointerCoords(0, coords);
				ev.getHistoricalPointerCoords(0, ev.getHistorySize() - 1, old_coords);

				delta = coords.y - old_coords.y;
			}
		}


		return b;
	}


	public boolean isScrollEnabled() {
		return scroll_enabled;
	}

	public void setScrollEnabled(boolean scroll_enabled) {
		this.scroll_enabled = scroll_enabled;
	}

	private float delta = 0; // Хранит delta Y.

	public float getDelta() {
		return delta;
	}

	@Override protected void onOverScrolled(int scrollX, int scrollY, boolean clampedX, boolean clampedY) {
		super.onOverScrolled(scrollX, scrollY, clampedX, clampedY);
		if (handler != null)
			handler.onOverScrolled(delta, clampedY);
	}

	@Override protected void onScrollChanged(int l, int t, int oldl, int oldt) {
		super.onScrollChanged(l, t, oldl, oldt);
		if (handler != null)
			handler.onScrolled(t, oldt);
	}

}
