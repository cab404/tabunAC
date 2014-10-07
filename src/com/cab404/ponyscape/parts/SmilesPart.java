package com.cab404.ponyscape.parts;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import com.cab404.acli.Part;
import com.cab404.ponyscape.R;

/**
 * @author cab404
 */
public class SmilesPart extends Part {

	@Override protected View create(LayoutInflater inflater, ViewGroup viewGroup, Context context) {
		View view = inflater.inflate(R.layout.part_smilopack, viewGroup, false);
		LinearLayout list = (LinearLayout) view.findViewById(R.id.smiles);

		int[] images = {
				R.drawable.ic_editor_bold,
				R.drawable.ic_bar_fav,
				R.drawable.ic_bar_settings,
				R.drawable.ic_reply,
				R.drawable.ic_collapse

		};

		for (int i = 0; i < 10; i++) {
			ImageView test = new ImageView(context);
			test.setImageResource(images[((int) (Math.random() * images.length))]);
			list.addView(test);
		}

		return view;
	}

}
