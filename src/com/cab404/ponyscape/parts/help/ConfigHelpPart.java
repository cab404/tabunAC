package com.cab404.ponyscape.parts.help;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import com.cab404.acli.Part;
import com.cab404.ponyscape.R;
import com.cab404.ponyscape.utils.Static;
import com.cab404.ponyscape.utils.text.HtmlRipper;

/**
 * @author cab404
 */
public class ConfigHelpPart extends Part {

	HtmlRipper ripper;

	@Override protected View create(LayoutInflater inflater, ViewGroup viewGroup, Context context) {
		LinearLayout data = new LinearLayout(context);
		data.setBackgroundColor(Static.ctx.getResources().getColor(R.color.bg_item));
		data.setOrientation(LinearLayout.VERTICAL);

		int padding = context.getResources().getDimensionPixelOffset(R.dimen.internal_margins);
		data.setPadding(padding, padding, padding, padding);

		ripper = new HtmlRipper(data);
		ripper.escape(context.getString(R.string.help_config));

		return data;
	}

	@Override protected void onRemove(View view, ViewGroup parent, Context context) {
		super.onRemove(view, parent, context);
		ripper.destroy();
	}
}
