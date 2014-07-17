package com.cab404.ponyscape.parts;

import android.view.View;
import com.cab404.jconsol.CommandHolder;
import com.cab404.ponyscape.utils.Static;

/**
 * @author cab404
 */

public class Help extends AbstractTextPart {


	@Override protected CharSequence getText() {

		StringBuilder data = new StringBuilder();
		for (CommandHolder h : Static.cm.registered()) {
			data
					.append((h.prefix + " " + h.annnotation.command()).trim()).append(" ");
			for (Class clazz : h.annnotation.params()) {
				data.append(clazz.getSimpleName()).append(" ");
			}
			data.append("\n");
		}
		data.deleteCharAt(data.length() - 1);
		return data;

	}

	@Override public void onClick(View view) {
		delete();
	}
}
