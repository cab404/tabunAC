package com.cab404.ponyscape.parts.editor.plugins;

import android.widget.EditText;

/**
* @author cab404
*/
public abstract class WrapEditorPlugin implements EditorPlugin {
	protected abstract CharSequence getStart();
	protected abstract CharSequence getEnd();

	@Override public void performAction(EditText text) {
		CharSequence start = getStart();
		CharSequence end = getEnd();

		int selectionStart;
		int selectionEnd;
		boolean sel = text.isSelected();

		selectionEnd = text.getSelectionEnd();
		selectionStart = text.getSelectionStart();

		text.getText().insert(selectionEnd, end);
		text.getText().insert(selectionStart, start);

		if (sel)
			text.setSelection(selectionStart, start.length() + end.length() + selectionEnd);
		else
			text.setSelection(selectionStart + start.length());
	}
}
