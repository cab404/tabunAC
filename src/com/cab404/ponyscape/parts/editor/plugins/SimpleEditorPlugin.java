package com.cab404.ponyscape.parts.editor.plugins;

import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import com.cab404.ponyscape.R;

/**
* @author cab404
*/
public class SimpleEditorPlugin extends WrapEditorPlugin {
	private final int drawable;
	private final CharSequence start;
	private final CharSequence end;

	public SimpleEditorPlugin(int drawable, CharSequence start, CharSequence end) {
		this.drawable = drawable;
		this.start = start;
		this.end = end;
	}

	@Override protected CharSequence getStart() {
		return start;
	}
	@Override protected CharSequence getEnd() {
		return end;
	}
	@Override public Drawable getDrawable(Context context) {
		Drawable icon = context.getResources().getDrawable(drawable);
		icon.setColorFilter(context.getResources().getColor(R.color.bg_item_label), PorterDuff.Mode.SRC_ATOP);
		return icon;
	}
}
