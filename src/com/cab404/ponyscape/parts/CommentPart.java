package com.cab404.ponyscape.parts;

import android.content.Context;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.cab404.acli.Part;
import com.cab404.libtabun.data.Comment;
import com.cab404.ponyscape.R;
import com.cab404.ponyscape.bus.E;
import com.cab404.ponyscape.utils.Static;
import com.cab404.ponyscape.utils.animation.Anim;
import com.cab404.ponyscape.utils.text.DateUtils;
import com.cab404.ponyscape.utils.text.HtmlRipper;
import com.cab404.sjbus.Bus;

/**
 * @author cab404
 */
public class CommentPart extends Part {

	private final boolean isLetter;
	private CharSequence text = null;
	public final com.cab404.libtabun.data.Comment comment;
	private HtmlRipper ripper;

	View view;
	public CommentPart(com.cab404.libtabun.data.Comment comment, boolean isLetter) {
		this.isLetter = isLetter;
		Static.bus.register(this);
		this.comment = comment;
	}

	@Override public View create(LayoutInflater inflater, ViewGroup viewGroup, final Context context) {
		view = inflater.inflate(R.layout.part_comment, viewGroup, false);
		convert(view, context);
		return view;
	}

	public void convert(final View view, Context context) {
		this.view = view;
		view.setTag(comment);

		if (ripper == null) {
			ripper = new HtmlRipper((ViewGroup) view.findViewById(R.id.content));
			ripper.escape(comment.text);
		} else {
			ripper.changeLayout((ViewGroup) view.findViewById(R.id.content));
		}

		Static.img.download(comment.author.small_icon);

		/* Собираем и раскрашиваем дату и ник*/
		SpannableStringBuilder date =
				new SpannableStringBuilder(comment.author.login + " " + DateUtils.convertToString(comment.date, context));
		date.setSpan(
				new ForegroundColorSpan(context.getResources().getColor(R.color.bg_item_shadow)),
				comment.author.login.length(),
				date.length(),
				0
		);

		((TextView) view.findViewById(R.id.data)).setText(date);


		/* Выставляем рейтинг*/
		((TextView) view.findViewById(R.id.rating))
				.setText(comment.votes > 0 ? "+" + comment.votes : "" + comment.votes);


		/* Если это письмо, то отключаем рейтинг и редактирование.*/
		if (!isLetter) {
			view.findViewById(R.id.plus).setOnClickListener(new View.OnClickListener() {
				@Override public void onClick(View view) {
					Static.bus.send(new E.Commands.Run("votefor comment " + comment.id + " +1"));
				}
			});

			view.findViewById(R.id.minus).setOnClickListener(new View.OnClickListener() {
				@Override public void onClick(View view) {
					Static.bus.send(new E.Commands.Run("votefor comment " + comment.id + " -1"));
				}
			});

		}

		view.findViewById(R.id.favourite).setOnClickListener(new View.OnClickListener() {
			@Override public void onClick(View v) {
				if (comment.in_favs = !comment.in_favs) {
					Static.bus.send(new E.Commands.Run("fav comment " + comment.id + " +"));
				} else {
					Static.bus.send(new E.Commands.Run("fav comment " + comment.id + " -"));
				}
			}
		});

//		view.findViewById(R.id.favourite).setVisibility(View.GONE);
		if (isLetter) {
			view.findViewById(R.id.plus).setVisibility(View.GONE);
			view.findViewById(R.id.edit).setVisibility(View.GONE);
			view.findViewById(R.id.minus).setVisibility(View.GONE);
			view.findViewById(R.id.rating).setVisibility(View.GONE);
		}

		if (comment.is_new)
			view.findViewById(R.id.root)
					.setBackgroundColor(
							context.getResources().getColor(R.color.bg_item_new)
					);
		else
			view.findViewById(R.id.root)
					.setBackgroundColor(
							context.getResources().getColor(R.color.bg_item)
					);

		((TextView) view.findViewById(R.id.id)).setText("#" + comment.id);


	}

	public void kill() {
		Static.bus.unregister(this);
		ripper.destroy();
		ripper = null;
	}

	@Bus.Handler
	public void handleVoteChange(final E.GotData.Vote.Comment vote) {
		if (comment.id == vote.id) {
			Static.handler.post(
					new Runnable() {
						@Override public void run() {
							comment.votes = vote.votes;
							final TextView rating = (TextView) view.findViewById(R.id.rating);
							Anim.swapText(rating, (comment.votes > 0 ? "+" : "") + comment.votes);
						}
					}
			);
		}
	}


	@Bus.Handler
	public void handleImage(final E.GotData.Image.Loaded img) {
		if (((Comment) view.getTag()).author.small_icon.equals(img.src)) {
			Static.handler.post(new Runnable() {
				@Override public void run() {
					ImageView avatar = (ImageView) view.findViewById(R.id.avatar);
					avatar.setImageBitmap(img.loaded);
				}
			});


//					Anim.swapIcon(
//							((ImageView) view.findViewById(R.id.icon)),
//							new BitmapDrawable(Static.app_context.getResources(), img.loaded)
//					);
		}
	}

	@Override protected void onRemove(View view, ViewGroup parent, Context context) {
		super.onRemove(view, parent, context);
		kill();
	}
}