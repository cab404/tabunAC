package com.cab404.ponyscape.parts;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import com.cab404.acli.Part;
import com.cab404.jconsol.CommandHolder;
import com.cab404.ponyscape.R;
import com.cab404.ponyscape.utils.Static;
import com.cab404.ponyscape.utils.text.HtmlRipper;

/**
 * @author cab404
 */
public class HelpPart extends Part {

	HtmlRipper ripper;

	@Override protected View create(LayoutInflater inflater, ViewGroup viewGroup, Context context) {
		LinearLayout data = new LinearLayout(context);
		data.setBackgroundResource(R.drawable.bg_part);

		int padding = context.getResources().getDimensionPixelOffset(R.dimen.internal_margins);
		data.setPadding(padding, padding, padding, padding);

		StringBuilder commands = new StringBuilder();
		for (CommandHolder h : Static.cm.registered()) {
			commands
					.append((h.prefix + " " + h.annnotation.command()).trim()).append(" ");
			for (Class clazz : h.annnotation.params()) {
				commands.append(clazz.getSimpleName()).append(" ");
			}
			commands.append("\n");
		}
		commands.deleteCharAt(commands.length() - 1);

		ripper = new HtmlRipper(data);
		ripper.escape(context.getString(R.string.help) + "\n\n" + commands + "\n");

		return data;
	}

	@Override protected void onRemove(View view, ViewGroup parent, Context context) {
		super.onRemove(view, parent, context);
		ripper.destroy();
	}
}
