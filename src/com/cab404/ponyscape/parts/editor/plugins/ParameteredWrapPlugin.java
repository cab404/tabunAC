package com.cab404.ponyscape.parts.editor.plugins;

import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.widget.EditText;
import com.cab404.ponyscape.R;
import com.cab404.ponyscape.bus.E;
import com.cab404.ponyscape.parts.editor.EditorPart;
import com.cab404.ponyscape.utils.Static;

/**
 * @author cab404
 */
public class ParameteredWrapPlugin implements EditorPlugin {


	private final CharSequence start;
	private final CharSequence end;
	private final CharSequence request;
	private final int drawable;

	public ParameteredWrapPlugin(
			int drawable,
			CharSequence start,
			CharSequence end,
			CharSequence request
	) {
		this.start = start;
		this.end = end;
		this.request = request;
		this.drawable = drawable;
	}

	@Override public Drawable getDrawable(Context context) {
		Drawable icon = context.getResources().getDrawable(drawable);
		icon.setColorFilter(context.getResources().getColor(R.color.bg_item_label), PorterDuff.Mode.SRC_ATOP);
		return icon;
	}

	private static EditorPart request(CharSequence request, EditorPart.EditorActionHandler handler) {
		EditorPart part = new EditorPart(request, "", handler, new EditorPlugin[0]);
		part.isSingleLine = true;
		return part;
	}

	@Override public void performAction(final EditText text) {
		final int selectionStart;
		final int selectionEnd;
		boolean sel = text.isSelected();

		selectionEnd = text.getSelectionEnd();
		selectionStart = text.getSelectionStart();

		Static.bus.send(new E.Parts.Run(request(request, new EditorPart.EditorActionHandler() {
			@Override public boolean finished(CharSequence param) {
				insert(
						text,
						selectionStart,
						selectionEnd,
						String.format(start.toString(), param),
						end
				);
				return true;
			}
			@Override public void cancelled() {}
		}), true));
	}

	public void insert(EditText text, int s_st, int s_ed, CharSequence start, CharSequence end) {
		text.getText().insert(s_ed, end);
		text.getText().insert(s_st, start);
	}

}
