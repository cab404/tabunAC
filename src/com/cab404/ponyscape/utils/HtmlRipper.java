package com.cab404.ponyscape.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.text.*;
import android.text.method.LinkMovementMethod;
import android.text.style.*;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.TextView;
import com.cab404.jconsol.util.ArrayMap;
import com.cab404.moonlight.parser.HTMLTree;
import com.cab404.moonlight.parser.Tag;
import com.cab404.moonlight.util.SU;
import com.cab404.ponyscape.R;
import com.cab404.ponyscape.bus.AppContextExecutor;
import com.cab404.ponyscape.bus.events.DataAcquired;
import com.cab404.sjbus.Bus;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * Rips html into view list
 *
 * @author cab404
 */
public class HtmlRipper {

	private ViewGroup layout;
	List<Runnable> onDestroy;
	ArrayList<View> cached_contents;

	public HtmlRipper(ViewGroup layout) {
		cached_contents = new ArrayList<>();
		onDestroy = new ArrayList<>();
		this.layout = layout;
	}

	public void destroy() {
		for (Runnable runnable : onDestroy)
			runnable.run();
	}

	public void changeLayout(ViewGroup group) {
		layout = group;
		group.removeViews(0, group.getChildCount());

		for (View view : cached_contents) {
			if (view.getParent() != null)
				((ViewGroup) view.getParent()).removeView(view);
			group.addView(view);
		}

		layout.requestLayout();
	}

	public void escape(String text) {
		destroy();
		onDestroy.clear();
		cached_contents.clear();

		escape(text, layout);
		for (int i = 0; i < layout.getChildCount(); i++) {
			cached_contents.add(layout.getChildAt(i));
		}
	}

