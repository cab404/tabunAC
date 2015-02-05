package com.cab404.ponyscape.parts;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.cab404.acli.Part;
import com.cab404.libtabun.data.Letter;
import com.cab404.moonlight.util.SU;
import com.cab404.ponyscape.R;
import com.cab404.ponyscape.bus.E;
import com.cab404.ponyscape.utils.Simple;
import com.cab404.ponyscape.utils.Static;
import com.cab404.ponyscape.utils.animation.Anim;
import com.cab404.ponyscape.utils.state.ArchiveUtils;
import com.cab404.ponyscape.utils.text.HtmlRipper;
import com.cab404.ponyscape.utils.text.Plurals;
import com.cab404.sjbus.Bus;

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


	private String link = "";

	public void setLink(String link) {
		this.link = link;
	}

	@Override protected View create(LayoutInflater inflater, final ViewGroup viewGroup, final Context context) {
		Static.bus.register(this);
		view = (ViewGroup) inflater.inflate(R.layout.part_topic, viewGroup, false);

		/* Загрузка аватарки. Складываем ссылку в тег картинки. */
		{
			ImageView avatar = (ImageView) view.findViewById(R.id.avatar);
			letter.starter.fillImages();
			avatar.setTag(letter.starter.small_icon);
			avatar.setImageDrawable(new ColorDrawable(Static.ctx.getResources().getColor(R.color.bg_item_shadow)));

			Static.img.download(letter.starter.small_icon);
		}
		/* Заголовок */
		{
			((TextView) view.findViewById(R.id.title))
					.setText(SU.deEntity(letter.title));
			view.findViewById(R.id.title)
					.setOnClickListener(new View.OnClickListener() {
						@Override public void onClick(View unused) {
							Static.bus.send(new E.Commands.Run(link));
						}
					});
		}

		/* Текст. */
		{

			ripper = new HtmlRipper((ViewGroup) view.findViewById(R.id.content));
			ripper.escape(letter.text);
			if (Build.VERSION.SDK_INT >= 11)
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
		}


		/* Скрываем ненужное */
		{
			view.findViewById(R.id.tags).setVisibility(View.GONE);
			view.findViewById(R.id.plus).setVisibility(View.GONE);
			view.findViewById(R.id.zero).setVisibility(View.GONE);
			view.findViewById(R.id.minus).setVisibility(View.GONE);
			view.findViewById(R.id.rating).setVisibility(View.GONE);
			view.findViewById(R.id.favourite).setVisibility(View.GONE);
		}

		/* Архивирование */
		{
			final ImageView save_button = (ImageView) view.findViewById(R.id.save);
			save_button.setColorFilter(context.getResources().getColor(
					ArchiveUtils.isLetterInArchive(letter.id) ?
							R.color.font_color_green
							:
							R.color.bg_item_shadow
			));

			save_button.setOnClickListener(new View.OnClickListener() {
				@Override public void onClick(View v) {
					if (ArchiveUtils.isLetterInArchive(letter.id)) {
						Static.bus.send(new E.Commands.Run("saved delete_letter " + letter.id));
					} else {
						Static.bus.send(new E.Commands.Run("save letter " + letter.id));
					}
				}
			});
		}

		((TextView) view.findViewById(R.id.data))
				.setText(Simple.buildRecipients(context, letter));

		// Для бегущей строки.
		view.findViewById(R.id.data)
				.setSelected(true);

		StringBuilder info = new StringBuilder("#" + letter.id);

		if (letter.comments != 0) {
			info
					.append('\n')
					.append(letter.comments)
					.append(" ")
					.append(Plurals.get(R.array.comments, letter.comments));
			if (letter.comments_new != 0)
				info
						.append(", ")
						.append(letter.comments_new)
						.append(" ")
						.append(Plurals.get(R.array.new_comments, letter.comments_new));

		}

		((TextView) view.findViewById(R.id.id)).setText(info);


//		view.setAlpha(0);
//		view.animate().alpha(1).setDuration(200);

		return view;
	}


	@Bus.Handler
	public void handleImage(final E.GotData.Image.Loaded img) {
		if (letter.starter.small_icon.equals(img.src)) {
			Static.handler.post(new Runnable() {
				@Override public void run() {
					ImageView avatar = (ImageView) view.findViewById(R.id.avatar);
					avatar.setImageBitmap(img.loaded);
				}
			});
		}
	}

	@Bus.Handler
	public void archiveHandler(final E.GotData.Arch.Letter e) {
		if (letter.id == e.id)
			Static.handler.post(new Runnable() {
				@Override public void run() {
					Anim.recolorIcon((ImageView) view.findViewById(R.id.save),
							Static.ctx.getResources().getColor(
									e.added ?
											R.color.font_color_green
											:
											R.color.bg_item_shadow
							));
				}
			});
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
