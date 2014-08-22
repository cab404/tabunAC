package com.cab404.ponyscape.parts;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.cab404.acli.Part;
import com.cab404.libtabun.data.LetterLabel;
import com.cab404.ponyscape.R;

/**
 * Краткий заголовок письма.
 *
 * @author cab404
 */
public class LetterLabelPart extends Part {
	private LetterLabel label;

	public LetterLabelPart(LetterLabel label) {
		this.label = label;
	}

	@Override protected View create(LayoutInflater inflater, ViewGroup viewGroup, Context context) {
		View view = inflater.inflate(R.layout.part_letter_label, viewGroup, false);


		return view;
	}
}