	private static int indexOf(CharSequence toProcess, int start, char ch) {
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

	/**
	 * Немного переписанный SU.deEntity
	 */
	public static void deEntity(Editable data) {

		int index = 0;
		int end_index;

		while ((index = SU.indexOf('&', data, index)) != -1) {

			end_index = SU.indexOf(';', data, index);

			if (end_index == -1) break;

			String inner = String.valueOf(data.subSequence(index + 1, end_index));

			// Если это числовой тег (?), то попытаемся его воспроизвести.
			if (inner.startsWith("#"))
				try {

					char uni = (char) Integer.parseInt(inner.substring(1), 16);

					data.replace(index, end_index + 1, String.valueOf(uni));

				} catch (NumberFormatException | IndexOutOfBoundsException e) {

					index++;

				}
			else if (SU.HTML_ESCAPE_SEQUENCES.containsKey(inner)) {

				data.replace(index, end_index + 1, String.valueOf(SU.HTML_ESCAPE_SEQUENCES.get(inner)));

			} else index++;

		}

	}

	/**
	 * Deletes recurring chars.<br/>
	 * <pre>("  a  b  c", ' ') = " a b c"</pre>
	 */
	public static void removeRecurringChars(Editable modify, char remove) {

		for (int i = 0; i < modify.length() - 1; ) {
			if (modify.charAt(i) == remove) {
				while ((i + 1 < modify.length() - 1) && modify.charAt(i + 1) == remove) {
					modify.delete(i, i + 1);
				}
			}
			i++;
		}

	}

	public void simpleEscape(final TextView target, final String text, final Context context) {
		final SpannableStringBuilder builder = new SpannableStringBuilder(text);
		HTMLTree tree = new HTMLTree(text);
		HashSet<String> loadImages = new HashSet<>();
		final ArrayMap<String, ImageSpan> targets = new ArrayMap<>();

		int off = 0;
		for (final Tag tag : tree) {
			try {
				if (tag.isOpening())
					switch (tag.name) {
						case "strong":
							builder.setSpan(
									new StyleSpan(Typeface.BOLD),
									off + tag.end,
									off + tree.get(tree.getClosingTag(tag)).start,
									Spanned.SPAN_INCLUSIVE_EXCLUSIVE
							);
							break;
						case "em":
							builder.setSpan(
									new StyleSpan(Typeface.ITALIC),
									off + tag.end,
									off + tree.get(tree.getClosingTag(tag)).start,
									Spanned.SPAN_INCLUSIVE_EXCLUSIVE
							);
							break;
						case "s":
							builder.setSpan(
									new StrikethroughSpan(),
									off + tag.end,
									off + tree.get(tree.getClosingTag(tag)).start,
									Spanned.SPAN_INCLUSIVE_EXCLUSIVE
							);
							break;
						case "u":
							builder.setSpan(
									new UnderlineSpan(),
									off + tag.end,
									off + tree.get(tree.getClosingTag(tag)).start,
									Spanned.SPAN_INCLUSIVE_EXCLUSIVE
							);
							break;
						// Пока так.
						case "sup":
						case "small":
						case "sub":
							builder.setSpan(
									new RelativeSizeSpan(0.5f),
									off + tag.end,
									off + tree.get(tree.getClosingTag(tag)).start,
									Spanned.SPAN_INCLUSIVE_EXCLUSIVE
							);
							break;
						case "a":
							String link = tag.get("href");

							if (link.isEmpty())
								continue;

							if (link.startsWith("/"))
								link = "http://" + Static.user.getHost().getHostName() + link;

							Log.v("TextEscaper", "LINK '" + link + "'");

							builder.setSpan(
									new URLSpan(link),
									off + tag.end,
									off + tree.get(tree.getClosingTag(tag)).start,
									Spanned.SPAN_INCLUSIVE_EXCLUSIVE
							);
							break;
						case "blockquote":
							builder.setSpan(
									new QuoteSpan(Color.GRAY),
									off + tag.end,
									off + tree.get(tree.getClosingTag(tag)).start,
									Spanned.SPAN_INCLUSIVE_EXCLUSIVE
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
												off + tag.end,
												off + tree.get(tree.getClosingTag(tag)).start,
												Spanned.SPAN_INCLUSIVE_EXCLUSIVE
										);
										break;
									case "center":
										builder.setSpan(
												new AlignmentSpan() {
													@Override public Layout.Alignment getAlignment() {return Layout.Alignment.ALIGN_CENTER;}
												},
												off + tag.end,
												off + tree.get(tree.getClosingTag(tag)).start,
												Spanned.SPAN_INCLUSIVE_EXCLUSIVE
										);
										break;
									case "left":
										builder.setSpan(
												new AlignmentSpan() {
													@Override public Layout.Alignment getAlignment() {return Layout.Alignment.ALIGN_NORMAL;}
												},
												off + tag.end,
												off + tree.get(tree.getClosingTag(tag)).start,
												Spanned.SPAN_INCLUSIVE_EXCLUSIVE
										);
										break;
								}
							switch (tag.get("class")) {
								case "red":
									builder.setSpan(
											new ForegroundColorSpan(Color.parseColor("#962323")),
											off + tag.end,
											off + tree.get(tree.getClosingTag(tag)).start,
											Spanned.SPAN_INCLUSIVE_EXCLUSIVE
									);
									break;
								case "green":
									builder.setSpan(
											new ForegroundColorSpan(Color.parseColor("#529041")),
											off + tag.end,
											off + tree.get(tree.getClosingTag(tag)).start,
											Spanned.SPAN_INCLUSIVE_EXCLUSIVE
									);
									break;
								case "blue":
									builder.setSpan(
											new ForegroundColorSpan(Color.parseColor("#261474")),
											off + tag.end,
											off + tree.get(tree.getClosingTag(tag)).start,
											Spanned.SPAN_INCLUSIVE_EXCLUSIVE
									);
									break;
								case "spoiler-gray":
									builder.setSpan(
											new LitespoilerSpan(),
											off + tag.end,
											off + tree.get(tree.getClosingTag(tag)).start,
											Spanned.SPAN_INCLUSIVE_EXCLUSIVE
									);
									break;
							}
							break;
						case "h4":
							builder.setSpan(
									new RelativeSizeSpan(1.5f),
									off + tag.end,
									off + tree.get(tree.getClosingTag(tag)).start,
									Spanned.SPAN_INCLUSIVE_EXCLUSIVE
							);
							builder.insert(off++ + tree.get(tree.getClosingTag(tag)).start, "\n");
							break;
						case "h5":
							builder.setSpan(
									new RelativeSizeSpan(1.25f),
									off + tag.end,
									off + tree.get(tree.getClosingTag(tag)).start,
									Spanned.SPAN_INCLUSIVE_EXCLUSIVE
							);
							builder.insert(off++ + tree.get(tree.getClosingTag(tag)).start, "\n");
							break;
					}
				else if (tag.isStandalone())
					switch (tag.name) {
						case "hr":
							final Bitmap bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888);
							bitmap.setPixel(0, 0, Color.BLACK);
							builder.setSpan(
									new ImageSpan(context, bitmap),
									off + tag.start,
									off + tag.end,
									Spanned.SPAN_INCLUSIVE_EXCLUSIVE
							);
							break;
						case "br":
//							builder.insert(off++ + tag.end, "\n");       // Используем расставление из html, ибо pre.
							break;
						case "img":
							Log.v("TextEscaper", "IMAGE");
							if (tag.get("src").isEmpty()) continue;

							builder.insert(off + tag.start, "||image||");
							Bitmap bm = Bitmap.createBitmap(50, 50, Bitmap.Config.ARGB_8888);
							bm.eraseColor(Color.RED);
							String src = tag.get("src");

							final ImageSpan replacer = new ImageSpan(context, bm);

							builder.setSpan(
									replacer,
									off + tag.start,
									off + tag.start + "||image||".length(),
									Spanned.SPAN_INCLUSIVE_EXCLUSIVE
							);
							builder.setSpan(
									new URLSpan(tag.get("src")),
									off + tag.start,
									off + tag.start + "||image||".length(),
									Spanned.SPAN_INCLUSIVE_EXCLUSIVE
							);

							targets.add(src, replacer);
							loadImages.add(src);

							off += "||image||".length();
							break;
					}
			} catch (HTMLTree.TagNotFoundException e) {
				Log.e("Tag escaper", "tag not found : " + tag, e);
			}
		}

