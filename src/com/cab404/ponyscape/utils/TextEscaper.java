package com.cab404.ponyscape.utils;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.text.Layout;
import android.text.SpannableStringBuilder;
import android.text.style.*;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.cab404.moonlight.parser.HTMLTree;
import com.cab404.moonlight.parser.Tag;
import com.cab404.moonlight.util.SU;
import com.cab404.ponyscape.R;

/**
 * @author cab404
 */
public class TextEscaper {

	private static int indexOf(SpannableStringBuilder toProcess, int start, char ch) {
		if (start >= toProcess.length()) return -1;

		for (int i = start; i < toProcess.length(); i++)
			if (toProcess.charAt(i) == ch) return i;

		return -1;
	}

	/**
	 * Searches for HTML tags and removes them.
	 */
	private static SpannableStringBuilder removeAllTags(SpannableStringBuilder toProcess) {
		int s;

		while ((s = indexOf(toProcess, 0, '<')) != -1) {
			int f = indexOf(toProcess, s, '>');
			if (f == -1) break;
			toProcess.delete(s, f + 1);
		}

		return toProcess;
	}


	public static CharSequence simpleEscape(String text, Context context) {
		SU.deEntity(text);
		SpannableStringBuilder builder = new SpannableStringBuilder(text);
		HTMLTree tree = new HTMLTree(text);

		for (Tag tag : tree) {
			try {
				if (tag.isOpening())
					switch (tag.name) {
						case "strong":
							builder.setSpan(
									new StyleSpan(Typeface.BOLD),
									tag.end,
									tree.get(tree.getClosingTag(tag)).start,
									0
							);
							break;
						case "em":
							builder.setSpan(
									new StyleSpan(Typeface.ITALIC),
									tag.end,
									tree.get(tree.getClosingTag(tag)).start,
									0
							);
							break;
						case "s":
							builder.setSpan(
									new StrikethroughSpan(),
									tag.end,
									tree.get(tree.getClosingTag(tag)).start,
									0
							);
							break;
						case "u":
							builder.setSpan(
									new UnderlineSpan(),
									tag.end,
									tree.get(tree.getClosingTag(tag)).start,
									0
							);
							break;
						case "sup":
							builder.setSpan(
									new RelativeSizeSpan(0.5f),
									tag.end,
									tree.get(tree.getClosingTag(tag)).start,
									0
							);

							break;
						case "small":
							builder.setSpan(
									new RelativeSizeSpan(0.5f),
									tag.end,
									tree.get(tree.getClosingTag(tag)).start,
									0
							);
							break;
						case "sub":
							builder.setSpan(
									new RelativeSizeSpan(0.5f),
									tag.end,
									tree.get(tree.getClosingTag(tag)).start,
									0
							);
							break;
						case "a":
							builder.setSpan(
									new URLSpan(tag.get("href")),
									tag.end,
									tree.get(tree.getClosingTag(tag)).start,
									0
							);
							break;
						case "blockquote":
							builder.setSpan(
									new QuoteSpan(Color.GRAY),
									tag.end,
									tree.get(tree.getClosingTag(tag)).start,
									0
							);
							break;
						case "span":
							if (!tag.get("align").isEmpty())
								switch (tag.get("align")) {
									case "right":
										builder.setSpan(
												new AlignmentSpan() {
													@Override public Layout.Alignment getAlignment() {return Layout.Alignment.ALIGN_OPPOSITE;}
												},
												tag.end,
												tree.get(tree.getClosingTag(tag)).start,
												0
										);
										break;
									case "center":
										builder.setSpan(
												new AlignmentSpan() {
													@Override public Layout.Alignment getAlignment() {return Layout.Alignment.ALIGN_CENTER;}
												},
												tag.end,
												tree.get(tree.getClosingTag(tag)).start,
												0
										);
										break;
									case "left":
										builder.setSpan(
												new AlignmentSpan() {
													@Override public Layout.Alignment getAlignment() {return Layout.Alignment.ALIGN_NORMAL;}
												},
												tag.end,
												tree.get(tree.getClosingTag(tag)).start,
												0
										);
										break;
								}
							switch (tag.get("class")) {
								case "red":
									builder.setSpan(
											new ForegroundColorSpan(Color.parseColor("#962323")),
											tag.end,
											tree.get(tree.getClosingTag(tag)).start,
											0
									);
									break;
								case "green":
									builder.setSpan(
											new ForegroundColorSpan(Color.parseColor("#529041")),
											tag.end,
											tree.get(tree.getClosingTag(tag)).start,
											0
									);
									break;
								case "blue":
									builder.setSpan(
											new ForegroundColorSpan(Color.parseColor("#261474")),
											tag.end,
											tree.get(tree.getClosingTag(tag)).start,
											0
									);
									break;
							}
							break;
						case "img":
							builder.setSpan(
									new ImageSpan(context, Uri.parse(tag.get("src"))),
									tag.end,
									tree.get(tree.getClosingTag(tag)).start,
									0
							);

					}
				else if (tag.isStandalone())
					switch (tag.name) {
						case "hr":
							break;
					}
			} catch (HTMLTree.TagNotFoundException e) {
				Log.e("Tag escaper", "tag not found : " + tag, e);
			}
		}


		removeAllTags(builder);

//        Au.i(tree, "Finished in " + (System.nanoTime() - time) + " ns. Text size: " + text.length() + ", tags: " + tree.copyList().size());
		return builder;
	}

