package com.cab404.ponyscape.parts;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.cab404.acli.Part;
import com.cab404.ponyscape.R;

/**
 * @author cab404
 */
public class ConfirmPart extends Part {

	private final CharSequence question;
	private final ResultHandler handler;
	private boolean resolved = false;

	private void resolve(boolean ok) {
		if (!resolved) {
			resolved = true;
			handler.resolved(ok);
		}
	}

	public ConfirmPart(CharSequence question, ResultHandler handler) {
		this.question = question;
		this.handler = handler;
	}

	public static interface ResultHandler {
		public void resolved(boolean ok);
	}

	@Override protected View create(LayoutInflater inflater, ViewGroup viewGroup, Context context) {
		View view = inflater.inflate(R.layout.part_confirm, viewGroup, false);

		((TextView) view.findViewById(R.id.title)).setText(question);
		view.findViewById(R.id.ok).setOnClickListener(new View.OnClickListener() {
			@Override public void onClick(View v) {
				resolve(true);
				delete();
			}
		});
		view.findViewById(R.id.cancel).setOnClickListener(new View.OnClickListener() {
			@Override public void onClick(View v) {
				resolve(false);
				delete();
			}
		});
		return view;
	}

	@Override protected void onRemove(View view, ViewGroup parent, Context context) {
		resolve(false);
		super.onRemove(view, parent, context);
	}
}
