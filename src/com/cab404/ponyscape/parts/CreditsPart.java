package com.cab404.ponyscape.parts;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.cab404.acli.Part;
import com.cab404.ponyscape.R;
import com.cab404.ponyscape.bus.E;
import com.cab404.ponyscape.utils.Static;

/**
 * @author cab404
 */

public class CreditsPart extends Part {

	@Override protected View create(LayoutInflater inflater, ViewGroup viewGroup, Context context) {
		View view = inflater.inflate(R.layout.part_about, viewGroup, false);
		view.setOnClickListener(new View.OnClickListener() {
			@Override public void onClick(View view) {
				Static.bus.send(new E.Parts.Remove(CreditsPart.this));
			}
		});
		return view;
	}
}