	public static TextView form(String text, Context context) {
		TextView view = new TextView(context);
		view.setText(simpleEscape(text, context));
//		view.getLayoutParams().width = ViewGroup.LayoutParams.MATCH_PARENT;
//		view.getLayoutParams().height = ViewGroup.LayoutParams.WRAP_CONTENT;
		return view;
	}

	/**
	 * Вставляет в группу новый набар контента. Удаляет предыдущий.
	 */
	public static void escape(String text, ViewGroup group) {
		group.removeViews(0, group.getChildCount());
		HTMLTree tree = new HTMLTree(text);

		int start_index = 0;

		for (int i = 0; i < tree.size(); i++) {

			Tag tag = tree.get(i);

			// Спойлеры
			if ("span".equals(tag.name) && "spoiler".equals(tag.get("class"))) {
				View spoiler = formSpoiler(tree.getContents(tag), group.getContext(), group);
				TextView pre_text = form(tree.html.subSequence(start_index, tag.start).toString(), group.getContext());

				group.addView(pre_text);
				group.addView(spoiler);

				Tag closing = tree.get(tree.getClosingTag(tag));
				i = closing.index - tree.offset();
				start_index = closing.end;
			}

		}

		if (start_index < tree.html.length()) {
			group.addView(
					form(
							tree.html.subSequence(
									start_index,
									tree.html.length()
							).toString(),
							group.getContext()
					)
			);
		}

	}

	public static View formSpoiler(String text, Context context, ViewGroup group) {
		final HTMLTree tree = new HTMLTree(text);

		final View view = LayoutInflater.from(context).inflate(R.layout.body_spoiler, group, false);
		Tag header = tree.xPathFirstTag("span&class=spoiler-title");
		final Tag body = tree.xPathFirstTag("span&class=spoiler-body");

		if (body == null)
			return null;


		if (header != null)
			escape(tree.getContents(header), (ViewGroup) view.findViewById(R.id.header));
		else
			escape("Спойлер", (ViewGroup) view.findViewById(R.id.header));

		view.findViewById(R.id.header).setOnClickListener(new View.OnClickListener() {
			@Override public void onClick(View viewww) {

				if (view.findViewById(R.id.body).getVisibility() == View.GONE)
					view.findViewById(R.id.body).setVisibility(View.VISIBLE);
				else
					view.findViewById(R.id.body).setVisibility(View.GONE);

				if (((ViewGroup) view.findViewById(R.id.body)).getChildCount() == 0)
					escape(tree.getContents(body), (ViewGroup) view.findViewById(R.id.body));
			}
		});


		return view;
	}

}
