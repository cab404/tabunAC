package com.cab404.ponyscape.parts;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.cab404.acli.Part;
import com.cab404.moonlight.util.SU;
import com.cab404.ponyscape.R;
import com.cab404.ponyscape.bus.AppContextExecutor;
import com.cab404.ponyscape.bus.E;
import com.cab404.ponyscape.utils.Static;
import com.cab404.ponyscape.utils.animation.Anim;
import com.cab404.ponyscape.utils.state.ArchiveUtils;
import com.cab404.ponyscape.utils.text.DateUtils;
import com.cab404.ponyscape.utils.text.HtmlRipper;
import com.cab404.sjbus.Bus;

/**
 * @author cab404
 */

public class TopicPart extends Part {

	public final com.cab404.libtabun.data.Topic topic;
	private ViewGroup view;
	private String link;

	public void setLink(String link) {
		this.link = link;
	}

	public TopicPart(com.cab404.libtabun.data.Topic topic) {
		this.topic = topic;
		link = "post load " + topic.id;
	}


	private HtmlRipper ripper;

	@Override protected View create(LayoutInflater inflater, final ViewGroup viewGroup, final Context context) {
		Static.bus.register(this);
		Log.v("View", "inf_invoked");
		view = (ViewGroup) inflater.inflate(R.layout.part_topic, viewGroup, false);
		{
			((TextView) view.findViewById(R.id.title))
					.setText(SU.deEntity(topic.title));
			view.findViewById(R.id.title)
					.setOnClickListener(new View.OnClickListener() {
						@Override public void onClick(View unused) {
							Static.bus.send(new E.Commands.Run(link));
						}
					});
		}

		/* Загрузка аватарки. Складываем ссылку в тег картинки. */
		{

			ImageView avatar = (ImageView) view.findViewById(R.id.avatar);
			topic.author.fillImages();
			avatar.setTag(topic.author.small_icon);
			avatar.setImageDrawable(new ColorDrawable(Static.ctx.getResources().getColor(R.color.bg_item_shadow)));

			Static.img.download(topic.author.small_icon);

		}

		/* Отслеживаем изменения родителя. */
		{
			ripper = new HtmlRipper((ViewGroup) view.findViewById(R.id.content));
			ripper.escape(topic.text);
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

		/* Дата, автор и блог */
		{
			String blog_name = SU.deEntity(topic.blog.name);
			Spannable date =
					new SpannableStringBuilder("" +
							topic.author.login
							+ " в блоге " + blog_name
							+ " " + DateUtils.convertToString(topic.date, context)
					);

			date.setSpan(
					new ForegroundColorSpan(context.getResources().getColor(R.color.bg_item_label)),
					0,
					topic.author.login.length(),
					0
			);

			date.setSpan(
					new ForegroundColorSpan(context.getResources().getColor(R.color.bg_item_label)),
					topic.author.login.length() + 9,
					topic.author.login.length() + 9 + blog_name.length(),
					0
			);

			((TextView) view.findViewById(R.id.data)).setText(date);

		}

		/* Архивирование */
		{
			final ImageView save_button = (ImageView) view.findViewById(R.id.save);
			save_button.setColorFilter(context.getResources().getColor(
					ArchiveUtils.isPostInArchive(topic.id) ?
							R.color.font_color_green
							:
							R.color.bg_item_shadow
			));

			save_button.setOnClickListener(new View.OnClickListener() {
				@Override public void onClick(View v) {
					if (ArchiveUtils.isPostInArchive(topic.id)) {
						Static.bus.send(new E.Commands.Run("saved delete_post " + topic.id));
					} else {
						Static.bus.send(new E.Commands.Run("save post " + topic.id));
					}
				}
			});
		}

		/* Голосование */
		{
			((TextView) view.findViewById(R.id.rating))
					.setText(topic.votes);

			if (!"?".equals(topic.votes)) {
				disableVotes();
			}

			view.findViewById(R.id.plus).setOnClickListener(new View.OnClickListener() {
				@Override public void onClick(View view) {
					Static.bus.send(new E.Commands.Run("votefor post " + topic.id + " 1"));
				}
			});


			view.findViewById(R.id.zero).setOnClickListener(new View.OnClickListener() {
				@Override public void onClick(View view) {
					Static.bus.send(new E.Commands.Run("votefor post " + topic.id + " 0"));
				}
			});

			view.findViewById(R.id.minus).setOnClickListener(new View.OnClickListener() {
				@Override public void onClick(View view) {
					Static.bus.send(new E.Commands.Run("votefor post " + topic.id + " -1"));
				}
			});

		}

		/* Избранное */
		{
			ImageView fav = (ImageView) view.findViewById(R.id.favourite);
			fav.setColorFilter(Static.ctx.getResources()
							.getColor
									(
											topic.in_favourites ?
													R.color.bg_item_fav
													:
													R.color.bg_item_shadow
									)
			);
			fav.setOnClickListener(new View.OnClickListener() {
				@Override public void onClick(View v) {
					if (topic.in_favourites) {
						Static.bus.send(new E.Commands.Run("fav post " + topic.id + " -"));
					} else {
						Static.bus.send(new E.Commands.Run("fav post " + topic.id + " +"));
					}
				}
			});

		}

		/* Теги */
		{
			((TextView) view.findViewById(R.id.tags))
					.setText(SU.deEntity(SU.join(topic.tags, ", ").toString()));
		}

		/* Количество комментариев и id */
		{
			StringBuilder info = new StringBuilder("#" + topic.id);
			if (topic.comments != 0) {
				info
						.append('\n')
						.append(topic.comments)
						.append(" ")
						.append(context.getResources().getQuantityString(R.plurals.comments, topic.comments));
				if (topic.comments_new != 0)
					info
							.append(", ")
							.append(topic.comments_new)
							.append(" ")
							.append(context.getResources().getQuantityString(R.plurals.new_comments, topic.comments_new));

			}

			((TextView) view.findViewById(R.id.id)).setText(info);
		}

		/* Fadein */
		if (Build.VERSION.SDK_INT >= 12) {
			view.setAlpha(0);
			view.animate().alpha(1).setDuration(200);
		}

		return view;
	}

	private void disableVotes() {
		Anim.fadeOut(view.findViewById(R.id.minus));
		Anim.fadeOut(view.findViewById(R.id.zero));
		Anim.fadeOut(view.findViewById(R.id.plus));
	}

	@Bus.Handler(executor = AppContextExecutor.class)
	public void handleVoteChange(E.GotData.Vote.Topic vote) {
		if (vote.id == topic.id) {
			disableVotes();
			topic.votes = (vote.votes > 0 ? "+" : "") + vote.votes;
			final TextView rating = ((TextView) view.findViewById(R.id.rating));

			// Скрываем и показываем уже изменённый рейтинг.
			Anim.swapText(rating, topic.votes);

		}
	}


	@Bus.Handler
	public void archiveHandler(final E.GotData.Arch.Topic e) {
		if (topic.id == e.id)
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

	@Bus.Handler
	public void handleFavChange(final E.GotData.Fav.Topic fav) {
		if (topic.id == fav.id) {
			Static.handler.post(
					new Runnable() {
						@Override public void run() {
							Log.v("A", fav.added + "");
							topic.in_favourites = fav.added;
							Anim.recolorIcon(
									(ImageView) view.findViewById(R.id.favourite),
									Static.ctx.getResources().getColor
											(
													topic.in_favourites ?
															R.color.bg_item_fav
															:
															R.color.bg_item_shadow
											)
							);
						}
					}
			);
		}
	}

	@Bus.Handler
	public void handleImage(final E.GotData.Image.Loaded img) {
		if (topic.author.small_icon.equals(img.src)) {
			Static.handler.post(new Runnable() {
				@Override public void run() {
					ImageView avatar = (ImageView) view.findViewById(R.id.avatar);
					avatar.setImageBitmap(img.loaded);
				}
			});
		}
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
