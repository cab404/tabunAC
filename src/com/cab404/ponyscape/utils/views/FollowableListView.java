package com.cab404.ponyscape.utils.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ListView;

/**
 * @author cab404
 */
public class FollowableListView extends ListView {
	public FollowableListView(Context context) {
		super(context);
	}
	public FollowableListView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	public FollowableListView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	private ScrollHandler handler;
	private float delta = 0; // Хранит delta Y.

	public void setHandler(ScrollHandler handler) {
		this.handler = handler;
	}

	@Override public boolean onTouchEvent(MotionEvent ev) {
		boolean b = super.onTouchEvent(ev);

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
