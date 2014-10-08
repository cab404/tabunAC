package com.cab404.ponyscape.parts.editor.plugins;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.widget.EditText;

/**
* @author cab404
*/
public interface EditorPlugin {
	public Drawable getDrawable(Context context);
	public void performAction(EditText text);
}
