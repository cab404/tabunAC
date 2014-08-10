package com.cab404.ponyscape.parts;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import com.cab404.acli.Part;

/**
 * @author cab404
 */
public class LoadingPart extends Part {

	@Override protected View create(LayoutInflater inflater, ViewGroup viewGroup, Context context) {
		ProgressBar bar = new ProgressBar(context);
		viewGroup.addView(bar);
		viewGroup.removeView(bar);

		bar.setMax(10000);


		return null;
	}

}