		removeAllTags(builder);
		removeRecurringChars(builder, '\n');
		deEntity(builder);

		target.setText(builder);

		final Object reader = new Object() {
			@Bus.Handler(executor = AppContextExecutor.class)
			public void image(DataAcquired.ImageLoaded loaded) {
				ImageSpan[] spans = builder.getSpans(0, builder.length(), ImageSpan.class);

				Bitmap use = loaded.loaded;
				int width = (int) (target.getWidth() - target.getTextSize());
				if (use.getWidth() > width) {
					int height = (int) (width * (use.getHeight() / (float) use.getWidth()));
					if (width != 0 && height != 0)
						use = Bitmap.createScaledBitmap(
								use,
								width,
								height,
								true
						);
				}

				for (ImageSpan span : targets.getValues(loaded.src)) {
					int start = builder.getSpanStart(span);
					int end = builder.getSpanEnd(span);
					if (start == -1) {
						Log.w("HtmlRipper", "Странная фигня тут: " + text);
						continue;
					}

					builder.removeSpan(span);
					builder.setSpan(
							new ImageSpan(context, use),
							start,
							end,
							Spanned.SPAN_INCLUSIVE_EXCLUSIVE
					);
					target.setText(builder);
				}
			}
		};

		Static.bus.register(reader);
		for (String str : loadImages)
			Static.img.download(str);


