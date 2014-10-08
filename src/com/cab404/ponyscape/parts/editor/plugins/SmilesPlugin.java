package com.cab404.ponyscape.parts.editor.plugins;

import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.widget.EditText;
import com.cab404.ponyscape.R;
import com.cab404.ponyscape.bus.E;
import com.cab404.ponyscape.parts.SmilesPart;
import com.cab404.ponyscape.utils.Static;

/**
 * @author cab404
 */
public class SmilesPlugin implements EditorPlugin {
	@Override public Drawable getDrawable(Context context) {
		Drawable icon = context.getResources().getDrawable(R.drawable.ic_bar_fav);
		icon.setColorFilter(context.getResources().getColor(R.color.bg_item_label), PorterDuff.Mode.SRC_ATOP);
		return icon;
	}
	@Override public void performAction(final EditText text) {
		final int start = text.getSelectionStart();

		Static.bus.send(new E.Parts.Run(new SmilesPart(new SmilesPart.SmileHandler() {
			@Override public void handle(String address) {
				text.getText().insert(start, "<img height=\"70\" src=\"" + address + "\"/>");
			}
		}), true));

	}
}
