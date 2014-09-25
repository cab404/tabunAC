package com.cab404.ponyscape.parts;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.cab404.acli.Part;
import com.cab404.libtabun.data.Letter;
import com.cab404.moonlight.util.SU;
import com.cab404.ponyscape.R;
import com.cab404.ponyscape.bus.E;
import com.cab404.ponyscape.utils.Simple;
import com.cab404.ponyscape.utils.Static;
import com.cab404.ponyscape.utils.text.HtmlRipper;

/**
 * @author cab404
 */
public class LetterPart extends Part {

	public final Letter letter;
	private ViewGroup view;

	public LetterPart(Letter letter) {
		this.letter = letter;
	}
	private HtmlRipper ripper;

	@Override protected View create(LayoutInflater inflater, final ViewGroup viewGroup, final Context context) {
		Static.bus.register(this);
		view = (ViewGroup) inflater.inflate(R.layout.part_topic, viewGroup, false);

		((TextView) view.findViewById(R.id.title))
				.setText(SU.deEntity(letter.title));

		ripper = new HtmlRipper((ViewGroup) view.findViewById(R.id.content));
		ripper.escape(letter.text);
		view.findViewById(R.id.content).addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
			int last_width = 0;
			@Override public void onLayoutChange(View v, int l, int t, int r, int b, int oL, int oT, int oR, int oB) {
				/*
				 * Запускаем обновление раскладки только при изменении ширины.
				 * Если убрать, то WebView заспамит в лог кучу ошибок: так он кидает только одну :D
				 */
				if (last_width != view.getWidth()) {
					last_width = view.getWidth();
					ripper.layout();
				}
			}
		});

		view.findViewById(R.id.tags).setVisibility(View.GONE);
		view.findViewById(R.id.plus).setVisibility(View.GONE);
		view.findViewById(R.id.zero).setVisibility(View.GONE);
		view.findViewById(R.id.minus).setVisibility(View.GONE);
		view.findViewById(R.id.rating).setVisibility(View.GONE);

		((TextView) view.findViewById(R.id.data))
				.setText(Simple.buildRecipients(context, letter));

		// Для бегущей строки.
		view.findViewById(R.id.data)
				.setSelected(true);

		view.findViewById(R.id.rating).setVisibility(View.GONE);

		view.findViewById(R.id.plus).setOnClickListener(new View.OnClickListener() {
			@Override public void onClick(View view) {
				Static.bus.send(new E.Commands.Run("votefor post " + letter.id + " 1"));
			}
		});

		view.findViewById(R.id.zero).setOnClickListener(new View.OnClickListener() {
			@Override public void onClick(View view) {
				Static.bus.send(new E.Commands.Run("votefor post " + letter.id + " 0"));
			}
		});

		view.findViewById(R.id.minus).setOnClickListener(new View.OnClickListener() {
			@Override public void onClick(View view) {
				Static.bus.send(new E.Commands.Run("votefor post " + letter.id + " -1"));
			}
		});


		StringBuilder info = new StringBuilder("#" + letter.id);

		if (letter.comments != 0) {
			info
					.append('\n')
					.append(letter.comments)
					.append(" ")
					.append(context.getResources().getQuantityString(R.plurals.comments, letter.comments));
			if (letter.comments_new != 0)
				info
						.append(", ")
						.append(letter.comments_new)
						.append(" ")
						.append(context.getResources().getQuantityString(R.plurals.new_comments, letter.comments_new));

		}

		((TextView) view.findViewById(R.id.id)).setText(info);


		view.findViewById(R.id.favourite).setVisibility(View.GONE);

//		view.setAlpha(0);
//		view.animate().alpha(1).setDuration(200);

		return view;
	}

	@Override protected void onRemove(View view, ViewGroup parent, Context context) {
		super.onRemove(view, parent, context);
		Static.bus.unregister(this);

		ripper.destroy();
	}

	//	int initial_height = 0;
	public void hide() {
//		initial_height = view.getHeight();
//		Anim.resize(view, 0, -1, 200, null);
		Static.bus.send(new E.Parts.Hide(this));
	}

	public void show() {
		Static.bus.send(new E.Parts.Show(this));
//		Anim.resize(view, initial_height, -1, 200, new Runnable() {
//			@Override public void run() {
//				view.getLayoutParams().height = ViewGroup.LayoutParams.WRAP_CONTENT;
//			}
//		});
	}

}