		onDestroy.add(new Runnable() {
			@Override public void run() {
				Static.bus.unregister(reader);
			}
		});

	}

	private TextView form(String text, Context context) {
		TextView view = new TextView(context);
		simpleEscape(view, text, context);
		view.setTextIsSelectable(true);
		view.setMovementMethod(LinkMovementMethod.getInstance());
		return view;
	}

	/**
	 * Вставляет в группу новый набар контента. Удаляет предыдущий.
	 */
	@SuppressWarnings("deprecation")
	private void escape(String text, ViewGroup group) {
		Context context = group.getContext();
		group.removeViews(0, group.getChildCount());
		HTMLTree tree = new HTMLTree(text);

		int start_index = 0;

		for (int i = 0; i < tree.size(); i++) {

			Tag tag = tree.get(i);

			// Спойлеры
			if ("span".equals(tag.name) && "spoiler".equals(tag.get("class"))) {
				// Заливаем набранный чистый текст.
				TextView pre_text = form(tree.html.subSequence(start_index, tag.start).toString(), context);
				group.addView(pre_text);

				View spoiler = formSpoiler(tree.getContents(tag), context, group);

				// Если тела спойлера нет, то возвращается null.
				if (spoiler != null)
					group.addView(spoiler);

				// Закрываем и двигаем индекс.
				Tag closing = tree.get(tree.getClosingTag(tag));
				i = closing.index - tree.offset();
				start_index = closing.end;
			}

			// Видео и пр.
			if ("iframe".equals(tag.name)) {
				// Заливаем набранный чистый текст.
				TextView pre_text = form(tree.html.subSequence(start_index, tag.start).toString(), context);
				group.addView(pre_text);

				final WebView iframe = new WebView(context);
				String src = tag.get("src");

				//noinspection Annotator
				iframe.getSettings().setJavaScriptEnabled(true);
				iframe.setBackgroundColor(Color.TRANSPARENT);
				iframe.getSettings().setPluginState(WebSettings.PluginState.ON);
				iframe.loadUrl(src);
				iframe.setWebChromeClient(new WebChromeClient());

				group.addView(iframe);

				onDestroy.add(new Runnable() {
					@Override public void run() {
						iframe.destroy();
					}
				});

				iframe.getLayoutParams().width = ViewGroup.LayoutParams.MATCH_PARENT;
				iframe.getLayoutParams().height = (int) (context.getResources().getDisplayMetrics().widthPixels * (2f / 3));

				iframe.requestLayout();

				// Закрываем и двигаем индекс.
				Tag closing = tree.get(tree.getClosingTag(tag));
				i = closing.index - tree.offset();
				start_index = closing.end;
			}

			// Кат
			if ("a".equals(tag.name) && tag.get("href") != null && tag.get("href").endsWith("#cut") && !tag.isStandalone()) {
				TextView pre_text = form(tree.html.subSequence(start_index, tag.start).toString(), context);
				group.addView(pre_text);

				int px_padding = context.getResources().getDimensionPixelSize(R.dimen.margins);
				// Немного костыльно, но сойдёт.
				TextView cut = form(tag + tree.getContents(tag).trim() + "</a>", context);
				cut.setBackgroundResource(R.drawable.bg_cut);
				cut.setPadding(px_padding, px_padding, px_padding, px_padding);
				group.addView(cut);
				cut.getLayoutParams().width = ViewGroup.LayoutParams.WRAP_CONTENT;
				cut.requestLayout();

				// Закрываем и двигаем индекс.
				Tag closing = tree.get(tree.getClosingTag(tag));
				i = closing.index - tree.offset();
				start_index = closing.end;
			}

			// Код
			if ("pre".equals(tag.name) && !tag.isStandalone()) {
				TextView pre_text = form(tree.html.subSequence(start_index, tag.start).toString(), context);
				group.addView(pre_text);

				int px_padding = context.getResources().getDimensionPixelSize(R.dimen.margins);

				TextView code = form(tree.getContents(tag).trim(), context);
				code.setBackgroundResource(R.drawable.bg_code);
				code.setPadding(px_padding, px_padding, px_padding, px_padding);
				code.setTypeface(Typeface.MONOSPACE);
				code.setTextSize(code.getTextSize() * 0.5f);
				code.setTextColor(context.getResources().getColor(R.color.code_color));

				group.addView(code);

				// Закрываем и двигаем индекс.
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
							context
					)
			);
		}

	}

	private View formSpoiler(String text, Context context, ViewGroup group) {
		final HTMLTree tree = new HTMLTree(text);

		final View view = LayoutInflater.from(context).inflate(R.layout.body_spoiler, group, false);

		Tag header = null;
		Tag body_search = null;

		for (Tag tag : tree) {
			if ("spoiler-title".equals(tag.get("class")) && header == null) header = tag;
			if ("spoiler-body".equals(tag.get("class")) && body_search == null) body_search = tag;
		}

		final Tag body = body_search;

		if (body == null)
			return null;


		ViewGroup header_layout = (ViewGroup) view.findViewById(R.id.header);

		if (header != null)
			escape(tree.getContents(header), header_layout);
		else
			escape("Спойлер", header_layout);

		// Отключаем перемещение по ссылкам в заголовке спойлера
		for (int i = 0; i < header_layout.getChildCount(); i++) {
			View ind = header_layout.getChildAt(i);
			if (ind instanceof TextView) {
				TextView txt = (TextView) ind;
				txt.setMovementMethod(null);
			}
		}


		view.findViewById(R.id.header).setOnClickListener(new View.OnClickListener() {
			@Override public void onClick(View non_used) {

				if (view.findViewById(R.id.body).getVisibility() == View.GONE)
					view.findViewById(R.id.body).setVisibility(View.VISIBLE);
				else
					view.findViewById(R.id.body).setVisibility(View.GONE);

				view.getParent().requestLayout();

				if (((ViewGroup) view.findViewById(R.id.body)).getChildCount() == 0)
					escape(tree.getContents(body), (ViewGroup) view.findViewById(R.id.body));
			}
		});


		return view;
	}

	private static class HandlingImageSpan extends ImageSpan {
		private final Uri uri;

		public HandlingImageSpan(Context context, Uri uri) {
			super(context, uri);
			this.uri = uri;
		}

		@Override public Drawable getDrawable() {
			return super.getDrawable();
		}


		public void handleLoadedBitmap() {

		}

	}

	private static class LitespoilerSpan extends ClickableSpan {
		boolean hidden = true;

		@Override public void updateDrawState(TextPaint ds) {
			ds.bgColor = hidden ? ds.getColor() : Color.TRANSPARENT;
		}

		@Override public void onClick(View view) {
			hidden = !hidden;
			TextView text = (TextView) view;

			// Инвалидэйтим вот так вот.
			text.setText(text.getText());
		}
	}


}
