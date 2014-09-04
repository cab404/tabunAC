package com.cab404.ponyscape.parts;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.cab404.acli.Part;
import com.cab404.ponyscape.R;

/**
 * @author cab404
 */
public class EditorPart extends Part {

	private final CharSequence title;
	private final CharSequence initial;
	private EditorActionHandler handler;
	private EditorPlugin[] plugins;

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
				new SimpleEditorPlugin(R.drawable.ic_editor_bold, "<b>", "</b>"),
				new SimpleEditorPlugin(R.drawable.ic_editor_italic, "<i>", "</i>"),
				new SimpleEditorPlugin(R.drawable.ic_editor_underlined, "<u>", "</u>"),
				new SimpleEditorPlugin(R.drawable.ic_editor_strikethrough, "<s>", "</s>"),
				new SimpleEditorPlugin(R.drawable.ic_editor_strikethrough, "<blockquote>", "</blockquote>"),
		};
	}

	@Override protected View create(LayoutInflater inflater, ViewGroup viewGroup, final Context context) {
		View view = inflater.inflate(R.layout.part_redactor, viewGroup, false);

		((TextView) view.findViewById(R.id.title)).setText(title);

		final EditText editor = (EditText) view.findViewById(R.id.editor);
		editor.setText(initial);


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
				if (handler.finished(editor.getText()))
					delete();
			}
		});

		view.findViewById(R.id.cancel).setOnClickListener(new View.OnClickListener() {
			@Override public void onClick(View v) {
				handler.cancelled();
				delete();
			}
		});


		return view;
	}

	public static interface EditorActionHandler {
		public boolean finished(CharSequence text);
		public void cancelled();
	}

	public static interface EditorPlugin {
		public Drawable getDrawable(Context context);
		public void performAction(EditText text);
	}

	public static abstract class WrapEditorPlugin implements EditorPlugin {
		protected abstract CharSequence getStart();
		protected abstract CharSequence getEnd();

		@Override public void performAction(EditText text) {
			if (!text.hasSelection()) return;

			CharSequence start = getStart();
			CharSequence end = getEnd();
			int selectionEnd = text.getSelectionEnd();
			int selectionStart = text.getSelectionStart();

			text.getText().insert(selectionEnd, end);
			text.getText().insert(selectionStart, start);
			text.setSelection(selectionStart, start.length() + end.length() + selectionEnd);
		}
	}

	public static class SimpleEditorPlugin extends WrapEditorPlugin {
		private final int drawable;
		private final CharSequence start;
		private final CharSequence end;

		public SimpleEditorPlugin(int drawable, CharSequence start, CharSequence end) {
			this.drawable = drawable;
			this.start = start;
			this.end = end;
		}

		@Override protected CharSequence getStart() {
			return start;
		}
		@Override protected CharSequence getEnd() {
			return end;
		}
		@Override public Drawable getDrawable(Context context) {
			return context.getResources().getDrawable(drawable);
		}
	}


}
