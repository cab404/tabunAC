package com.cab404.ponyscape.parts;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.cab404.acli.Part;
import com.cab404.libtabun.data.Comment;
import com.cab404.ponyscape.R;
import com.cab404.ponyscape.utils.DateUtils;
import com.cab404.ponyscape.utils.TextEscaper;

/**
 * @author cab404
 */
public class CommentPart extends Part {

	private CharSequence text = null;
	private Comment comment;

	public CommentPart(Comment comment) {
		this.comment = comment;
	}

	@Override public View create(LayoutInflater inflater, ViewGroup viewGroup, final Context context) {
		View view = inflater.inflate(R.layout.part_comment, viewGroup, false);
		convert(view, context);
		return view;
	}

	public void convert(View view, Context context) {
		((TextView) view.findViewById(R.id.text))
				.setText(TextEscaper.simpleEscape(comment.text, context));
		((TextView) view.findViewById(R.id.data))
				.setText(comment.author.login + ", " + DateUtils.convertToString(comment.date, context));
		((TextView) view.findViewById(R.id.rating))
				.setText(comment.votes > 0 ? "+" + comment.votes : "" + comment.votes);



		((TextView) view.findViewById(R.id.id)).setText("#" + comment.id);
	}

	@Override protected void update(View view, ViewGroup parent, Context context) {
		super.update(view, parent, context);
		((TextView) view.findViewById(R.id.data))
				.setText(comment.author.login + ", " + DateUtils.convertToString(comment.date, context));
	}
}