package com.cab404.ponyscape.parts.editor;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.cab404.acli.Part;
import com.cab404.ponyscape.R;
import com.cab404.ponyscape.parts.editor.plugins.EditorPlugin;
import com.cab404.ponyscape.parts.editor.plugins.ParameteredWrapPlugin;
import com.cab404.ponyscape.parts.editor.plugins.SimpleEditorPlugin;

/**
 * @author cab404
 */
public class EditorPart extends Part {

	private final CharSequence title;
	private final CharSequence initial;
	private EditorActionHandler handler;
	private EditorPlugin[] plugins;
	public boolean isSingleLine = false;


	public EditorPart(CharSequence title, CharSequence initial, EditorActionHandler handler, EditorPlugin[] plugins) {
		this.title = title;
		this.initial = initial;
		this.handler = handler;
		this.plugins = plugins;
	}

	public EditorPart(CharSequence title, CharSequence initial, EditorActionHandler handler) {
		this.title = title;
		this.initial = initial;
		this.handler = handler;
		this.plugins = new EditorPlugin[]{
				new SimpleEditorPlugin(R.drawable.ic_editor_bold, "<strong>", "</strong>"),
				new SimpleEditorPlugin(R.drawable.ic_editor_italic, "<em>", "</em>"),
				new SimpleEditorPlugin(R.drawable.ic_editor_underlined, "<u>", "</u>"),
				new SimpleEditorPlugin(R.drawable.ic_editor_strikethrough, "<s>", "</s>"),
				new SimpleEditorPlugin(R.drawable.ic_editor_quote, "<blockquote>", "</blockquote>"),
				new SimpleEditorPlugin(R.drawable.ic_editor_code, "<pre>", "</pre>"),
				new ParameteredWrapPlugin(R.drawable.ic_editor_link, "<a href=\"%s\">", "</a>", "Введите адрес ссылки."),

				new ParameteredWrapPlugin(
						R.drawable.ic_editor_spoiler,
						"<span class=\"spoiler\"><span class=\"spoiler-title\" onclick=\"return true;\">%s</span><span class=\"spoiler-body\">",
						"</span></span>",
						"Введите название спойлера"
				),

				new ParameteredWrapPlugin(
						R.drawable.ic_editor_image,
						"<img src=\"%s\"/>",
						"",
						"Введите адрес изображения"
				),
		};
	}

	boolean already_cancelled = false;
	private void cancel() {
		if (!already_cancelled) {
			already_cancelled = true;
			handler.cancelled();
		}

	}


	@Override protected View create(LayoutInflater inflater, ViewGroup viewGroup, final Context context) {
		View view = inflater.inflate(R.layout.part_redactor, viewGroup, false);

		((TextView) view.findViewById(R.id.title)).setText(title);

		final EditText editor = (EditText) view.findViewById(R.id.editor);
		editor.setText(initial);

		/** НЕ МЕНЯТЬ. Эта функция при вызове с false выставит maxLines в over9000 */
		if (isSingleLine) editor.setSingleLine(true);


		LinearLayout actions = (LinearLayout) view.findViewById(R.id.editor_actions);
		for (final EditorPlugin plugin : plugins) {
			ImageView action = new ImageView(context);
			actions.addView(action);
			LinearLayout.LayoutParams actionLP = (LinearLayout.LayoutParams) action.getLayoutParams();

			actionLP.width = actionLP.height = actions.getLayoutParams().height;
			actionLP.leftMargin = actionLP.rightMargin = actionLP.width / 8;

			action.setImageDrawable(plugin.getDrawable(context));

			action.setOnClickListener(new View.OnClickListener() {
				@Override public void onClick(View v) {
					plugin.performAction(editor);
				}
			});

			action.requestLayout();
		}

		if (plugins.length == 0) actions.setVisibility(View.GONE);

		view.findViewById(R.id.ok).setOnClickListener(new View.OnClickListener() {
			@Override public void onClick(View v) {
				if (handler.finished(editor.getText())) {
					already_cancelled = true;
					delete();
				}
			}
		});

		view.findViewById(R.id.cancel).setOnClickListener(new View.OnClickListener() {
			@Override public void onClick(View v) {
				cancel();
				delete();
			}
		});


		return view;
	}

	@Override protected void onRemove(View view, ViewGroup parent, Context context) {
		super.onRemove(view, parent, context);
		cancel();
	}

	public static interface EditorActionHandler {
		public boolean finished(CharSequence text);
		public void cancelled();
	}

	@Override public void delete() {
		super.delete();
	}
}
