package com.cab404.ponyscape.parts;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.cab404.acli.Part;
import com.cab404.ponyscape.R;

/**
 * @author cab404
 */

public class Credits extends Part {

	@Override protected View create(LayoutInflater inflater, ViewGroup viewGroup, Context context) {
		View view = inflater.inflate(R.layout.about, viewGroup, false);
		view.setOnClickListener(new View.OnClickListener() {
			@Override public void onClick(View view) {
				delete();
			}
		});
		return view;
	}
}
