package com.cab404.ponyscape.parts;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.cab404.acli.Part;
import com.cab404.ponyscape.R;
import com.cab404.ponyscape.utils.Static;

/**
 * @author cab404
 */
public abstract class AbstractTextPart extends Part implements View.OnClickListener {

	protected TextView text;

	protected abstract CharSequence getText();

	protected void updateText() {
		final CharSequence text = getText();
		Static.handler.post(new Runnable() {
			@Override public void run() {
				if (AbstractTextPart.this.text != null)
					AbstractTextPart.this.text.setText(text);
			}
		});
	}

	@Override protected View create(LayoutInflater inflater, ViewGroup viewGroup, Context context) {
		View view = inflater.inflate(R.layout.part_plain_text, viewGroup, false);
		view.setOnClickListener(this);

		text = ((TextView) view.findViewById(R.id.title));
		text.setText(getText());

		return view;
	}


	@Override public void onClick(View view) {
	}

	public void delete() {
		super.delete();
	}

}
